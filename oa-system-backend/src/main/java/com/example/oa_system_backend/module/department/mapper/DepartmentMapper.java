package com.example.oa_system_backend.module.department.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.department.dto.DepartmentQueryRequest;
import com.example.oa_system_backend.module.department.entity.Department;
import com.example.oa_system_backend.module.department.vo.DepartmentStatisticsVO;
import com.example.oa_system_backend.module.department.vo.DepartmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部门Mapper接口
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 分页查询部门列表
     */
    @Select("<script>" +
            "SELECT " +
            "  d.*, " +
            "  p.name AS parent_name, " +
            "  e.name AS leader_name " +
            "FROM sys_department d " +
            "LEFT JOIN sys_department p ON d.parent_id = p.id " +
            "LEFT JOIN sys_employee e ON d.leader_id = e.id " +
            "WHERE d.is_deleted = 0 " +
            "<if test='request.keyword != null and request.keyword != \"\"'>" +
            "  AND (d.name LIKE CONCAT('%', #{request.keyword}, '%') " +
            "       OR d.short_name LIKE CONCAT('%', #{request.keyword}, '%')) " +
            "</if>" +
            "<if test='request.status != null and request.status != \"\"'>" +
            "  AND d.status = #{request.status} " +
            "</if>" +
            "<if test='request.leaderId != null and request.leaderId != \"\"'>" +
            "  AND d.leader_id = #{request.leaderId} " +
            "</if>" +
            "<if test='request.level != null'>" +
            "  AND d.level = #{request.level} " +
            "</if>" +
            "ORDER BY d.level, d.sort" +
            "</script>")
    IPage<DepartmentVO> selectPageByQuery(
            Page<DepartmentVO> page,
            @Param("request") DepartmentQueryRequest request
    );

    /**
     * 查询部门树(带员工数量和负责人信息)
     */
    @Select("<script>" +
            "SELECT " +
            "  d.*, " +
            "  (SELECT name FROM sys_employee WHERE id = d.leader_id) AS leader_name, " +
            "  (SELECT COUNT(*) FROM sys_employee WHERE department_id = d.id AND is_deleted = 0) AS employee_count " +
            "FROM sys_department d " +
            "WHERE d.is_deleted = 0 " +
            "<if test='status != null and status != \"\"'>" +
            "AND d.status = #{status} " +
            "</if>" +
            "ORDER BY d.level, d.sort" +
            "</script>")
    List<DepartmentVO> selectDepartmentTree(@Param("status") String status);

    /**
     * 查询根部门列表
     */
    @Select("<script>" +
            "SELECT * FROM sys_department " +
            "WHERE parent_id IS NULL " +
            "AND is_deleted = 0 " +
            "<if test='status != null and status != \"\"'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY sort ASC" +
            "</script>")
    List<DepartmentVO> selectRootDepartments(@Param("status") String status);

    /**
     * 查询子部门列表
     */
    @Select("<script>" +
            "SELECT * FROM sys_department " +
            "WHERE parent_id = #{parentId} " +
            "AND is_deleted = 0 " +
            "<if test='status != null and status != \"\"'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY sort ASC" +
            "</script>")
    List<DepartmentVO> selectChildDepartments(@Param("parentId") String parentId,
                                             @Param("status") String status);

    /**
     * 查询部门详情(含关联信息)
     */
    @Select("SELECT " +
            "  d.*, " +
            "  p.name AS parent_name, " +
            "  e.name AS leader_name, " +
            "  e.position AS leader_position, " +
            "  e.phone AS leader_phone, " +
            "  e.email AS leader_email, " +
            "  (SELECT COUNT(*) FROM sys_employee emp WHERE emp.department_id = d.id AND emp.is_deleted = 0) AS employee_count, " +
            "  (SELECT COUNT(*) FROM sys_department child WHERE child.parent_id = d.id AND child.is_deleted = 0) AS child_count " +
            "FROM sys_department d " +
            "LEFT JOIN sys_department p ON d.parent_id = p.id " +
            "LEFT JOIN sys_employee e ON d.leader_id = e.id " +
            "WHERE d.id = #{id} AND d.is_deleted = 0")
    Department selectDepartmentDetail(@Param("id") String id);

    /**
     * 查询所有子孙部门ID
     */
    @Select("WITH RECURSIVE dept_tree AS (" +
            "  SELECT id, parent_id " +
            "  FROM sys_department " +
            "  WHERE id = #{id} AND is_deleted = 0 " +
            "  UNION ALL " +
            "  SELECT d.id, d.parent_id " +
            "  FROM sys_department d " +
            "  INNER JOIN dept_tree dt ON d.parent_id = dt.id " +
            "  WHERE d.is_deleted = 0 " +
            ") " +
            "SELECT id FROM dept_tree WHERE id != #{id}")
    List<String> selectDescendantIds(@Param("id") String id);

    /**
     * 查询部门统计信息
     */
    @Select("SELECT " +
            "  COUNT(*) AS total_count, " +
            "  SUM(CASE WHEN level = 1 THEN 1 ELSE 0 END) AS level1_count, " +
            "  SUM(CASE WHEN level = 2 THEN 1 ELSE 0 END) AS level2_count, " +
            "  SUM(CASE WHEN level = 3 THEN 1 ELSE 0 END) AS level3_count, " +
            "  SUM(CASE WHEN level = 4 THEN 1 ELSE 0 END) AS level4_count, " +
            "  MAX(level) AS max_level, " +
            "  SUM(CASE WHEN leader_id IS NOT NULL THEN 1 ELSE 0 END) AS with_leader_count, " +
            "  (SELECT COUNT(DISTINCT department_id) FROM sys_employee WHERE is_deleted = 0) AS total_employees, " +
            "  SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) AS active_department_count, " +
            "  SUM(CASE WHEN status = 'disabled' THEN 1 ELSE 0 END) AS disabled_department_count " +
            "FROM sys_department " +
            "WHERE is_deleted = 0")
    DepartmentStatisticsVO selectStatistics();

    /**
     * 检查部门名称是否存在(同级)
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_department " +
            "WHERE name = #{name} " +
            "AND is_deleted = 0 " +
            "AND (parent_id IS NULL AND #{parentId} IS NULL OR parent_id = #{parentId}) " +
            "<if test='excludeId != null and excludeId != \"\"'>" +
            "AND id != #{excludeId} " +
            "</if>" +
            "</script>")
    Long countByNameInSameLevel(@Param("name") String name,
                                @Param("parentId") String parentId,
                                @Param("excludeId") String excludeId);

    /**
     * 检查是否有子部门
     */
    @Select("SELECT COUNT(*) FROM sys_department " +
            "WHERE parent_id = #{id} AND is_deleted = 0")
    Long countChildren(@Param("id") String id);

    /**
     * 检查是否有员工
     */
    @Select("SELECT COUNT(*) FROM sys_employee " +
            "WHERE department_id = #{id} AND is_deleted = 0")
    Long countEmployees(@Param("id") String id);

    /**
     * 查询部门成员列表
     */
    @Select("SELECT " +
            "  e.id AS employee_id, " +
            "  e.name AS employee_name, " +
            "  e.avatar AS employee_avatar, " +
            "  e.position, " +
            "  e.status, " +
            "  e.join_date AS join_department_date, " +
            "  CASE WHEN d.leader_id = e.id THEN 1 ELSE 0 END AS is_leader " +
            "FROM sys_employee e " +
            "LEFT JOIN sys_department d ON e.department_id = d.id " +
            "WHERE e.department_id = #{departmentId} " +
            "AND e.is_deleted = 0 " +
            "ORDER BY is_leader DESC, e.join_date ASC")
    List<com.example.oa_system_backend.module.department.vo.DepartmentMemberVO> selectDepartmentMembers(@Param("departmentId") String departmentId);

    /**
     * 查询部门总数（用于生成部门ID）
     */
    @Select("SELECT COUNT(*) FROM sys_department WHERE is_deleted = 0")
    Long countTotal();
}
