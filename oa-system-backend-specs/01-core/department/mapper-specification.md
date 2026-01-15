# 部门管理 Mapper 层规范

> **模块**: department
> **版本**: v1.0.0
> **更新日期**: 2026-01-15

---

## 🗄️ Mapper 层架构

### Mapper 接口列表

```
DepartmentMapper (部门Mapper)
    ├── 基础CRUD操作
    ├── 树形结构查询
    └── 统计查询

DepartmentMemberMapper (部门成员Mapper)
    ├── 基础CRUD操作
    └── 关联查询
```

---

## 📦 核心Mapper类

### 1. DepartmentMapper

**文件路径**: `com/oa/system/module/department/mapper/DepartmentMapper.java`

```java
package com.oa.system.module.department.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.system.module.department.entity.Department;
import com.oa.system.module.department.vo.DepartmentStatisticsVO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 部门Mapper接口
 *
 * @author OA System
 * @since 2026-01-15
 */
@Repository
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 查询根部门列表
     *
     * @param status 状态(可选)
     * @return 根部门列表
     */
    @Select("<script>" +
            "SELECT * FROM sys_department " +
            "WHERE parent_id IS NULL " +
            "AND is_deleted = 0 " +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY sort ASC" +
            "</script>")
    List<Department> selectRootDepartments(@Param("status") String status);

    /**
     * 查询子部门列表
     *
     * @param parentId 父部门ID
     * @param status 状态(可选)
     * @return 子部门列表
     */
    @Select("<script>" +
            "SELECT * FROM sys_department " +
            "WHERE parent_id = #{parentId} " +
            "AND is_deleted = 0 " +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY sort ASC" +
            "</script>")
    List<Department> selectChildDepartments(@Param("parentId") String parentId,
                                           @Param("status") String status);

    /**
     * 查询部门树(带员工数量和负责人信息)
     *
     * @param status 状态(可选)
     * @return 部门树列表
     */
    @Select("<script>" +
            "SELECT " +
            "  d.*, " +
            "  (SELECT COUNT(*) FROM sys_employee e WHERE e.department_id = d.id AND e.is_deleted = 0) AS employee_count, " +
            "  (SELECT name FROM sys_employee WHERE id = d.leader_id) AS leader_name " +
            "FROM sys_department d " +
            "WHERE d.is_deleted = 0 " +
            "<if test='status != null'>" +
            "AND d.status = #{status} " +
            "</if>" +
            "ORDER BY d.level, d.sort" +
            "</script>")
    @Results(id = "departmentTreeResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "shortName", column = "short_name"),
            @Result(property = "parentId", column = "parent_id"),
            @Result(property = "leaderId", column = "leader_id"),
            @Result(property = "level", column = "level"),
            @Result(property = "sort", column = "sort"),
            @Result(property = "employeeCount", column = "employee_count"),
            @Result(property = "leaderName", column = "leader_name"),
            @Result(property = "status", column = "status"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<Department> selectDepartmentTree(@Param("status") String status);

    /**
     * 查询部门详情(含关联信息)
     *
     * @param id 部门ID
     * @return 部门详情
     */
    @Select("SELECT " +
            "  d.*, " +
            "  p.name AS parent_name, " +
            "  e.name AS leader_name, " +
            "  e.position AS leader_position, " +
            "  e.phone AS leader_phone, " +
            "  e.email AS leader_email, " +
            "  (SELECT COUNT(*) FROM sys_employee emp WHERE emp.department_id = d.id AND emp.is_deleted = 0) AS employee_count " +
            "FROM sys_department d " +
            "LEFT JOIN sys_department p ON d.parent_id = p.id " +
            "LEFT JOIN sys_employee e ON d.leader_id = e.id " +
            "WHERE d.id = #{id} AND d.is_deleted = 0")
    @ResultMap("departmentDetailResultMap")
    Department selectDepartmentDetail(@Param("id") String id);

    /**
     * 查询部门路径(从根到当前部门)
     *
     * @param id 部门ID
     * @return 部门路径列表
     */
    @Select("WITH RECURSIVE dept_path AS (" +
            "  SELECT id, name, parent_id, 1 AS depth " +
            "  FROM sys_department " +
            "  WHERE id = #{id} AND is_deleted = 0 " +
            "  UNION ALL " +
            "  SELECT d.id, d.name, d.parent_id, dp.depth + 1 " +
            "  FROM sys_department d " +
            "  INNER JOIN dept_path dp ON d.id = dp.parent_id " +
            "  WHERE d.is_deleted = 0 " +
            ") " +
            "SELECT * FROM dept_path ORDER BY depth DESC")
    List<Department> selectDepartmentPath(@Param("id") String id);

    /**
     * 查询所有子孙部门ID
     *
     * @param id 部门ID
     * @return 子孙部门ID列表
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
     *
     * @return 统计信息
     */
    @Select("SELECT " +
            "  COUNT(*) AS total_count, " +
            "  SUM(CASE WHEN level = 1 THEN 1 ELSE 0 END) AS level1_count, " +
            "  SUM(CASE WHEN level = 2 THEN 1 ELSE 0 END) AS level2_count, " +
            "  SUM(CASE WHEN level = 3 THEN 1 ELSE 0 END) AS level3_count, " +
            "  SUM(CASE WHEN level = 4 THEN 1 ELSE 0 END) AS level4_count, " +
            "  MAX(level) AS max_level, " +
            "  SUM(CASE WHEN leader_id IS NOT NULL THEN 1 ELSE 0 END) AS with_leader_count, " +
            "  (SELECT COUNT(DISTINCT department_id) FROM sys_employee WHERE is_deleted = 0) AS total_employees " +
            "FROM sys_department " +
            "WHERE is_deleted = 0")
    @Result(property = "totalCount", column = "total_count"),
    @Result(property = "level1Count", column = "level1_count"),
    @Result(property = "level2Count", column = "level2_count"),
    @Result(property = "level3Count", column = "level3_count"),
    @Result(property = "level4Count", column = "level4_count"),
    @Result(property = "maxLevel", column = "max_level"),
    @Result(property = "withLeaderCount", column = "with_leader_count"),
    @Result(property = "totalEmployees", column = "total_employees")
    DepartmentStatisticsVO selectStatistics();

    /**
     * 检查部门名称是否存在(同级)
     *
     * @param name 部门名称
     * @param parentId 父部门ID
     * @param excludeId 排除的部门ID(更新时使用)
     * @return 存在的记录数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_department " +
            "WHERE name = #{name} " +
            "AND is_deleted = 0 " +
            "AND (parent_id IS NULL AND #{parentId} IS NULL OR parent_id = #{parentId}) " +
            "<if test='excludeId != null'>" +
            "AND id != #{excludeId} " +
            "</if>" +
            "</script>")
    Long countByNameInSameLevel(@Param("name") String name,
                                @Param("parentId") String parentId,
                                @Param("excludeId") String excludeId);

    /**
     * 检查是否有子部门
     *
     * @param id 部门ID
     * @return 子部门数量
     */
    @Select("SELECT COUNT(*) FROM sys_department " +
            "WHERE parent_id = #{id} AND is_deleted = 0")
    Long countChildren(@Param("id") String id);

    /**
     * 批量更新部门层级
     *
     * @param ids 部门ID列表
     * @param levelOffset 层级偏移量
     * @return 更新的行数
     */
    @Update("<script>" +
            "UPDATE sys_department " +
            "SET level = level + #{levelOffset} " +
            "WHERE id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateLevel(@Param("ids") List<String> ids,
                       @Param("levelOffset") Integer levelOffset);

    /**
     * 搜索部门(关键词匹配)
     *
     * @param keyword 关键词
     * @param status 状态(可选)
     * @return 部门列表
     */
    @Select("<script>" +
            "SELECT * FROM sys_department " +
            "WHERE is_deleted = 0 " +
            "AND (name LIKE CONCAT('%', #{keyword}, '%') " +
            "  OR short_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY level, sort" +
            "</script>")
    List<Department> searchDepartments(@Param("keyword") String keyword,
                                      @Param("status") String status);
}
```

---

### 2. DepartmentMemberMapper

**文件路径**: `com/oa/system/module/department/mapper/DepartmentMemberMapper.java`

```java
package com.oa.system.module.department.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.system.module.department.entity.DepartmentMember;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 部门成员Mapper接口
 *
 * @author OA System
 * @since 2026-01-15
 */
@Repository
@Mapper
public interface DepartmentMemberMapper extends BaseMapper<DepartmentMember> {

    /**
     * 查询部门成员列表
     *
     * @param departmentId 部门ID
     * @return 成员列表
     */
    @Select("SELECT " +
            "  dm.*, " +
            "  e.name AS employee_name, " +
            "  e.position AS employee_position, " +
            "  e.avatar AS employee_avatar, " +
            "  e.status AS employee_status " +
            "FROM sys_department_member dm " +
            "INNER JOIN sys_employee e ON dm.employee_id = e.id " +
            "WHERE dm.department_id = #{departmentId} " +
            "  AND dm.leave_date IS NULL " +
            "  AND e.is_deleted = 0 " +
            "ORDER BY dm.is_leader DESC, e.join_date")
    List<DepartmentMember> selectDepartmentMembers(@Param("departmentId") String departmentId);

    /**
     * 查询员工当前部门
     *
     * @param employeeId 员工ID
     * @return 部门成员关系
     */
    @Select("SELECT * FROM sys_department_member " +
            "WHERE employee_id = #{employeeId} " +
            "  AND leave_date IS NULL " +
            "LIMIT 1")
    DepartmentMember selectCurrentDepartment(@Param("employeeId") String employeeId);

    /**
     * 查询员工部门历史
     *
     * @param employeeId 员工ID
     * @return 部门历史列表
     */
    @Select("SELECT " +
            "  dm.*, " +
            "  d.name AS department_name " +
            "FROM sys_department_member dm " +
            "INNER JOIN sys_department d ON dm.department_id = d.id " +
            "WHERE dm.employee_id = #{employeeId} " +
            "ORDER BY dm.join_date DESC")
    List<DepartmentMember> selectEmployeeDepartmentHistory(@Param("employeeId") String employeeId);

    /**
     * 添加部门成员
     *
     * @param departmentId 部门ID
     * @param employeeId 员工ID
     * @param isLeader 是否为负责人
     * @param joinDate 加入日期
     * @return 插入的记录数
     */
    @Insert("INSERT INTO sys_department_member " +
            "(department_id, employee_id, is_leader, join_date, created_at, updated_at) " +
            "VALUES (#{departmentId}, #{employeeId}, #{isLeader}, #{joinDate}, NOW(), NOW())")
    int insertDepartmentMember(@Param("departmentId") String departmentId,
                              @Param("employeeId") String employeeId,
                              @Param("isLeader") Integer isLeader,
                              @Param("joinDate") LocalDate joinDate);

    /**
     * 移除部门成员(设置离开日期)
     *
     * @param departmentId 部门ID
     * @param employeeId 员工ID
     * @param leaveDate 离开日期
     * @return 更新的记录数
     */
    @Update("UPDATE sys_department_member " +
            "SET leave_date = #{leaveDate}, updated_at = NOW() " +
            "WHERE department_id = #{departmentId} " +
            "  AND employee_id = #{employeeId} " +
            "  AND leave_date IS NULL")
    int removeDepartmentMember(@Param("departmentId") String departmentId,
                              @Param("employeeId") String employeeId,
                              @Param("leaveDate") LocalDate leaveDate);

    /**
     * 转移部门成员
     *
     * @param oldDepartmentId 旧部门ID
     * @param newDepartmentId 新部门ID
     * @param employeeId 员工ID
     * @param transferDate 转移日期
     * @return 影响的记录数
     */
    @Update("UPDATE sys_department_member " +
            "SET leave_date = #{transferDate}, updated_at = NOW() " +
            "WHERE department_id = #{oldDepartmentId} " +
            "  AND employee_id = #{employeeId} " +
            "  AND leave_date IS NULL; " +
            "INSERT INTO sys_department_member " +
            "(department_id, employee_id, is_leader, join_date, created_at, updated_at) " +
            "VALUES (#{newDepartmentId}, #{employeeId}, 0, #{transferDate}, NOW(), NOW())")
    int transferDepartmentMember(@Param("oldDepartmentId") String oldDepartmentId,
                                @Param("newDepartmentId") String newDepartmentId,
                                @Param("employeeId") String employeeId,
                                @Param("transferDate") LocalDate transferDate);

    /**
     * 更新部门负责人
     *
     * @param departmentId 部门ID
     * @param oldLeaderId 旧负责人ID
     * @param newLeaderId 新负责人ID
     * @return 更新的记录数
     */
    @Update("UPDATE sys_department_member SET is_leader = 0, updated_at = NOW() " +
            "WHERE department_id = #{departmentId} AND employee_id = #{oldLeaderId}; " +
            "UPDATE sys_department_member SET is_leader = 1, updated_at = NOW() " +
            "WHERE department_id = #{departmentId} AND employee_id = #{newLeaderId}")
    int updateDepartmentLeader(@Param("departmentId") String departmentId,
                              @Param("oldLeaderId") String oldLeaderId,
                              @Param("newLeaderId") String newLeaderId);

    /**
     * 统计部门成员数量
     *
     * @param departmentId 部门ID
     * @return 成员数量
     */
    @Select("SELECT COUNT(*) FROM sys_department_member " +
            "WHERE department_id = #{departmentId} AND leave_date IS NULL")
    Long countDepartmentMembers(@Param("departmentId") String departmentId);

    /**
     * 查询部门负责人
     *
     * @param departmentId 部门ID
     * @return 负责人信息
     */
    @Select("SELECT " +
            "  dm.*, " +
            "  e.name AS employee_name, " +
            "  e.position AS employee_position, " +
            "  e.phone AS employee_phone, " +
            "  e.email AS employee_email " +
            "FROM sys_department_member dm " +
            "INNER JOIN sys_employee e ON dm.employee_id = e.id " +
            "WHERE dm.department_id = #{departmentId} " +
            "  AND dm.is_leader = 1 " +
            "  AND dm.leave_date IS NULL " +
            "LIMIT 1")
    DepartmentMember selectDepartmentLeader(@Param("departmentId") String departmentId);
}
```

---

## 📄 XML Mapper 文件

### DepartmentMapper.xml

**文件路径**: `resources/mapper/department/DepartmentMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.oa.system.module.department.mapper.DepartmentMapper">

    <!-- 结果映射 -->
    <resultMap id="BaseResultMap" type="com.oa.system.module.department.entity.Department">
        <id column="id" property="id"/>
        <result column="name" property="name"/>
        <result column="short_name" property="shortName"/>
        <result column="parent_id" property="parentId"/>
        <result column="leader_id" property="leaderId"/>
        <result column="level" property="level"/>
        <result column="sort" property="sort"/>
        <result column="established_date" property="establishedDate"/>
        <result column="description" property="description"/>
        <result column="icon" property="icon"/>
        <result column="status" property="status"/>
        <result column="created_at" property="createdAt"/>
        <result column="created_by" property="createdBy"/>
        <result column="updated_at" property="updatedAt"/>
        <result column="updated_by" property="updatedBy"/>
        <result column="is_deleted" property="isDeleted"/>
        <result column="deleted_at" property="deletedAt"/>
        <result column="deleted_by" property="deletedBy"/>
        <result column="version" property="version"/>
    </resultMap>

    <!-- 部门详情结果映射 -->
    <resultMap id="departmentDetailResultMap" type="com.oa.system.module.department.entity.Department"
               extends="BaseResultMap">
        <result column="parent_name" property="parentName"/>
        <result column="leader_name" property="leaderName"/>
        <result column="leader_position" property="leaderPosition"/>
        <result column="leader_phone" property="leaderPhone"/>
        <result column="leader_email" property="leaderEmail"/>
        <result column="employee_count" property="employeeCount"/>
    </resultMap>

    <!-- 查询部门列表(分页) -->
    <select id="selectDepartmentList" resultMap="departmentDetailResultMap">
        SELECT
            d.*,
            p.name AS parent_name,
            e.name AS leader_name,
            (SELECT COUNT(*) FROM sys_employee emp
             WHERE emp.department_id = d.id AND emp.is_deleted = 0) AS employee_count
        FROM sys_department d
        LEFT JOIN sys_department p ON d.parent_id = p.id
        LEFT JOIN sys_employee e ON d.leader_id = e.id
        WHERE d.is_deleted = 0
        <if test="keyword != null and keyword != ''">
            AND (d.name LIKE CONCAT('%', #{keyword}, '%')
            OR d.short_name LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <if test="status != null and status != ''">
            AND d.status = #{status}
        </if>
        <if test="leaderId != null and leaderId != ''">
            AND d.leader_id = #{leaderId}
        </if>
        <if test="level != null">
            AND d.level = #{level}
        </if>
        ORDER BY d.level, d.sort
    </select>

    <!-- 查询部门树 -->
    <select id="selectDepartmentTreeXML" resultMap="BaseResultMap">
        WITH RECURSIVE dept_tree AS (
            -- 根节点
            SELECT
                d.*,
                (SELECT COUNT(*) FROM sys_employee e
                 WHERE e.department_id = d.id AND e.is_deleted = 0) AS employee_count,
                (SELECT name FROM sys_employee WHERE id = d.leader_id) AS leader_name,
                1 AS depth
            FROM sys_department d
            WHERE d.parent_id IS NULL
              AND d.is_deleted = 0
              <if test="status != null and status != ''">
              AND d.status = #{status}
              </if>

            UNION ALL

            -- 递归子节点
            SELECT
                d.*,
                (SELECT COUNT(*) FROM sys_employee e
                 WHERE e.department_id = d.id AND e.is_deleted = 0) AS employee_count,
                (SELECT name FROM sys_employee WHERE id = d.leader_id) AS leader_name,
                dt.depth + 1 AS depth
            FROM sys_department d
            INNER JOIN dept_tree dt ON d.parent_id = dt.id
            WHERE d.is_deleted = 0
              <if test="status != null and status != ''">
              AND d.status = #{status}
              </if>
        )
        SELECT * FROM dept_tree
        ORDER BY level, sort
    </select>

    <!-- 移动部门(更新层级) -->
    <update id="moveDepartment">
        WITH RECURSIVE dept_tree AS (
            SELECT id, parent_id, 0 AS level_offset
            FROM sys_department
            WHERE id = #{departmentId}

            UNION ALL

            SELECT d.id, d.parent_id, dt.level_offset + 1
            FROM sys_department d
            INNER JOIN dept_tree dt ON d.parent_id = dt.id
            WHERE d.is_deleted = 0
        )
        UPDATE sys_department s
        INNER JOIN (
            SELECT
                t.id,
                (#{newLevel} - #{currentLevel} + t.level_offset) AS new_level
            FROM dept_tree t
        ) calc ON s.id = calc.id
        SET s.level = calc.new_level
        WHERE s.id IN (
            SELECT id FROM dept_tree
        )
    </update>

</mapper>
```

---

### DepartmentMemberMapper.xml

**文件路径**: `resources/mapper/department/DepartmentMemberMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.oa.system.module.department.mapper.DepartmentMemberMapper">

    <!-- 结果映射 -->
    <resultMap id="BaseResultMap" type="com.oa.system.module.department.entity.DepartmentMember">
        <id column="id" property="id"/>
        <result column="department_id" property="departmentId"/>
        <result column="employee_id" property="employeeId"/>
        <result column="is_leader" property="isLeader"/>
        <result column="join_date" property="joinDate"/>
        <result column="leave_date" property="leaveDate"/>
        <result column="created_at" property="createdAt"/>
        <result column="updated_at" property="updatedAt"/>
    </resultMap>

    <!-- 部门成员结果映射 -->
    <resultMap id="departmentMemberResultMap" type="com.oa.system.module.department.entity.DepartmentMember"
               extends="BaseResultMap">
        <result column="employee_name" property="employeeName"/>
        <result column="employee_position" property="employeePosition"/>
        <result column="employee_avatar" property="employeeAvatar"/>
        <result column="employee_status" property="employeeStatus"/>
    </resultMap>

    <!-- 查询部门成员列表 -->
    <select id="selectDepartmentMembersXML" resultMap="departmentMemberResultMap">
        SELECT
            dm.*,
            e.name AS employee_name,
            e.position AS employee_position,
            e.avatar AS employee_avatar,
            e.status AS employee_status
        FROM sys_department_member dm
        INNER JOIN sys_employee e ON dm.employee_id = e.id
        WHERE dm.department_id = #{departmentId}
          AND dm.leave_date IS NULL
          AND e.is_deleted = 0
        ORDER BY dm.is_leader DESC, e.join_date
    </select>

</mapper>
```

---

## 🔧 MyBatis 配置

### application.yml

```yaml
# MyBatis配置
mybatis-plus:
  # Mapper文件位置
  mapper-locations: classpath*:mapper/**/*Mapper.xml

  # 实体类包路径
  type-aliases-package: com.oa.system.module.**.entity

  # 配置
  configuration:
    # 驼峰命名转换
    map-underscore-to-camel-case: true
    # 日志实现
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
    # 缓存开关
    cache-enabled: true
    # 延迟加载开关
    lazy-loading-enabled: true
    # 积极延迟加载
    aggressive-lazy-loading: false

  # 全局配置
  global-config:
    # 数据库配置
    db-config:
      # 主键类型
      id-type: input
      # 逻辑删除字段
      logic-delete-field: isDeleted
      # 逻辑删除值
      logic-delete-value: 1
      # 逻辑未删除值
      logic-not-delete-value: 0
```

---

## 📊 性能优化建议

### 1. 索引优化

```sql
-- 确保以下索引存在
CREATE INDEX idx_department_parent ON sys_department(parent_id);
CREATE INDEX idx_department_leader ON sys_department(leader_id);
CREATE INDEX idx_department_level ON sys_department(level);
CREATE INDEX idx_department_status ON sys_department(status);
CREATE INDEX idx_department_sort ON sys_department(parent_id, sort);
```

### 2. 查询优化

- 使用递归CTE查询树形结构
- 使用JOIN减少查询次数
- 使用分页避免大数据量查询
- 使用缓存减少重复查询

### 3. 批量操作

- 批量插入使用`INSERT INTO ... VALUES (...), (...), ...`
- 批量更新使用`CASE WHEN`或临时表
- 使用事务保证数据一致性

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-15
