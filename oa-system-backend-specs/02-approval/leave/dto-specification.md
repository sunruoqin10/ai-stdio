# 请假管理模块 - DTO规范

> **模块**: leave
> **版本**: v1.0.0
> **更新日期**: 2026-01-18

---

## 📦 DTO列表

| DTO | 说明 |
|-----|------|
| LeaveCreateDTO | 创建请假申请DTO |
| LeaveUpdateDTO | 更新请假申请DTO |
| LeaveQueryDTO | 查询请假申请DTO |
| LeaveSubmitDTO | 提交请假申请DTO |
| ApprovalDTO | 审批DTO |
| BalanceQueryDTO | 年假余额查询DTO |
| BalanceUpdateDTO | 年假余额更新DTO |
| HolidayQueryDTO | 节假日查询DTO |
| HolidayCreateDTO | 节假日创建DTO |

---

## 1. LeaveCreateDTO (创建请假申请DTO)

**文件路径**: `com/oa/system/module/leave/dto/request/LeaveCreateDTO.java`

```java
package com.oa.system.module.leave.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 创建请假申请DTO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class LeaveCreateDTO {

    @NotBlank(message = "请假类型不能为空")
    private String type;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    private String[] attachments;

    @NotBlank(message = "请假事由不能为空")
    private String reason;
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 是 | 请假类型: annual/sick/personal/comp_time/marriage/maternity |
| startTime | LocalDateTime | 是 | 开始时间 |
| endTime | LocalDateTime | 是 | 结束时间 |
| attachments | String[] | 否 | 附件URL数组 |
| reason | String | 是 | 请假事由 |

### 校验规则

- type: 不能为空，必须是有效的请假类型
- startTime: 不能为空，必须早于endTime
- endTime: 不能为空，必须晚于startTime
- reason: 不能为空，长度1-500字符

---

## 2. LeaveUpdateDTO (更新请假申请DTO)

**文件路径**: `com/oa/system/module/leave/dto/request/LeaveUpdateDTO.java`

```java
package com.oa.system.module.leave.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 更新请假申请DTO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class LeaveUpdateDTO {

    private String type;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String[] attachments;

    private String reason;

    @NotNull(message = "版本号不能为空")
    private Integer version;
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 否 | 请假类型 |
| startTime | LocalDateTime | 否 | 开始时间 |
| endTime | LocalDateTime | 否 | 结束时间 |
| attachments | String[] | 否 | 附件URL数组 |
| reason | String | 否 | 请假事由 |
| version | Integer | 是 | 乐观锁版本号 |

### 校验规则

- version: 不能为空，用于乐观锁控制
- 如果修改了时间，startTime必须早于endTime
- 只能编辑草稿状态的申请

---

## 3. LeaveQueryDTO (查询请假申请DTO)

**文件路径**: `com/oa/system/module/leave/dto/request/LeaveQueryDTO.java`

```java
package com.oa.system.module.leave.dto.request;

import lombok.Data;

/**
 * 查询请假申请DTO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class LeaveQueryDTO {

    private String applicantId;

    private String departmentId;

    private String type;

    private String status;

    private LocalDateTime startTimeStart;

    private LocalDateTime startTimeEnd;

    private LocalDateTime endTimeStart;

    private LocalDateTime endTimeEnd;

    private String keyword;

    private Integer page = 1;

    private Integer pageSize = 10;

    private String sortBy = "created_at";

    private String sortOrder = "DESC";
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| applicantId | String | 否 | 申请人ID |
| departmentId | String | 否 | 部门ID |
| type | String | 否 | 请假类型 |
| status | String | 否 | 状态 |
| startTimeStart | LocalDateTime | 否 | 开始时间范围-开始 |
| startTimeEnd | LocalDateTime | 否 | 开始时间范围-结束 |
| endTimeStart | LocalDateTime | 否 | 结束时间范围-开始 |
| endTimeEnd | LocalDateTime | 否 | 结束时间范围-结束 |
| keyword | String | 否 | 关键词(匹配申请人姓名、事由) |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |
| sortBy | String | 否 | 排序字段，默认created_at |
| sortOrder | String | 否 | 排序方向，默认DESC |

---

## 4. LeaveSubmitDTO (提交请假申请DTO)

**文件路径**: `com/oa/system/module/leave/dto/request/LeaveSubmitDTO.java`

```java
package com.oa.system.module.leave.dto.request;

import lombok.Data;

/**
 * 提交请假申请DTO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class LeaveSubmitDTO {

}
```

### 说明

提交请假申请不需要额外参数，只需在URL中指定申请ID。

---

## 5. ApprovalDTO (审批DTO)

**文件路径**: `com/oa/system/module/leave/dto/request/ApprovalDTO.java`

```java
package com.oa.system.module.leave.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 审批DTO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class ApprovalDTO {

    @NotBlank(message = "审批状态不能为空")
    private String status;

    private String opinion;
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 是 | 审批状态: approved/rejected |
| opinion | String | 否 | 审批意见，驳回时必填 |

### 校验规则

- status: 不能为空，必须是approved或rejected
- opinion: 当status为rejected时必填

---

## 6. BalanceQueryDTO (年假余额查询DTO)

**文件路径**: `com/oa/system/module/leave/dto/request/BalanceQueryDTO.java`

```java
package com.oa.system.module.leave.dto.request;

import lombok.Data;

/**
 * 年假余额查询DTO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class BalanceQueryDTO {

    private String employeeId;

    private Integer year;

    private String departmentId;

    private Integer page = 1;

    private Integer pageSize = 10;
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| employeeId | String | 否 | 员工ID |
| year | Integer | 否 | 年份，默认当前年份 |
| departmentId | String | 否 | 部门ID |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |

---

## 7. BalanceUpdateDTO (年假余额更新DTO)

**文件路径**: `com/oa/system/module/leave/dto/request/BalanceUpdateDTO.java`

```java
package com.oa.system.module.leave.dto.request;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 年假余额更新DTO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class BalanceUpdateDTO {

    @NotBlank(message = "员工ID不能为空")
    private String employeeId;

    @NotNull(message = "年份不能为空")
    private Integer year;

    @NotNull(message = "年假总额不能为空")
    @DecimalMin(value = "0", message = "年假总额不能小于0")
    private BigDecimal annualTotal;
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| employeeId | String | 是 | 员工ID |
| year | Integer | 是 | 年份 |
| annualTotal | BigDecimal | 是 | 年假总额 |

### 校验规则

- employeeId: 不能为空
- year: 不能为空
- annualTotal: 不能为空，不能小于0

---

## 8. HolidayQueryDTO (节假日查询DTO)

**文件路径**: `com/oa/system/module/leave/dto/request/HolidayQueryDTO.java`

```java
package com.oa.system.module.leave.dto.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * 节假日查询DTO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class HolidayQueryDTO {

    private Integer year;

    private String type;

    private LocalDate startDate;

    private LocalDate endDate;

    private String keyword;
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| year | Integer | 否 | 年份，默认当前年份 |
| type | String | 否 | 类型: national/company |
| startDate | LocalDate | 否 | 开始日期 |
| endDate | LocalDate | 否 | 结束日期 |
| keyword | String | 否 | 关键词(匹配节假日名称) |

---

## 9. HolidayCreateDTO (节假日创建DTO)

**文件路径**: `com/oa/system/module/leave/dto/request/HolidayCreateDTO.java`

```java
package com.oa.system.module.leave.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 节假日创建DTO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class HolidayCreateDTO {

    @NotNull(message = "日期不能为空")
    private LocalDate date;

    @NotBlank(message = "节假日名称不能为空")
    private String name;

    @NotBlank(message = "类型不能为空")
    private String type;

    @NotNull(message = "年份不能为空")
    private Integer year;

    private Integer isWorkday;
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| date | LocalDate | 是 | 日期 |
| name | String | 是 | 节假日名称 |
| type | String | 是 | 类型: national/company |
| year | Integer | 是 | 年份 |
| isWorkday | Integer | 否 | 是否为工作日(调休): 0-否 1-是 |

### 校验规则

- date: 不能为空
- name: 不能为空，长度1-100字符
- type: 不能为空，必须是national或company
- year: 不能为空
- isWorkday: 可选，默认0

---

## 🔧 分页响应DTO

### PageResult<T>

**文件路径**: `com/oa/system/common/dto/PageResult.java`

```java
package com.oa.system.common.dto;

import lombok.Data;

import java.util.List;

/**
 * 分页响应DTO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class PageResult<T> {

    private List<T> list;

    private Long total;

    private Integer page;

    private Integer pageSize;

    private Integer totalPages;

    public PageResult(List<T> list, Long total, Integer page, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }
}
```

---

## 🔧 统一响应DTO

### ApiResponse<T>

**文件路径**: `com/oa/system/common/dto/ApiResponse.java`

```java
package com.oa.system.common.dto;

import lombok.Data;

/**
 * 统一响应DTO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class ApiResponse<T> {

    private Integer code;

    private String message;

    private T data;

    private Long timestamp;

    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }
}
```

---

## 🔧 批量操作DTO

### BatchRequest<T>

**文件路径**: `com/oa/system/common/dto/BatchRequest.java`

```java
package com.oa.system.common.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量操作请求DTO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class BatchRequest<T> {

    @NotEmpty(message = "操作列表不能为空")
    @Valid
    private List<T> items;
}
```

### BatchResult

**文件路径**: `com/oa/system/common/dto/BatchResult.java`

```java
package com.oa.system.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量操作结果DTO
 *
 * @author OA System
 * @since 2026-01-18
 */
@Data
public class BatchResult {

    private Integer total;

    private Integer success;

    private Integer failed;

    private List<BatchError> errors;

    public BatchResult(Integer total) {
        this.total = total;
        this.success = 0;
        this.failed = 0;
        this.errors = new ArrayList<>();
    }
}

@Data
class BatchError {

    private String id;

    private String message;
}
```

---

## 📝 DTO使用示例

### 创建请假申请

```java
POST /api/leave/requests
Content-Type: application/json

{
  "type": "annual",
  "startTime": "2026-01-20T09:00:00",
  "endTime": "2026-01-22T18:00:00",
  "reason": "个人事务",
  "attachments": [
    "https://example.com/file1.pdf"
  ]
}
```

### 查询请假申请

```java
GET /api/leave/requests?applicantId=EMP000001&status=pending&page=1&pageSize=10
```

### 审批请假申请

```java
POST /api/leave/approvals/LEAVE20260118001/approve
Content-Type: application/json

{
  "status": "approved",
  "opinion": "同意"
}
```

### 更新年假额度

```java
PUT /api/leave/balance/quota
Content-Type: application/json

{
  "employeeId": "EMP000001",
  "year": 2026,
  "annualTotal": 10
}
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-18
