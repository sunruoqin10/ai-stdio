# 请假管理模块 - VO规范

> **模块**: leave
> **版本**: v1.0.0
> **更新日期**: 2026-01-18

---

## 📦 VO列表

| VO | 说明 |
|----|------|
| LeaveRequestVO | 请假申请VO |
| LeaveDetailVO | 请假申请详情VO |
| ApprovalRecordVO | 审批记录VO |
| LeaveBalanceVO | 年假余额VO |
| LeaveStatisticsVO | 请假统计VO |
| HolidayVO | 节假日VO |
| EmployeeInfoVO | 员工信息VO |
| DepartmentInfoVO | 部门信息VO |

---

## 1. LeaveRequestVO (请假申请VO)

**文件路径**: `com/oa/system/module/leave/vo/LeaveRequestVO.java`

```java
package com.oa.system.module.leave.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 请假申请VO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class LeaveRequestVO {

    private String id;

    private String applicantId;

    private String applicantName;

    private String applicantPosition;

    private String applicantAvatar;

    private String departmentId;

    private String departmentName;

    private String type;

    private String typeName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal duration;

    private String reason;

    private String[] attachments;

    private String status;

    private String statusName;

    private Integer currentApprovalLevel;

    private Integer totalApprovalLevels;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 申请ID |
| applicantId | String | 申请人ID |
| applicantName | String | 申请人姓名 |
| applicantPosition | String | 申请人职位 |
| applicantAvatar | String | 申请人头像 |
| departmentId | String | 部门ID |
| departmentName | String | 部门名称 |
| type | String | 请假类型编码 |
| typeName | String | 请假类型名称 |
| startTime | LocalDateTime | 开始时间 |
| endTime | LocalDateTime | 结束时间 |
| duration | BigDecimal | 请假时长(天) |
| reason | String | 请假事由 |
| attachments | String[] | 附件URL数组 |
| status | String | 状态编码 |
| statusName | String | 状态名称 |
| currentApprovalLevel | Integer | 当前审批层级 |
| totalApprovalLevels | Integer | 总审批层级数 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

---

## 2. LeaveDetailVO (请假申请详情VO)

**文件路径**: `com/oa/system/module/leave/vo/LeaveDetailVO.java`

```java
package com.oa.system.module.leave.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 请假申请详情VO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class LeaveDetailVO {

    private String id;

    private String applicantId;

    private String applicantName;

    private String applicantPosition;

    private String applicantPhone;

    private String applicantEmail;

    private String applicantAvatar;

    private String departmentId;

    private String departmentName;

    private String managerId;

    private String managerName;

    private String type;

    private String typeName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal duration;

    private String reason;

    private String[] attachments;

    private String status;

    private String statusName;

    private Integer currentApprovalLevel;

    private Integer totalApprovalLevels;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<ApprovalRecordVO> approvals;

    private String rejectReason;

    private LocalDateTime rejectTime;
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 申请ID |
| applicantId | String | 申请人ID |
| applicantName | String | 申请人姓名 |
| applicantPosition | String | 申请人职位 |
| applicantPhone | String | 申请人电话 |
| applicantEmail | String | 申请人邮箱 |
| applicantAvatar | String | 申请人头像 |
| departmentId | String | 部门ID |
| departmentName | String | 部门名称 |
| managerId | String | 直属上级ID |
| managerName | String | 直属上级姓名 |
| type | String | 请假类型编码 |
| typeName | String | 请假类型名称 |
| startTime | LocalDateTime | 开始时间 |
| endTime | LocalDateTime | 结束时间 |
| duration | BigDecimal | 请假时长(天) |
| reason | String | 请假事由 |
| attachments | String[] | 附件URL数组 |
| status | String | 状态编码 |
| statusName | String | 状态名称 |
| currentApprovalLevel | Integer | 当前审批层级 |
| totalApprovalLevels | Integer | 总审批层级数 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |
| approvals | List<ApprovalRecordVO> | 审批记录列表 |
| rejectReason | String | 驳回原因 |
| rejectTime | LocalDateTime | 驳回时间 |

---

## 3. ApprovalRecordVO (审批记录VO)

**文件路径**: `com/oa/system/module/leave/vo/ApprovalRecordVO.java`

```java
package com.oa.system.module.leave.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批记录VO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class ApprovalRecordVO {

    private Long id;

    private String requestId;

    private String approverId;

    private String approverName;

    private String approverPosition;

    private String approverAvatar;

    private String approverDepartment;

    private Integer approvalLevel;

    private String approvalLevelName;

    private String status;

    private String statusName;

    private String opinion;

    private LocalDateTime timestamp;

    private Boolean isCurrent;
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 记录ID |
| requestId | String | 申请ID |
| approverId | String | 审批人ID |
| approverName | String | 审批人姓名 |
| approverPosition | String | 审批人职位 |
| approverAvatar | String | 审批人头像 |
| approverDepartment | String | 审批人部门 |
| approvalLevel | Integer | 审批层级 |
| approvalLevelName | String | 审批层级名称 |
| status | String | 审批状态编码 |
| statusName | String | 审批状态名称 |
| opinion | String | 审批意见 |
| timestamp | LocalDateTime | 审批时间 |
| isCurrent | Boolean | 是否为当前审批节点 |

---

## 4. LeaveBalanceVO (年假余额VO)

**文件路径**: `com/oa/system/module/leave/vo/LeaveBalanceVO.java`

```java
package com.oa.system.module.leave.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 年假余额VO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class LeaveBalanceVO {

    private Long id;

    private String employeeId;

    private String employeeName;

    private String employeeAvatar;

    private String departmentId;

    private String departmentName;

    private Integer year;

    private BigDecimal annualTotal;

    private BigDecimal annualUsed;

    private BigDecimal annualRemaining;

    private BigDecimal usagePercentage;

    private String warningLevel;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| employeeId | String | 员工ID |
| employeeName | String | 员工姓名 |
| employeeAvatar | String | 员工头像 |
| departmentId | String | 部门ID |
| departmentName | String | 部门名称 |
| year | Integer | 年份 |
| annualTotal | BigDecimal | 年假总额(天) |
| annualUsed | BigDecimal | 已使用(天) |
| annualRemaining | BigDecimal | 剩余(天) |
| usagePercentage | BigDecimal | 使用百分比 |
| warningLevel | String | 警告级别: none/warning/critical |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### 警告级别说明

| 级别 | 条件 | 说明 |
|------|------|------|
| none | annualRemaining >= 3 | 正常 |
| warning | 1 <= annualRemaining < 3 | 警告 |
| critical | annualRemaining < 1 | 严重 |

---

## 5. LeaveStatisticsVO (请假统计VO)

**文件路径**: `com/oa/system/module/leave/vo/LeaveStatisticsVO.java`

```java
package com.oa.system.module.leave.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 请假统计VO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class LeaveStatisticsVO {

    private Integer totalRequests;

    private BigDecimal totalDuration;

    private Map<String, Integer> byType;

    private Map<String, Integer> byStatus;

    private List<MonthlyData> monthlyData;

    private List<DepartmentData> departmentData;

    private List<EmployeeData> employeeData;

    @Data
    public static class MonthlyData {
        private String month;
        private Integer count;
        private BigDecimal duration;
    }

    @Data
    public static class DepartmentData {
        private String departmentId;
        private String departmentName;
        private Integer count;
        private BigDecimal duration;
    }

    @Data
    public static class EmployeeData {
        private String employeeId;
        private String employeeName;
        private Integer count;
        private BigDecimal duration;
    }
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| totalRequests | Integer | 总申请次数 |
| totalDuration | BigDecimal | 总请假天数 |
| byType | Map<String, Integer> | 按类型统计 |
| byStatus | Map<String, Integer> | 按状态统计 |
| monthlyData | List<MonthlyData> | 月度数据 |
| departmentData | List<DepartmentData> | 部门数据 |
| employeeData | List<EmployeeData> | 员工数据 |

---

## 6. HolidayVO (节假日VO)

**文件路径**: `com/oa/system/module/leave/vo/HolidayVO.java`

```java
package com.oa.system.module.leave.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 节假日VO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class HolidayVO {

    private Long id;

    private LocalDate date;

    private String name;

    private String type;

    private String typeName;

    private Integer year;

    private Integer isWorkday;

    private String isWorkdayName;
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| date | LocalDate | 日期 |
| name | String | 节假日名称 |
| type | String | 类型编码 |
| typeName | String | 类型名称 |
| year | Integer | 年份 |
| isWorkday | Integer | 是否为工作日 |
| isWorkdayName | String | 工作日名称 |

---

## 7. EmployeeInfoVO (员工信息VO)

**文件路径**: `com/oa/system/module/leave/vo/EmployeeInfoVO.java`

```java
package com.oa.system.module.leave.vo;

import lombok.Data;

/**
 * 员工信息VO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class EmployeeInfoVO {

    private String id;

    private String name;

    private String avatar;

    private String position;

    private String phone;

    private String email;

    private String departmentId;

    private String departmentName;

    private String status;
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 员工ID |
| name | String | 员工姓名 |
| avatar | String | 员工头像 |
| position | String | 职位 |
| phone | String | 电话 |
| email | String | 邮箱 |
| departmentId | String | 部门ID |
| departmentName | String | 部门名称 |
| status | String | 状态 |

---

## 8. DepartmentInfoVO (部门信息VO)

**文件路径**: `com/oa/system/module/leave/vo/DepartmentInfoVO.java`

```java
package com.oa.system.module.leave.vo;

import lombok.Data;

/**
 * 部门信息VO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class DepartmentInfoVO {

    private String id;

    private String name;

    private String shortName;

    private String parentId;

    private String parentName;

    private String leaderId;

    private String leaderName;

    private Integer level;

    private Integer employeeCount;

    private String status;
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 部门ID |
| name | String | 部门名称 |
| shortName | String | 部门简称 |
| parentId | String | 父部门ID |
| parentName | String | 父部门名称 |
| leaderId | String | 负责人ID |
| leaderName | String | 负责人姓名 |
| level | Integer | 层级 |
| employeeCount | Integer | 员工数量 |
| status | String | 状态 |

---

## 🔧 VO转换工具

### LeaveVOConverter

**文件路径**: `com/oa/system/module/leave/converter/LeaveVOConverter.java`

```java
package com.oa.system.module.leave.converter;

import com.oa.system.module.leave.entity.LeaveRequest;
import com.oa.system.module.leave.enums.LeaveStatus;
import com.oa.system.module.leave.enums.LeaveType;
import com.oa.system.module.leave.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 请假VO转换器
 *
 * @author OA System
 * @since 2026-01-18
 */
@Mapper(componentModel = "spring")
public interface LeaveVOConverter {

    LeaveRequestVO toVO(LeaveRequest entity);

    @Mapping(target = "typeName", source = "type", qualifiedByName = "typeToName")
    @Mapping(target = "statusName", source = "status", qualifiedByName = "statusToName")
    @Mapping(target = "totalApprovalLevels", source = "duration", qualifiedByName = "calculateApprovalLevels")
    LeaveDetailVO toDetailVO(LeaveRequest entity);

    LeaveBalanceVO toBalanceVO(LeaveBalance entity);

    @Mapping(target = "usagePercentage", source = "entity", qualifiedByName = "calculateUsagePercentage")
    @Mapping(target = "warningLevel", source = "entity", qualifiedByName = "calculateWarningLevel")
    LeaveBalanceVO toBalanceVOWithDetails(LeaveBalance entity);

    HolidayVO toHolidayVO(Holiday entity);

    @Named("typeToName")
    default String typeToName(String type) {
        return LeaveType.fromCode(type).getName();
    }

    @Named("statusToName")
    default String statusToName(String status) {
        return LeaveStatus.fromCode(status).getName();
    }

    @Named("calculateApprovalLevels")
    default Integer calculateApprovalLevels(BigDecimal duration) {
        if (duration == null) {
            return 1;
        }
        if (duration.compareTo(new BigDecimal("3")) <= 0) {
            return 1;
        } else if (duration.compareTo(new BigDecimal("7")) <= 0) {
            return 2;
        } else {
            return 3;
        }
    }

    @Named("calculateUsagePercentage")
    default BigDecimal calculateUsagePercentage(LeaveBalance entity) {
        if (entity.getAnnualTotal().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return entity.getAnnualUsed()
                .divide(entity.getAnnualTotal(), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    @Named("calculateWarningLevel")
    default String calculateWarningLevel(LeaveBalance entity) {
        BigDecimal remaining = entity.getAnnualRemaining();
        if (remaining.compareTo(new BigDecimal("3")) >= 0) {
            return "none";
        } else if (remaining.compareTo(new BigDecimal("1")) >= 0) {
            return "warning";
        } else {
            return "critical";
        }
    }
}
```

---

## 📝 VO使用示例

### 请假申请列表响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "LEAVE20260118001",
        "applicantId": "EMP000001",
        "applicantName": "张三",
        "applicantPosition": "软件工程师",
        "departmentId": "DEPT001",
        "departmentName": "技术部",
        "type": "annual",
        "typeName": "年假",
        "startTime": "2026-01-20T09:00:00",
        "endTime": "2026-01-22T18:00:00",
        "duration": 3.0,
        "reason": "个人事务",
        "status": "pending",
        "statusName": "待审批",
        "currentApprovalLevel": 1,
        "totalApprovalLevels": 1,
        "createdAt": "2026-01-18T10:00:00",
        "updatedAt": "2026-01-18T10:00:00"
      }
    ],
    "total": 1,
    "page": 1,
    "pageSize": 10,
    "totalPages": 1
  },
  "timestamp": 1642579200000
}
```

### 请假申请详情响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "LEAVE20260118001",
    "applicantId": "EMP000001",
    "applicantName": "张三",
    "applicantPosition": "软件工程师",
    "applicantPhone": "13800138000",
    "applicantEmail": "zhangsan@example.com",
    "departmentId": "DEPT001",
    "departmentName": "技术部",
    "type": "annual",
    "typeName": "年假",
    "startTime": "2026-01-20T09:00:00",
    "endTime": "2026-01-22T18:00:00",
    "duration": 3.0,
    "reason": "个人事务",
    "status": "pending",
    "statusName": "待审批",
    "currentApprovalLevel": 1,
    "totalApprovalLevels": 1,
    "createdAt": "2026-01-18T10:00:00",
    "updatedAt": "2026-01-18T10:00:00",
    "approvals": [
      {
        "id": 1,
        "requestId": "LEAVE20260118001",
        "approverId": "EMP000005",
        "approverName": "孙经理",
        "approverPosition": "部门经理",
        "approvalLevel": 1,
        "approvalLevelName": "一级审批",
        "status": "pending",
        "statusName": "待审批",
        "isCurrent": true
      }
    ]
  },
  "timestamp": 1642579200000
}
```

### 年假余额响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "employeeId": "EMP000001",
    "employeeName": "张三",
    "departmentId": "DEPT001",
    "departmentName": "技术部",
    "year": 2026,
    "annualTotal": 10.0,
    "annualUsed": 2.0,
    "annualRemaining": 8.0,
    "usagePercentage": 20.0,
    "warningLevel": "none",
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-01-18T10:00:00"
  },
  "timestamp": 1642579200000
}
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-18
