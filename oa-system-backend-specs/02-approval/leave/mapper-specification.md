# 请假管理模块 - Mapper层规范

> **模块**: leave
> **版本**: v1.0.0
> **更新日期**: 2026-01-18

---

## 🗄️ Mapper层架构

### Mapper接口列表

```
LeaveRequestMapper (请假申请Mapper)
LeaveApprovalMapper (审批记录Mapper)
LeaveBalanceMapper (年假余额Mapper)
LeaveUsageLogMapper (年假使用记录Mapper)
HolidayMapper (节假日Mapper)
```

---

## 📦 核心Mapper类

### 1. LeaveRequestMapper

**文件路径**: `com/oa/system/module/leave/mapper/LeaveRequestMapper.java`

```java
package com.oa.system.module.leave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.system.module.leave.entity.LeaveRequest;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 请假申请Mapper接口
 *
 * @author OA System
 * @since 2026-01-18
 */
@Repository
@Mapper
public interface LeaveRequestMapper extends BaseMapper<LeaveRequest> {

    @Select("<script>" +
            "SELECT * FROM approval_leave_request " +
            "WHERE is_deleted = 0 " +
            "<if test='applicantId != null and applicantId != \"\"'>" +
            "AND applicant_id = #{applicantId} " +
            "</if>" +
            "<if test='departmentId != null and departmentId != \"\"'>" +
            "AND department_id = #{departmentId} " +
            "</if>" +
            "<if test='type != null and type != \"\"'>" +
            "AND type = #{type} " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND status = #{status} " +
            "</if>" +
            "<if test='startTimeStart != null'>" +
            "AND start_time >= #{startTimeStart} " +
            "</if>" +
            "<if test='startTimeEnd != null'>" +
            "AND start_time <= #{startTimeEnd} " +
            "</if>" +
            "<if test='endTimeStart != null'>" +
            "AND end_time >= #{endTimeStart} " +
            "</if>" +
            "<if test='endTimeEnd != null'>" +
            "AND end_time <= #{endTimeEnd} " +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND reason LIKE CONCAT('%', #{keyword}, '%') " +
            "</if>" +
            "ORDER BY ${sortBy} ${sortOrder}" +
            "</script>")
    IPage<LeaveRequest> selectPageByCondition(
            Page<LeaveRequest> page,
            @Param("applicantId") String applicantId,
            @Param("departmentId") String departmentId,
            @Param("type") String type,
            @Param("status") String status,
            @Param("startTimeStart") LocalDateTime startTimeStart,
            @Param("startTimeEnd") LocalDateTime startTimeEnd,
            @Param("endTimeStart") LocalDateTime endTimeStart,
            @Param("endTimeEnd") LocalDateTime endTimeEnd,
            @Param("keyword") String keyword,
            @Param("sortBy") String sortBy,
            @Param("sortOrder") String sortOrder
    );

    @Select("<script>" +
            "SELECT COUNT(*) FROM approval_leave_request " +
            "WHERE applicant_id = #{applicantId} " +
            "AND status IN ('pending', 'approving', 'approved') " +
            "AND is_deleted = 0 " +
            "AND (" +
            "  (start_time <= #{startTime} AND end_time > #{startTime}) " +
            "  OR (start_time < #{endTime} AND end_time >= #{endTime}) " +
            "  OR (start_time >= #{startTime} AND end_time <= #{endTime})" +
            ") " +
            "<if test='excludeId != null and excludeId != \"\"'>" +
            "AND id != #{excludeId} " +
            "</if>" +
            "</script>")
    Long countTimeConflict(
            @Param("applicantId") String applicantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") String excludeId
    );

    @Select("SELECT " +
            "  type, " +
            "  COUNT(*) AS count, " +
            "  SUM(duration) AS total_duration " +
            "FROM approval_leave_request " +
            "WHERE applicant_id = #{applicantId} " +
            "  AND status = 'approved' " +
            "  AND YEAR(start_time) = #{year} " +
            "  AND is_deleted = 0 " +
            "GROUP BY type")
    List<LeaveStatisticsByType> selectStatisticsByType(
            @Param("applicantId") String applicantId,
            @Param("year") Integer year
    );

    @Select("SELECT " +
            "  status, " +
            "  COUNT(*) AS count " +
            "FROM approval_leave_request " +
            "WHERE applicant_id = #{applicantId} " +
            "  AND YEAR(start_time) = #{year} " +
            "  AND is_deleted = 0 " +
            "GROUP BY status")
    List<LeaveStatisticsByStatus> selectStatisticsByStatus(
            @Param("applicantId") String applicantId,
            @Param("year") Integer year
    );

    @Select("SELECT " +
            "  DATE_FORMAT(start_time, '%Y-%m') AS month, " +
            "  COUNT(*) AS count, " +
            "  SUM(duration) AS duration " +
            "FROM approval_leave_request " +
            "WHERE applicant_id = #{applicantId} " +
            "  AND status = 'approved' " +
            "  AND YEAR(start_time) = #{year} " +
            "  AND is_deleted = 0 " +
            "GROUP BY DATE_FORMAT(start_time, '%Y-%m') " +
            "ORDER BY month")
    List<LeaveStatisticsByMonth> selectStatisticsByMonth(
            @Param("applicantId") String applicantId,
            @Param("year") Integer year
    );
}
```

---

### 2. LeaveApprovalMapper

**文件路径**: `com/oa/system/module/leave/mapper/LeaveApprovalMapper.java`

```java
package com.oa.system.module.leave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.system.module.leave.entity.LeaveApproval;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 审批记录Mapper接口
 *
 * @author OA System
 * @since 2026-01-18
 */
@Repository
@Mapper
public interface LeaveApprovalMapper extends BaseMapper<LeaveApproval> {

    @Select("SELECT * FROM approval_leave_approval " +
            "WHERE request_id = #{requestId} " +
            "ORDER BY approval_level ASC")
    List<LeaveApproval> selectByRequestId(@Param("requestId") String requestId);

    @Select("SELECT * FROM approval_leave_approval " +
            "WHERE approver_id = #{approverId} " +
            "  AND status = 'pending' " +
            "ORDER BY created_at ASC")
    List<LeaveApproval> selectPendingByApproverId(@Param("approverId") String approverId);

    @Select("SELECT COUNT(*) FROM approval_leave_approval " +
            "WHERE request_id = #{requestId} " +
            "  AND approval_level = #{approvalLevel}")
    Long countByRequestIdAndLevel(
            @Param("requestId") String requestId,
            @Param("approvalLevel") Integer approvalLevel
    );

    @Select("SELECT COUNT(*) FROM approval_leave_approval " +
            "WHERE request_id = #{requestId} " +
            "  AND status = 'approved'")
    Long countApprovedByRequestId(@Param("requestId") String requestId);

    @Select("SELECT COUNT(*) FROM approval_leave_approval " +
            "WHERE request_id = #{requestId} " +
            "  AND status = 'rejected'")
    Long countRejectedByRequestId(@Param("requestId") String requestId);
}
```

---

### 3. LeaveBalanceMapper

**文件路径**: `com/oa/system/module/leave/mapper/LeaveBalanceMapper.java`

```java
package com.oa.system.module.leave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.system.module.leave.entity.LeaveBalance;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 年假余额Mapper接口
 *
 * @author OA System
 * @since 2026-01-18
 */
@Repository
@Mapper
public interface LeaveBalanceMapper extends BaseMapper<LeaveBalance> {

    @Select("SELECT * FROM approval_leave_balance " +
            "WHERE employee_id = #{employeeId} " +
            "  AND year = #{year}")
    LeaveBalance selectByEmployeeIdAndYear(
            @Param("employeeId") String employeeId,
            @Param("year") Integer year
    );

    @Select("<script>" +
            "SELECT * FROM approval_leave_balance " +
            "WHERE 1=1 " +
            "<if test='employeeId != null and employeeId != \"\"'>" +
            "AND employee_id = #{employeeId} " +
            "</if>" +
            "<if test='year != null'>" +
            "AND year = #{year} " +
            "</if>" +
            "ORDER BY year DESC, employee_id" +
            "</script>")
    List<LeaveBalance> selectByCondition(
            @Param("employeeId") String employeeId,
            @Param("year") Integer year
    );

    @Select("SELECT " +
            "  COUNT(*) AS total, " +
            "  SUM(annual_total) AS total_quota, " +
            "  SUM(annual_used) AS total_used, " +
            "  SUM(annual_remaining) AS total_remaining " +
            "FROM approval_leave_balance " +
            "WHERE year = #{year}")
    LeaveBalanceSummary selectSummaryByYear(@Param("year") Integer year);

    @Select("SELECT " +
            "  b.*, " +
            "  e.name AS employee_name, " +
            "  d.name AS department_name " +
            "FROM approval_leave_balance b " +
            "LEFT JOIN sys_employee e ON b.employee_id = e.id " +
            "LEFT JOIN sys_department d ON e.department_id = d.id " +
            "WHERE b.year = #{year} " +
            "ORDER BY b.annual_remaining ASC")
    List<LeaveBalanceWithInfo> selectWithInfoByYear(@Param("year") Integer year);
}
```

---

### 4. LeaveUsageLogMapper

**文件路径**: `com/oa/system/module/leave/mapper/LeaveUsageLogMapper.java`

```java
package com.oa.system.module.leave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.system.module.leave.entity.LeaveUsageLog;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 年假使用记录Mapper接口
 *
 * @author OA System
 * @since 2026-01-18
 */
@Repository
@Mapper
public interface LeaveUsageLogMapper extends BaseMapper<LeaveUsageLog> {

    @Select("SELECT * FROM approval_leave_usage_log " +
            "WHERE employee_id = #{employeeId} " +
            "  AND year = #{year} " +
            "ORDER BY created_at DESC")
    List<LeaveUsageLog> selectByEmployeeIdAndYear(
            @Param("employeeId") String employeeId,
            @Param("year") Integer year
    );

    @Select("SELECT * FROM approval_leave_usage_log " +
            "WHERE request_id = #{requestId} " +
            "ORDER BY created_at DESC")
    List<LeaveUsageLog> selectByRequestId(@Param("requestId") String requestId);

    @Select("SELECT " +
            "  change_type, " +
            "  COUNT(*) AS count, " +
            "  SUM(duration) AS total_duration " +
            "FROM approval_leave_usage_log " +
            "WHERE employee_id = #{employeeId} " +
            "  AND year = #{year} " +
            "GROUP BY change_type")
    List<LeaveUsageSummary> selectSummaryByEmployeeIdAndYear(
            @Param("employeeId") String employeeId,
            @Param("year") Integer year
    );
}
```

---

### 5. HolidayMapper

**文件路径**: `com/oa/system/module/leave/mapper/HolidayMapper.java`

```java
package com.oa.system.module.leave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.system.module.leave.entity.Holiday;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 节假日Mapper接口
 *
 * @author OA System
 * @since 2026-01-18
 */
@Repository
@Mapper
public interface HolidayMapper extends BaseMapper<Holiday> {

    @Select("<script>" +
            "SELECT * FROM approval_holiday " +
            "WHERE 1=1 " +
            "<if test='year != null'>" +
            "AND year = #{year} " +
            "</if>" +
            "<if test='type != null and type != \"\"'>" +
            "AND type = #{type} " +
            "</if>" +
            "<if test='startDate != null'>" +
            "AND date >= #{startDate} " +
            "</if>" +
            "<if test='endDate != null'>" +
            "AND date <= #{endDate} " +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND name LIKE CONCAT('%', #{keyword}, '%') " +
            "</if>" +
            "ORDER BY date" +
            "</script>")
    List<Holiday> selectByCondition(
            @Param("year") Integer year,
            @Param("type") String type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("keyword") String keyword
    );

    @Select("SELECT * FROM approval_holiday " +
            "WHERE date = #{date}")
    Holiday selectByDate(@Param("date") LocalDate date);

    @Select("SELECT * FROM approval_holiday " +
            "WHERE year = #{year} " +
            "ORDER BY date")
    List<Holiday> selectByYear(@Param("year") Integer year);

    @Select("SELECT COUNT(*) FROM approval_holiday " +
            "WHERE date >= #{startDate} AND date <= #{endDate}")
    Long countByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
```

---

## 📄 XML Mapper文件

### LeaveRequestMapper.xml

**文件路径**: `resources/mapper/leave/LeaveRequestMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.oa.system.module.leave.mapper.LeaveRequestMapper">

    <resultMap id="BaseResultMap" type="com.oa.system.module.leave.entity.LeaveRequest">
        <id column="id" property="id"/>
        <result column="applicant_id" property="applicantId"/>
        <result column="department_id" property="departmentId"/>
        <result column="type" property="type"/>
        <result column="start_time" property="startTime"/>
        <result column="end_time" property="endTime"/>
        <result column="duration" property="duration"/>
        <result column="reason" property="reason"/>
        <result column="attachments" property="attachments"
                typeHandler="com.oa.system.common.handler.JsonTypeHandler"/>
        <result column="status" property="status"/>
        <result column="current_approval_level" property="currentApprovalLevel"/>
        <result column="created_at" property="createdAt"/>
        <result column="updated_at" property="updatedAt"/>
        <result column="is_deleted" property="isDeleted"/>
        <result column="deleted_at" property="deletedAt"/>
        <result column="deleted_by" property="deletedBy"/>
        <result column="version" property="version"/>
    </resultMap>

    <resultMap id="LeaveRequestWithInfoMap" type="com.oa.system.module.leave.entity.LeaveRequest"
               extends="BaseResultMap">
        <result column="applicant_name" property="applicantName"/>
        <result column="applicant_position" property="applicantPosition"/>
        <result column="applicant_avatar" property="applicantAvatar"/>
        <result column="department_name" property="departmentName"/>
    </resultMap>

    <select id="selectWithInfo" resultMap="LeaveRequestWithInfoMap">
        SELECT
            r.*,
            e.name AS applicant_name,
            e.position AS applicant_position,
            e.avatar AS applicant_avatar,
            d.name AS department_name
        FROM approval_leave_request r
        LEFT JOIN sys_employee e ON r.applicant_id = e.id
        LEFT JOIN sys_department d ON r.department_id = d.id
        WHERE r.is_deleted = 0
        <if test="applicantId != null and applicantId != ''">
            AND r.applicant_id = #{applicantId}
        </if>
        <if test="departmentId != null and departmentId != ''">
            AND r.department_id = #{departmentId}
        </if>
        <if test="type != null and type != ''">
            AND r.type = #{type}
        </if>
        <if test="status != null and status != ''">
            AND r.status = #{status}
        </if>
        ORDER BY r.created_at DESC
    </select>

</mapper>
```

---

### LeaveApprovalMapper.xml

**文件路径**: `resources/mapper/leave/LeaveApprovalMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.oa.system.module.leave.mapper.LeaveApprovalMapper">

    <resultMap id="BaseResultMap" type="com.oa.system.module.leave.entity.LeaveApproval">
        <id column="id" property="id"/>
        <result column="request_id" property="requestId"/>
        <result column="approver_id" property="approverId"/>
        <result column="approver_name" property="approverName"/>
        <result column="approval_level" property="approvalLevel"/>
        <result column="status" property="status"/>
        <result column="opinion" property="opinion"/>
        <result column="timestamp" property="timestamp"/>
    </resultMap>

    <resultMap id="ApprovalWithInfoMap" type="com.oa.system.module.leave.entity.LeaveApproval"
               extends="BaseResultMap">
        <result column="approver_position" property="approverPosition"/>
        <result column="approver_avatar" property="approverAvatar"/>
        <result column="approver_department" property="approverDepartment"/>
    </resultMap>

    <select id="selectWithInfo" resultMap="ApprovalWithInfoMap">
        SELECT
            a.*,
            e.position AS approver_position,
            e.avatar AS approver_avatar,
            d.name AS approver_department
        FROM approval_leave_approval a
        LEFT JOIN sys_employee e ON a.approver_id = e.id
        LEFT JOIN sys_department d ON e.department_id = d.id
        WHERE a.request_id = #{requestId}
        ORDER BY a.approval_level ASC
    </select>

</mapper>
```

---

## 🔧 MyBatis配置

### application.yml

```yaml
mybatis-plus:
  mapper-locations: classpath*:mapper/leave/**/*Mapper.xml
  type-aliases-package: com.oa.system.module.leave.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
    cache-enabled: true
    lazy-loading-enabled: true
    aggressive-lazy-loading: false
  global-config:
    db-config:
      id-type: input
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

---

## 📊 性能优化建议

### 1. 索引优化

```sql
-- 请假申请表索引
CREATE INDEX idx_leave_applicant ON approval_leave_request(applicant_id);
CREATE INDEX idx_leave_department ON approval_leave_request(department_id);
CREATE INDEX idx_leave_status ON approval_leave_request(status);
CREATE INDEX idx_leave_type ON approval_leave_request(type);
CREATE INDEX idx_leave_start_time ON approval_leave_request(start_time);
CREATE INDEX idx_leave_end_time ON approval_leave_request(end_time);
CREATE INDEX idx_leave_created_at ON approval_leave_request(created_at);

-- 审批记录表索引
CREATE INDEX idx_approval_request ON approval_leave_approval(request_id);
CREATE INDEX idx_approval_approver ON approval_leave_approval(approver_id);
CREATE INDEX idx_approval_status ON approval_leave_approval(status);
CREATE INDEX idx_approval_timestamp ON approval_leave_approval(timestamp);

-- 年假余额表索引
CREATE INDEX idx_balance_employee_year ON approval_leave_balance(employee_id, year);
CREATE INDEX idx_balance_year ON approval_leave_balance(year);

-- 年假使用记录表索引
CREATE INDEX idx_usage_employee ON approval_leave_usage_log(employee_id);
CREATE INDEX idx_usage_request ON approval_leave_usage_log(request_id);
CREATE INDEX idx_usage_created_at ON approval_leave_usage_log(created_at);

-- 节假日表索引
CREATE INDEX idx_holiday_year ON approval_holiday(year);
CREATE INDEX idx_holiday_date ON approval_holiday(date);
```

### 2. 查询优化

- 使用分页查询避免大数据量
- 使用索引字段进行查询
- 使用JOIN减少查询次数
- 使用缓存减少重复查询

### 3. 批量操作

- 批量插入使用`INSERT INTO ... VALUES (...), (...), ...`
- 批量更新使用`CASE WHEN`或临时表
- 使用事务保证数据一致性

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-18
