package com.example.oa_system_backend.module.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.employee.entity.Employee;
import com.example.oa_system_backend.module.employee.vo.EmployeeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工Mapper接口
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    /**
     * 分页查询员工列表(带部门名称)
     */
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
    Integer countByJoinDate(@Param("dateStr") String dateStr);

    /**
     * 检查邮箱是否存在
     */
    Integer countByEmail(@Param("email") String email,
                         @Param("excludeId") String excludeId);

    /**
     * 检查手机号是否存在
     */
    Integer countByPhone(@Param("phone") String phone,
                         @Param("excludeId") String excludeId);

    /**
     * 检查部门是否存在
     */
    Integer countByDepartmentId(@Param("departmentId") String departmentId);

    /**
     * 检查上级是否存在
     */
    Integer countByManagerId(@Param("managerId") String managerId);

    /**
     * 查询所有在职员工
     */
    List<Employee> selectAllActive();

    /**
     * 查询今天生日的在职员工
     */
    List<Employee> selectTodayBirthday();

    /**
     * 查询指定天数内试用期到期的员工
     */
    List<Employee> selectProbationExpiring(@Param("days") Integer days);

    /**
     * 统计各部门员工数量
     */
    List<Employee> countByDepartment();
}
