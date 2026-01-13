package com.example.oa_system_backend.module.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.employee.entity.Employee;
import com.example.oa_system_backend.module.employee.vo.EmployeeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工Mapper接口
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    /**
     * 分页查询员工列表(带部门名称)
     */
    @Select("<script>" +
            "SELECT e.*, " +
            "       d.name AS department_name, " +
            "       m.name AS manager_name " +
            "FROM sys_employee e " +
            "LEFT JOIN sys_department d ON e.department_id = d.id AND d.is_deleted = 0 " +
            "LEFT JOIN sys_employee m ON e.manager_id = m.id AND m.is_deleted = 0 " +
            "WHERE e.is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (e.name LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR e.id LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR e.phone LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "  AND e.status = #{status} " +
            "</if>" +
            "<if test='departmentIds != null and departmentIds != \"\"'>" +
            "  AND FIND_IN_SET(e.department_id, #{departmentIds}) " +
            "</if>" +
            "<if test='position != null and position != \"\"'>" +
            "  AND e.position = #{position} " +
            "</if>" +
            "<if test='probationStatus != null and probationStatus != \"\"'>" +
            "  AND e.probation_status = #{probationStatus} " +
            "</if>" +
            "<if test='gender != null and gender != \"\"'>" +
            "  AND e.gender = #{gender} " +
            "</if>" +
            "<if test='joinDateStart != null and joinDateStart != \"\"'>" +
            "  AND e.join_date &gt;= #{joinDateStart} " +
            "</if>" +
            "<if test='joinDateEnd != null and joinDateEnd != \"\"'>" +
            "  AND e.join_date &lt;= #{joinDateEnd} " +
            "</if>" +
            "ORDER BY e.created_at DESC" +
            "</script>")
    IPage<EmployeeVO> selectPageWithDetails(
            Page<EmployeeVO> page,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("departmentIds") String departmentIds,
            @Param("position") String position,
            @Param("probationStatus") String probationStatus,
            @Param("gender") String gender,
            @Param("joinDateStart") String joinDateStart,
            @Param("joinDateEnd") String joinDateEnd
    );

    /**
     * 根据入职日期范围查询员工数量
     * 用于生成员工编号
     */
    @Select("SELECT COUNT(*) FROM sys_employee " +
            "WHERE id LIKE CONCAT(#{dateStr}, '%') " +
            "AND is_deleted = 0")
    Integer countByJoinDate(@Param("dateStr") String dateStr);

    /**
     * 检查邮箱是否存在
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_employee " +
            "WHERE email = #{email} " +
            "AND is_deleted = 0 " +
            "<if test='excludeId != null'>" +
            "AND id != #{excludeId} " +
            "</if>" +
            "</script>")
    Integer countByEmail(@Param("email") String email,
                         @Param("excludeId") String excludeId);

    /**
     * 检查手机号是否存在
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_employee " +
            "WHERE phone = #{phone} " +
            "AND is_deleted = 0 " +
            "<if test='excludeId != null'>" +
            "AND id != #{excludeId} " +
            "</if>" +
            "</script>")
    Integer countByPhone(@Param("phone") String phone,
                         @Param("excludeId") String excludeId);

    /**
     * 检查部门是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_department " +
            "WHERE id = #{departmentId} " +
            "AND is_deleted = 0")
    Integer countByDepartmentId(@Param("departmentId") String departmentId);

    /**
     * 检查上级是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_employee " +
            "WHERE id = #{managerId} " +
            "AND is_deleted = 0 " +
            "AND status = 'active'")
    Integer countByManagerId(@Param("managerId") String managerId);

    /**
     * 查询所有在职员工
     */
    @Select("SELECT * FROM sys_employee " +
            "WHERE status = 'active' " +
            "AND is_deleted = 0")
    List<Employee> selectAllActive();

    /**
     * 查询今天生日的在职员工
     */
    @Select("SELECT * FROM sys_employee " +
            "WHERE MONTH(birth_date) = MONTH(CURDATE()) " +
            "AND DAY(birth_date) = DAY(CURDATE()) " +
            "AND status = 'active' " +
            "AND is_deleted = 0")
    List<Employee> selectTodayBirthday();

    /**
     * 查询指定天数内试用期到期的员工
     */
    @Select("SELECT * FROM sys_employee " +
            "WHERE probation_status = 'probation' " +
            "AND probation_end_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL #{days} DAY) " +
            "AND status = 'active' " +
            "AND is_deleted = 0")
    List<Employee> selectProbationExpiring(@Param("days") Integer days);

    /**
     * 统计各部门员工数量
     */
    @Select("SELECT e.department_id, d.name AS department_name, COUNT(*) AS count " +
            "FROM sys_employee e " +
            "LEFT JOIN sys_department d ON e.department_id = d.id AND d.is_deleted = 0 " +
            "WHERE e.status = 'active' " +
            "AND e.is_deleted = 0 " +
            "GROUP BY e.department_id, d.name")
    List<Employee> countByDepartment();
}
