# 请假管理模块 - 实体类规范

> **模块**: leave
> **版本**: v1.0.0
> **更新日期**: 2026-01-18

---

## 📦 实体类列表

| 实体类 | 对应表 | 说明 |
|--------|--------|------|
| LeaveRequest | approval_leave_request | 请假申请实体 |
| LeaveApproval | approval_leave_approval | 审批记录实体 |
| LeaveBalance | approval_leave_balance | 年假余额实体 |
| LeaveUsageLog | approval_leave_usage_log | 年假使用记录实体 |
| Holiday | approval_holiday | 节假日实体 |

---

## 1. LeaveRequest (请假申请实体)

**文件路径**: `com/oa/system/module/leave/entity/LeaveRequest.java`

```java
package com.oa.system.module.leave.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 请假申请实体
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
@TableName("approval_leave_request")
public class LeaveRequest {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("applicant_id")
    private String applicantId;

    @TableField("department_id")
    private String departmentId;

    @TableField("type")
    private String type;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("duration")
    private BigDecimal duration;

    @TableField("reason")
    private String reason;

    @TableField(value = "attachments", typeHandler = com.oa.system.common.handler.JsonTypeHandler.class)
    private String[] attachments;

    @TableField("status")
    private String status;

    @TableField("current_approval_level")
    private Integer currentApprovalLevel;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    @TableLogic
    private Integer isDeleted;

    @TableField(value = "deleted_at")
    private LocalDateTime deletedAt;

    @TableField(value = "deleted_by")
    private String deletedBy;

    @TableField("version")
    private Integer version;
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | 是 | 编号: LEAVE+YYYYMMDD+序号 |
| applicantId | String | 是 | 申请人ID |
| departmentId | String | 是 | 部门ID |
| type | String | 是 | 请假类型: annual/sick/personal/comp_time/marriage/maternity |
| startTime | LocalDateTime | 是 | 开始时间 |
| endTime | LocalDateTime | 是 | 结束时间 |
| duration | BigDecimal | 是 | 请假时长(天),支持0.5天 |
| reason | String | 是 | 请假事由 |
| attachments | String[] | 否 | 附件URL数组 |
| status | String | 是 | 状态: draft/pending/approving/approved/rejected/cancelled |
| currentApprovalLevel | Integer | 否 | 当前审批层级(0/1/2/3) |
| createdAt | LocalDateTime | 是 | 创建时间 |
| updatedAt | LocalDateTime | 是 | 更新时间 |
| isDeleted | Integer | 是 | 是否删除: 0-否 1-是 |
| deletedAt | LocalDateTime | 否 | 删除时间 |
| deletedBy | String | 否 | 删除人 |
| version | Integer | 是 | 乐观锁版本号 |

---

## 2. LeaveApproval (审批记录实体)

**文件路径**: `com/oa/system/module/leave/entity/LeaveApproval.java`

```java
package com.oa.system.module.leave.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批记录实体
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
@TableName("approval_leave_approval")
public class LeaveApproval {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("request_id")
    private String requestId;

    @TableField("approver_id")
    private String approverId;

    @TableField("approver_name")
    private String approverName;

    @TableField("approval_level")
    private Integer approvalLevel;

    @TableField("status")
    private String status;

    @TableField("opinion")
    private String opinion;

    @TableField("timestamp")
    private LocalDateTime timestamp;
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID(自增) |
| requestId | String | 是 | 申请ID |
| approverId | String | 是 | 审批人ID |
| approverName | String | 是 | 审批人姓名(快照) |
| approvalLevel | Integer | 是 | 审批层级(1/2/3) |
| status | String | 是 | 审批状态: pending/approved/rejected |
| opinion | String | 否 | 审批意见 |
| timestamp | LocalDateTime | 否 | 审批时间 |

---

## 3. LeaveBalance (年假余额实体)

**文件路径**: `com/oa/system/module/leave/entity/LeaveBalance.java`

```java
package com.oa.system.module.leave.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 年假余额实体
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
@TableName("approval_leave_balance")
public class LeaveBalance {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("employee_id")
    private String employeeId;

    @TableField("year")
    private Integer year;

    @TableField("annual_total")
    private BigDecimal annualTotal;

    @TableField("annual_used")
    private BigDecimal annualUsed;

    @TableField("annual_remaining")
    private BigDecimal annualRemaining;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 主键ID(自增) |
| employeeId | String | 是 | 员工ID |
| year | Integer | 是 | 年份 |
| annualTotal | BigDecimal | 是 | 年假总额(天) |
| annualUsed | BigDecimal | 是 | 已使用(天) |
| annualRemaining | BigDecimal | 是 | 剩余(天) |
| createdAt | LocalDateTime | 是 | 创建时间 |
| updatedAt | LocalDateTime | 是 | 更新时间 |

---

## 4. LeaveUsageLog (年假使用记录实体)

**文件路径**: `com/oa/system/module/leave/entity/LeaveUsageLog.java`

```java
package com.oa.system.module.leave.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 年假使用记录实体
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
@TableName("approval_leave_usage_log")
public class LeaveUsageLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("employee_id")
    private String employeeId;

    @TableField("request_id")
    private String requestId;

    @TableField("type")
    private String type;

    @TableField("duration")
    private BigDecimal duration;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("change_type")
    private String changeType;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID(自增) |
| employeeId | String | 是 | 员工ID |
| requestId | String | 是 | 申请ID |
| type | String | 是 | 请假类型 |
| duration | BigDecimal | 是 | 请假时长(天) |
| startTime | LocalDateTime | 是 | 开始时间 |
| endTime | LocalDateTime | 是 | 结束时间 |
| changeType | String | 是 | 变动类型: deduct/rollback |
| createdAt | LocalDateTime | 是 | 记录时间 |

---

## 5. Holiday (节假日实体)

**文件路径**: `com/oa/system/module/leave/entity/Holiday.java`

```java
package com.oa.system.module.leave.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 节假日实体
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
@TableName("approval_holiday")
public class Holiday {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("date")
    private LocalDate date;

    @TableField("name")
    private String name;

    @TableField("type")
    private String type;

    @TableField("year")
    private Integer year;

    @TableField("is_workday")
    private Integer isWorkday;
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 主键ID(自增) |
| date | LocalDate | 是 | 日期 |
| name | String | 是 | 节假日名称 |
| type | String | 是 | 类型: national/company |
| year | Integer | 是 | 年份 |
| isWorkday | Integer | 否 | 是否为工作日(调休): 0-否 1-是 |

---

## 🔧 枚举类

### LeaveType (请假类型枚举)

**文件路径**: `com/oa/system/module/leave/enums/LeaveType.java`

```java
package com.oa.system.module.leave.enums;

import lombok.Getter;

/**
 * 请假类型枚举
 *
 * @author OA System
 * @since 2026-01-18
 */
@Getter
public enum LeaveType {

    ANNUAL("annual", "年假"),
    SICK("sick", "病假"),
    PERSONAL("personal", "事假"),
    COMP_TIME("comp_time", "调休"),
    MARRIAGE("marriage", "婚假"),
    MATERNITY("maternity", "产假");

    private final String code;
    private final String name;

    LeaveType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static LeaveType fromCode(String code) {
        for (LeaveType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown leave type: " + code);
    }
}
```

### LeaveStatus (请假状态枚举)

**文件路径**: `com/oa/system/module/leave/enums/LeaveStatus.java`

```java
package com.oa.system.module.leave.enums;

import lombok.Getter;

/**
 * 请假状态枚举
 *
 * @author OA System
 * @since 2026-01-18
 */
@Getter
public enum LeaveStatus {

    DRAFT("draft", "草稿"),
    PENDING("pending", "待审批"),
    APPROVING("approving", "审批中"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已拒绝"),
    CANCELLED("cancelled", "已取消");

    private final String code;
    private final String name;

    LeaveStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static LeaveStatus fromCode(String code) {
        for (LeaveStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown leave status: " + code);
    }
}
```

### ApprovalStatus (审批状态枚举)

**文件路径**: `com/oa/system/module/leave/enums/ApprovalStatus.java`

```java
package com.oa.system.module.leave.enums;

import lombok.Getter;

/**
 * 审批状态枚举
 *
 * @author OA System
 * @since 2026-01-18
 */
@Getter
public enum ApprovalStatus {

    PENDING("pending", "待审批"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已拒绝");

    private final String code;
    private final String name;

    ApprovalStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ApprovalStatus fromCode(String code) {
        for (ApprovalStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown approval status: " + code);
    }
}
```

---

## 📝 自定义TypeHandler

### JsonTypeHandler (JSON数组类型处理器)

**文件路径**: `com/oa/system/common/handler/JsonTypeHandler.java`

```java
package com.oa.system.common.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JSON数组类型处理器
 *
 * @author OA System
 * @since 2026-01-18
 */
@MappedTypes({String[].class})
public class JsonTypeHandler extends BaseTypeHandler<String[]> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String[] parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting String[] to JSON", e);
        }
    }

    @Override
    public String[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public String[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public String[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    private String[] parseJson(String json) throws SQLException {
        if (json == null || json.isEmpty()) {
            return new String[0];
        }
        try {
            return objectMapper.readValue(json, new TypeReference<String[]>() {});
        } catch (JsonProcessingException e) {
            throw new SQLException("Error parsing JSON to String[]", e);
        }
    }
}
```

---

## 🔍 外键约束实现

### 外键约束说明

由于数据库层面的外键约束可能影响性能，建议在Service层实现外键约束逻辑：

```java
@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    public void validateForeignKeys(LeaveRequest request) {
        if (request.getApplicantId() != null) {
            Employee employee = employeeService.getById(request.getApplicantId());
            if (employee == null) {
                throw new BusinessException(3001, "申请人不存在");
            }
        }

        if (request.getDepartmentId() != null) {
            Department department = departmentService.getById(request.getDepartmentId());
            if (department == null) {
                throw new BusinessException(3002, "部门不存在");
            }
        }
    }
}
```

---

## ✅ 检查约束实现

### 检查约束说明

在Service层实现检查约束逻辑：

```java
@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    public void validateConstraints(LeaveRequest request) {
        if (request.getDuration() != null && request.getDuration().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(3003, "请假时长必须大于0");
        }

        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (!request.getEndTime().isAfter(request.getStartTime())) {
                throw new BusinessException(3004, "结束时间必须晚于开始时间");
            }
        }
    }
}
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-18
