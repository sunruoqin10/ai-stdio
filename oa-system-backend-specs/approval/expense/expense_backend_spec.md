# 费用报销模块 - 后端技术实现规范

> **文档类型**: 后端技术实现规范 (Backend Technical Specification)
> **技术栈**: Spring Boot 3.x + MyBatis-Plus + MySQL 8.0
> **对应前端**: [expense_Technical.md](../../../oa-system-frontend-specs/approval/expense/expense_Technical.md)
> **对应数据库**: [expense.md](../../../oa-system-database-specs/02-approval/expense.md)
> **版本**: v1.0.0
> **创建日期**: 2026-01-21

---

## 1. 项目结构

### 1.1 模块目录结构

```
com.example.oa_system_backend.module.expense/
├── controller/              # 控制器层
│   └── ExpenseController.java
├── service/                 # 业务逻辑层
│   ├── ExpenseService.java
│   └── impl/
│       └── ExpenseServiceImpl.java
├── mapper/                  # 数据访问层
│   ├── ExpenseMapper.java
│   ├── ExpenseItemMapper.java
│   ├── ExpenseInvoiceMapper.java
│   └── ExpensePaymentMapper.java
├── entity/                  # 实体类
│   ├── Expense.java
│   ├── ExpenseItem.java
│   ├── ExpenseInvoice.java
│   └── ExpensePayment.java
├── dto/                     # 数据传输对象
│   ├── ExpenseCreateRequest.java
│   ├── ExpenseUpdateRequest.java
│   ├── ExpenseQueryRequest.java
│   ├── ApprovalRequest.java
│   └── InvoiceOCRRequest.java
├── vo/                      # 视图对象
│   ├── ExpenseVO.java
│   ├── ExpenseDetailVO.java
│   └── ExpenseStatisticsVO.java
├── enums/                   # 枚举类
│   ├── ExpenseType.java
│   ├── ExpenseStatus.java
│   ├── InvoiceType.java
│   └── PaymentMethod.java
└── util/                    # 工具类
    ├── ExpenseIdGenerator.java
    └── InvoiceValidator.java
```

---

## 2. 实体类设计

### 2.1 Expense.java - 报销单实体

```java
package com.example.oa_system_backend.module.expense.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 费用报销单实体
 */
@Data
@TableName("approval_expense")
public class Expense {

    /**
     * 报销单号: EXP+YYYYMMDD+序号
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 报销人ID
     */
    @TableField("applicant_id")
    private String applicantId;

    /**
     * 部门ID
     */
    @TableField("department_id")
    private String departmentId;

    /**
     * 报销类型: travel/hospitality/office/transport/other
     */
    private String type;

    /**
     * 总金额
     */
    private BigDecimal amount;

    /**
     * 报销事由
     */
    private String reason;

    /**
     * 申请日期
     */
    @TableField("apply_date")
    private LocalDate applyDate;

    /**
     * 费用发生日期
     */
    @TableField("expense_date")
    private LocalDate expenseDate;

    /**
     * 状态: draft/dept_pending/finance_pending/paid/rejected
     */
    private String status;

    // ========== 部门审批 ==========

    /**
     * 部门审批人ID
     */
    @TableField("dept_approver_id")
    private String deptApproverId;

    /**
     * 部门审批人姓名
     */
    @TableField("dept_approver_name")
    private String deptApproverName;

    /**
     * 部门审批状态: pending/approved/rejected
     */
    @TableField("dept_approval_status")
    private String deptApprovalStatus;

    /**
     * 部门审批意见
     */
    @TableField("dept_approval_opinion")
    private String deptApprovalOpinion;

    /**
     * 部门审批时间
     */
    @TableField("dept_approval_time")
    private LocalDateTime deptApprovalTime;

    // ========== 财务审批 ==========

    /**
     * 财务审批人ID
     */
    @TableField("finance_approver_id")
    private String financeApproverId;

    /**
     * 财务审批人姓名
     */
    @TableField("finance_approver_name")
    private String financeApproverName;

    /**
     * 财务审批状态: pending/approved/rejected
     */
    @TableField("finance_approval_status")
    private String financeApprovalStatus;

    /**
     * 财务审批意见
     */
    @TableField("finance_approval_opinion")
    private String financeApprovalOpinion;

    /**
     * 财务审批时间
     */
    @TableField("finance_approval_time")
    private LocalDateTime financeApprovalTime;

    // ========== 打款信息 ==========

    /**
     * 打款时间
     */
    @TableField("payment_date")
    private LocalDate paymentDate;

    /**
     * 打款凭证URL
     */
    @TableField("payment_proof")
    private String paymentProof;

    // ========== 审计字段 ==========

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 删除时间
     */
    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    // ========== 逻辑外键字段 (不映射到数据库) ==========

    /**
     * 报销人姓名
     */
    @TableField(exist = false)
    private String applicantName;

    /**
     * 报销人职位
     */
    @TableField(exist = false)
    private String applicantPosition;

    /**
     * 部门名称
     */
    @TableField(exist = false)
    private String departmentName;

    /**
     * 费用明细数量
     */
    @TableField(exist = false)
    private Integer itemCount;

    /**
     * 发票数量
     */
    @TableField(exist = false)
    private Integer invoiceCount;
}
```

### 2.2 ExpenseItem.java - 费用明细实体

```java
package com.example.oa_system_backend.module.expense.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 费用明细实体
 */
@Data
@TableName("approval_expense_item")
public class ExpenseItem {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报销单号
     */
    @TableField("expense_id")
    private String expenseId;

    /**
     * 费用说明
     */
    private String description;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 发生日期
     */
    @TableField("expense_date")
    private LocalDate expenseDate;

    /**
     * 费用分类
     */
    private String category;

    /**
     * 排序序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
```

### 2.3 ExpenseInvoice.java - 发票实体

```java
package com.example.oa_system_backend.module.expense.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 发票实体
 */
@Data
@TableName("approval_expense_invoice")
public class ExpenseInvoice {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报销单号
     */
    @TableField("expense_id")
    private String expenseId;

    /**
     * 发票类型: vat_special/vat_common/electronic
     */
    @TableField("invoice_type")
    private String invoiceType;

    /**
     * 发票号码
     */
    @TableField("invoice_number")
    private String invoiceNumber;

    /**
     * 发票金额
     */
    private BigDecimal amount;

    /**
     * 开票日期
     */
    @TableField("invoice_date")
    private LocalDate invoiceDate;

    /**
     * 发票图片URL
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 是否已验真
     */
    private Boolean verified;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
```

### 2.4 ExpensePayment.java - 打款记录实体

```java
package com.example.oa_system_backend.module.expense.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打款记录实体
 */
@Data
@TableName("approval_expense_payment")
public class ExpensePayment {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报销单号
     */
    @TableField("expense_id")
    private String expenseId;

    /**
     * 打款金额
     */
    private BigDecimal amount;

    /**
     * 打款方式: bank_transfer/cash/check
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 打款日期
     */
    @TableField("payment_date")
    private LocalDate paymentDate;

    /**
     * 收款账号
     */
    private String bankAccount;

    /**
     * 打款凭证URL
     */
    private String proof;

    /**
     * 状态: pending/completed/failed
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
```

---

## 3. 枚举类设计

### 3.1 ExpenseType.java - 报销类型

```java
package com.example.oa_system_backend.module.expense.enums;

import lombok.Getter;

/**
 * 报销类型枚举
 */
@Getter
public enum ExpenseType {

    /**
     * 差旅费
     */
    TRAVEL("travel", "差旅费"),

    /**
     * 招待费
     */
    HOSPITALITY("hospitality", "招待费"),

    /**
     * 办公费
     */
    OFFICE("office", "办公费"),

    /**
     * 交通费
     */
    TRANSPORT("transport", "交通费"),

    /**
     * 其他
     */
    OTHER("other", "其他");

    private final String code;
    private final String name;

    ExpenseType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据编码获取枚举
     */
    public static ExpenseType fromCode(String code) {
        for (ExpenseType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown expense type: " + code);
    }

    /**
     * 大额审批阈值
     */
    public BigDecimal getApprovalThreshold() {
        return switch (this) {
            case TRAVEL -> new BigDecimal("5000");
            case HOSPITALITY -> new BigDecimal("2000");
            case OFFICE -> new BigDecimal("1000");
            default -> BigDecimal.ZERO;
        };
    }
}
```

### 3.2 ExpenseStatus.java - 报销状态

```java
package com.example.oa_system_backend.module.expense.enums;

import lombok.Getter;

/**
 * 报销状态枚举
 */
@Getter
public enum ExpenseStatus {

    /**
     * 草稿
     */
    DRAFT("draft", "草稿"),

    /**
     * 待部门审批
     */
    DEPT_PENDING("dept_pending", "待部门审批"),

    /**
     * 待财务审批
     */
    FINANCE_PENDING("finance_pending", "待财务审批"),

    /**
     * 已打款
     */
    PAID("paid", "已打款"),

    /**
     * 已拒绝
     */
    REJECTED("rejected", "已拒绝");

    private final String code;
    private final String name;

    ExpenseStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据编码获取枚举
     */
    public static ExpenseStatus fromCode(String code) {
        for (ExpenseStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown expense status: " + code);
    }

    /**
     * 是否可以编辑
     */
    public boolean canEdit() {
        return this == DRAFT;
    }

    /**
     * 是否可以提交
     */
    public boolean canSubmit() {
        return this == DRAFT || this == REJECTED;
    }

    /**
     * 是否可以撤销
     */
    public boolean canCancel() {
        return this == DEPT_PENDING || this == FINANCE_PENDING;
    }

    /**
     * 是否可以删除
     */
    public boolean canDelete() {
        return this == DRAFT;
    }
}
```

### 3.3 InvoiceType.java - 发票类型

```java
package com.example.oa_system_backend.module.expense.enums;

import lombok.Getter;

/**
 * 发票类型枚举
 */
@Getter
public enum InvoiceType {

    /**
     * 增值税专用发票
     */
    VAT_SPECIAL("vat_special", "增值税专用发票"),

    /**
     * 增值税普通发票
     */
    VAT_COMMON("vat_common", "增值税普通发票"),

    /**
     * 电子发票
     */
    ELECTRONIC("electronic", "电子发票");

    private final String code;
    private final String name;

    InvoiceType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static InvoiceType fromCode(String code) {
        for (InvoiceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown invoice type: " + code);
    }
}
```

### 3.4 PaymentMethod.java - 打款方式

```java
package com.example.oa_system_backend.module.expense.enums;

import lombok.Getter;

/**
 * 打款方式枚举
 */
@Getter
public enum PaymentMethod {

    /**
     * 银行转账
     */
    BANK_TRANSFER("bank_transfer", "银行转账"),

    /**
     * 现金
     */
    CASH("cash", "现金"),

    /**
     * 支票
     */
    CHECK("check", "支票");

    private final String code;
    private final String name;

    PaymentMethod(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static PaymentMethod fromCode(String code) {
        for (PaymentMethod method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown payment method: " + code);
    }
}
```

---

## 4. DTO 设计

### 4.1 ExpenseCreateRequest.java - 创建报销单请求

```java
package com.example.oa_system_backend.module.expense.dto;

import com.example.oa_system_backend.module.expense.enums.ExpenseType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 创建报销单请求
 */
@Data
public class ExpenseCreateRequest {

    /**
     * 报销类型
     */
    @NotBlank(message = "报销类型不能为空")
    private String type;

    /**
     * 报销事由
     */
    @NotBlank(message = "报销事由不能为空")
    @Size(min = 10, max = 500, message = "报销事由长度必须在10-500字符之间")
    private String reason;

    /**
     * 费用发生日期
     */
    @NotNull(message = "费用发生日期不能为空")
    @PastOrPresent(message = "费用发生日期不能晚于今天")
    private LocalDate expenseDate;

    /**
     * 费用明细列表
     */
    @NotEmpty(message = "费用明细不能为空")
    @Size(min = 1, message = "至少需要一条费用明细")
    private List<ExpenseItemRequest> items;

    /**
     * 发票列表
     */
    @NotEmpty(message = "发票不能为空")
    @Size(min = 1, message = "至少需要一张发票")
    private List<ExpenseInvoiceRequest> invoices;

    /**
     * 费用明细请求
     */
    @Data
    public static class ExpenseItemRequest {

        /**
         * 费用说明
         */
        @NotBlank(message = "费用说明不能为空")
        @Size(max = 500, message = "费用说明不能超过500字符")
        private String description;

        /**
         * 金额
         */
        @NotNull(message = "金额不能为空")
        @DecimalMin(value = "0.01", message = "金额必须大于0")
        @Digits(integer = 8, fraction = 2, message = "金额格式不正确")
        private BigDecimal amount;

        /**
         * 发生日期
         */
        @NotNull(message = "发生日期不能为空")
        @PastOrPresent(message = "发生日期不能晚于今天")
        private LocalDate expenseDate;

        /**
         * 费用分类
         */
        private String category;
    }

    /**
     * 发票请求
     */
    @Data
    public static class ExpenseInvoiceRequest {

        /**
         * 发票类型
         */
        @NotBlank(message = "发票类型不能为空")
        private String invoiceType;

        /**
         * 发票号码
         */
        @NotBlank(message = "发票号码不能为空")
        @Pattern(regexp = "^\\d{8}$|^\\d{20}$", message = "发票号码格式不正确,应为8位或20位数字")
        private String invoiceNumber;

        /**
         * 发票金额
         */
        @NotNull(message = "发票金额不能为空")
        @DecimalMin(value = "0.01", message = "发票金额必须大于0")
        @Digits(integer = 8, fraction = 2, message = "发票金额格式不正确")
        private BigDecimal amount;

        /**
         * 开票日期
         */
        @NotNull(message = "开票日期不能为空")
        @PastOrPresent(message = "开票日期不能晚于今天")
        private LocalDate invoiceDate;

        /**
         * 发票图片URL
         */
        private String imageUrl;
    }
}
```

### 4.2 ExpenseUpdateRequest.java - 更新报销单请求

```java
package com.example.oa_system_backend.module.expense.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新报销单请求
 */
@Data
public class ExpenseUpdateRequest {

    /**
     * 版本号(乐观锁)
     */
    @NotNull(message = "版本号不能为空")
    private Integer version;

    /**
     * 报销事由
     */
    @Size(min = 10, max = 500, message = "报销事由长度必须在10-500字符之间")
    private String reason;

    /**
     * 费用发生日期
     */
    private java.time.LocalDate expenseDate;

    /**
     * 费用明细列表
     */
    @Valid
    private List<ExpenseCreateRequest.ExpenseItemRequest> items;

    /**
     * 发票列表
     */
    @Valid
    private List<ExpenseCreateRequest.ExpenseInvoiceRequest> invoices;
}
```

### 4.3 ExpenseQueryRequest.java - 查询报销单请求

```java
package com.example.oa_system_backend.module.expense.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 查询报销单请求
 */
@Data
public class ExpenseQueryRequest {

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 报销人ID
     */
    private String applicantId;

    /**
     * 部门ID
     */
    private String departmentId;

    /**
     * 报销类型
     */
    private String type;

    /**
     * 状态
     */
    private String status;

    /**
     * 申请日期开始
     */
    private LocalDate applyDateStart;

    /**
     * 申请日期结束
     */
    private LocalDate applyDateEnd;

    /**
     * 关键词搜索
     */
    private String keyword;
}
```

### 4.4 ApprovalRequest.java - 审批请求

```java
package com.example.oa_system_backend.module.expense.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 审批请求
 */
@Data
public class ApprovalRequest {

    /**
     * 审批状态: approved/rejected
     */
    @NotNull(message = "审批状态不能为空")
    @Pattern(regexp = "^(approved|rejected)$", message = "审批状态只能是approved或rejected")
    private String status;

    /**
     * 审批意见
     */
    @Size(max = 500, message = "审批意见不能超过500字符")
    private String opinion;
}
```

---

## 5. VO 设计

### 5.1 ExpenseVO.java - 报销单视图对象

```java
package com.example.oa_system_backend.module.expense.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报销单视图对象
 */
@Data
public class ExpenseVO {

    /**
     * 报销单号
     */
    private String id;

    /**
     * 报销人ID
     */
    private String applicantId;

    /**
     * 报销人姓名
     */
    private String applicantName;

    /**
     * 部门ID
     */
    private String departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 报销类型编码
     */
    private String type;

    /**
     * 报销类型名称
     */
    private String typeName;

    /**
     * 总金额
     */
    private BigDecimal amount;

    /**
     * 报销事由
     */
    private String reason;

    /**
     * 申请日期
     */
    private LocalDate applyDate;

    /**
     * 费用发生日期
     */
    private LocalDate expenseDate;

    /**
     * 状态编码
     */
    private String status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 部门审批人姓名
     */
    private String deptApproverName;

    /**
     * 部门审批状态
     */
    private String deptApprovalStatus;

    /**
     * 部门审批时间
     */
    private LocalDateTime deptApprovalTime;

    /**
     * 财务审批人姓名
     */
    private String financeApproverName;

    /**
     * 财务审批状态
     */
    private String financeApprovalStatus;

    /**
     * 财务审批时间
     */
    private LocalDateTime financeApprovalTime;

    /**
     * 打款时间
     */
    private LocalDate paymentDate;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 费用明细数量
     */
    private Integer itemCount;

    /**
     * 发票数量
     */
    private Integer invoiceCount;
}
```

### 5.2 ExpenseDetailVO.java - 报销单详情视图对象

```java
package com.example.oa_system_backend.module.expense.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报销单详情视图对象
 */
@Data
public class ExpenseDetailVO extends ExpenseVO {

    /**
     * 报销人职位
     */
    private String applicantPosition;

    /**
     * 报销人电话
     */
    private String applicantPhone;

    /**
     * 报销人邮箱
     */
    private String applicantEmail;

    /**
     * 部门审批意见
     */
    private String deptApprovalOpinion;

    /**
     * 财务审批意见
     */
    private String financeApprovalOpinion;

    /**
     * 打款凭证
     */
    private String paymentProof;

    /**
     * 费用明细列表
     */
    private List<ExpenseItemVO> items;

    /**
     * 发票列表
     */
    private List<ExpenseInvoiceVO> invoices;

    /**
     * 打款记录
     */
    private ExpensePaymentVO payment;

    /**
     * 费用明细VO
     */
    @Data
    public static class ExpenseItemVO {
        private Long id;
        private String description;
        private BigDecimal amount;
        private LocalDate expenseDate;
        private String category;
    }

    /**
     * 发票VO
     */
    @Data
    public static class ExpenseInvoiceVO {
        private Long id;
        private String invoiceType;
        private String invoiceTypeName;
        private String invoiceNumber;
        private BigDecimal amount;
        private LocalDate invoiceDate;
        private String imageUrl;
        private Boolean verified;
    }

    /**
     * 打款记录VO
     */
    @Data
    public static class ExpensePaymentVO {
        private Long id;
        private BigDecimal amount;
        private String paymentMethod;
        private String paymentMethodName;
        private LocalDate paymentDate;
        private String bankAccount;
        private String proof;
        private String status;
        private String statusName;
        private String remark;
    }
}
```

### 5.3 ExpenseStatisticsVO.java - 统计视图对象

```java
package com.example.oa_system_backend.module.expense.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 统计视图对象
 */
@Data
public class ExpenseStatisticsVO {

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 报销单数量
     */
    private Integer count;

    /**
     * 平均金额
     */
    private BigDecimal avgAmount;

    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;

    /**
     * 已拒绝金额
     */
    private BigDecimal rejectedAmount;

    /**
     * 占比(百分比)
     */
    private Double percentage;
}
```

---

## 6. Mapper 接口设计

### 6.1 ExpenseMapper.java

```java
package com.example.oa_system_backend.module.expense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.expense.entity.Expense;
import com.example.oa_system_backend.module.expense.dto.ExpenseQueryRequest;
import com.example.oa_system_backend.module.expense.vo.ExpenseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 费用报销Mapper
 */
@Mapper
public interface ExpenseMapper extends BaseMapper<Expense> {

    /**
     * 分页查询报销单列表
     */
    IPage<ExpenseVO> selectPageByCondition(
        Page<ExpenseVO> page,
        @Param("query") ExpenseQueryRequest query
    );

    /**
     * 查询报销单详情
     */
    ExpenseVO selectDetailById(@Param("id") String id);

    /**
     * 查询待部门审批的报销单
     */
    IPage<ExpenseVO> selectPendingDeptApproval(
        Page<ExpenseVO> page,
        @Param("approverId") String approverId
    );

    /**
     * 查询待财务审批的报销单
     */
    IPage<ExpenseVO> selectPendingFinanceApproval(
        Page<ExpenseVO> page,
        @Param("approverId") String approverId
    );
}
```

### 6.2 ExpenseMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.oa_system_backend.module.expense.mapper.ExpenseMapper">

    <!-- 结果映射 -->
    <resultMap id="ExpenseVOResultMap" type="com.example.oa_system_backend.module.expense.vo.ExpenseVO">
        <id property="id" column="id"/>
        <result property="applicantId" column="applicant_id"/>
        <result property="applicantName" column="applicant_name"/>
        <result property="departmentId" column="department_id"/>
        <result property="departmentName" column="department_name"/>
        <result property="type" column="type"/>
        <result property="amount" column="amount"/>
        <result property="reason" column="reason"/>
        <result property="applyDate" column="apply_date"/>
        <result property="expenseDate" column="expense_date"/>
        <result property="status" column="status"/>
        <result property="deptApproverName" column="dept_approver_name"/>
        <result property="deptApprovalStatus" column="dept_approval_status"/>
        <result property="deptApprovalTime" column="dept_approval_time"/>
        <result property="financeApproverName" column="finance_approver_name"/>
        <result property="financeApprovalStatus" column="finance_approval_status"/>
        <result property="financeApprovalTime" column="finance_approval_time"/>
        <result property="paymentDate" column="payment_date"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>

    <!-- 分页查询报销单列表 -->
    <select id="selectPageByCondition" resultMap="ExpenseVOResultMap">
        SELECT
            e.*,
            emp.name AS applicant_name,
            emp.position AS applicant_position,
            d.name AS department_name,
            dt.label AS type_name,
            ds.label AS status_name,
            (SELECT COUNT(*) FROM approval_expense_item WHERE expense_id = e.id) AS item_count,
            (SELECT COUNT(*) FROM approval_expense_invoice WHERE expense_id = e.id) AS invoice_count
        FROM approval_expense e
        LEFT JOIN sys_employee emp ON e.applicant_id = emp.id
        LEFT JOIN sys_department d ON e.department_id = d.id
        LEFT JOIN sys_dict_item dt ON dt.type_code = 'expense_type' AND dt.value = e.type
        LEFT JOIN sys_dict_item ds ON ds.type_code = 'expense_status' AND ds.value = e.status
        <where>
            e.deleted_at IS NULL
            <if test="query.applicantId != null and query.applicantId != ''">
                AND e.applicant_id = #{query.applicantId}
            </if>
            <if test="query.departmentId != null and query.departmentId != ''">
                AND e.department_id = #{query.departmentId}
            </if>
            <if test="query.type != null and query.type != ''">
                AND e.type = #{query.type}
            </if>
            <if test="query.status != null and query.status != ''">
                AND e.status = #{query.status}
            </if>
            <if test="query.applyDateStart != null">
                AND e.apply_date >= #{query.applyDateStart}
            </if>
            <if test="query.applyDateEnd != null">
                AND e.apply_date &lt;= #{query.applyDateEnd}
            </if>
            <if test="query.keyword != null and query.keyword != ''">
                AND (
                    e.reason LIKE CONCAT('%', #{query.keyword}, '%')
                    OR emp.name LIKE CONCAT('%', #{query.keyword}, '%')
                    OR e.id LIKE CONCAT('%', #{query.keyword}, '%')
                )
            </if>
        </where>
        ORDER BY e.created_at DESC
    </select>

    <!-- 查询报销单详情 -->
    <select id="selectDetailById" resultMap="ExpenseVOResultMap">
        SELECT
            e.*,
            emp.name AS applicant_name,
            emp.position AS applicant_position,
            emp.phone AS applicant_phone,
            emp.email AS applicant_email,
            d.name AS department_name,
            dt.label AS type_name,
            ds.label AS status_name
        FROM approval_expense e
        LEFT JOIN sys_employee emp ON e.applicant_id = emp.id
        LEFT JOIN sys_department d ON e.department_id = d.id
        LEFT JOIN sys_dict_item dt ON dt.type_code = 'expense_type' AND dt.value = e.type
        LEFT JOIN sys_dict_item ds ON ds.type_code = 'expense_status' AND ds.value = e.status
        WHERE e.id = #{id} AND e.deleted_at IS NULL
    </select>

    <!-- 查询待部门审批的报销单 -->
    <select id="selectPendingDeptApproval" resultMap="ExpenseVOResultMap">
        SELECT
            e.*,
            emp.name AS applicant_name,
            d.name AS department_name,
            dt.label AS type_name,
            ds.label AS status_name
        FROM approval_expense e
        LEFT JOIN sys_employee emp ON e.applicant_id = emp.id
        LEFT JOIN sys_department d ON e.department_id = d.id
        LEFT JOIN sys_dict_item dt ON dt.type_code = 'expense_type' AND dt.value = e.type
        LEFT JOIN sys_dict_item ds ON ds.type_code = 'expense_status' AND ds.value = e.status
        WHERE e.status = 'dept_pending'
            AND e.deleted_at IS NULL
            AND e.dept_approver_id = #{approverId}
        ORDER BY e.created_at ASC
    </select>

    <!-- 查询待财务审批的报销单 -->
    <select id="selectPendingFinanceApproval" resultMap="ExpenseVOResultMap">
        SELECT
            e.*,
            emp.name AS applicant_name,
            d.name AS department_name,
            dt.label AS type_name,
            ds.label AS status_name
        FROM approval_expense e
        LEFT JOIN sys_employee emp ON e.applicant_id = emp.id
        LEFT JOIN sys_department d ON e.department_id = d.id
        LEFT JOIN sys_dict_item dt ON dt.type_code = 'expense_type' AND dt.value = e.type
        LEFT JOIN sys_dict_item ds ON ds.type_code = 'expense_status' AND ds.value = e.status
        WHERE e.status = 'finance_pending'
            AND e.deleted_at IS NULL
            AND e.finance_approver_id = #{approverId}
        ORDER BY e.dept_approval_time ASC
    </select>

</mapper>
```

---

## 7. Service 接口设计

### 7.1 ExpenseService.java

```java
package com.example.oa_system_backend.module.expense.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.module.expense.dto.*;
import com.example.oa_system_backend.module.expense.entity.Expense;
import com.example.oa_system_backend.module.expense.vo.ExpenseDetailVO;
import com.example.oa_system_backend.module.expense.vo.ExpenseStatisticsVO;
import com.example.oa_system_backend.module.expense.vo.ExpenseVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 费用报销Service接口
 */
public interface ExpenseService {

    /**
     * 分页查询报销单列表
     */
    IPage<ExpenseVO> getExpenseList(ExpenseQueryRequest query);

    /**
     * 查询报销单详情
     */
    ExpenseDetailVO getExpenseDetail(String id);

    /**
     * 创建报销单
     */
    Expense createExpense(ExpenseCreateRequest request);

    /**
     * 更新报销单
     */
    Expense updateExpense(String id, ExpenseUpdateRequest request);

    /**
     * 删除报销单
     */
    void deleteExpense(String id);

    /**
     * 提交报销单
     */
    void submitExpense(String id);

    /**
     * 部门审批
     */
    void deptApprove(String id, ApprovalRequest request);

    /**
     * 财务审批
     */
    void financeApprove(String id, ApprovalRequest request);

    /**
     * 撤销报销单
     */
    void cancelExpense(String id);

    /**
     * 创建打款记录
     */
    void createPayment(String id);

    /**
     * 上传打款凭证
     */
    void uploadPaymentProof(String expenseId, String proofUrl);

    /**
     * 查询待部门审批列表
     */
    IPage<ExpenseVO> getPendingDeptApproval(ExpenseQueryRequest query);

    /**
     * 查询待财务审批列表
     */
    IPage<ExpenseVO> getPendingFinanceApproval(ExpenseQueryRequest query);

    /**
     * 按部门统计
     */
    List<ExpenseStatisticsVO> getDepartmentStats(LocalDate startDate, LocalDate endDate, String departmentId);

    /**
     * 按类型统计
     */
    List<ExpenseStatisticsVO> getTypeStats(LocalDate startDate, LocalDate endDate);

    /**
     * 按月度统计
     */
    List<ExpenseStatisticsVO> getMonthlyStats(Integer year);

    /**
     * 验证发票唯一性
     */
    boolean validateInvoiceUnique(String invoiceNumber);

    /**
     * 发票OCR识别
     */
    Object recognizeInvoice(String imageUrl);
}
```

---

## 8. Service 实现设计

### 8.1 ExpenseServiceImpl.java 核心方法

```java
package com.example.oa_system_backend.module.expense.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oa_system_backend.common.exception.BusinessException;
import com.example.oa_system_backend.common.utils.SecurityUtils;
import com.example.oa_system_backend.module.expense.dto.*;
import com.example.oa_system_backend.module.expense.entity.*;
import com.example.oa_system_backend.module.expense.enums.*;
import com.example.oa_system_backend.module.expense.mapper.*;
import com.example.oa_system_backend.module.expense.service.ExpenseService;
import com.example.oa_system_backend.module.expense.util.ExpenseIdGenerator;
import com.example.oa_system_backend.module.expense.util.InvoiceValidator;
import com.example.oa_system_backend.module.expense.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 费用报销Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl extends ServiceImpl<ExpenseMapper, Expense>
        implements ExpenseService {

    private final ExpenseMapper expenseMapper;
    private final ExpenseItemMapper expenseItemMapper;
    private final ExpenseInvoiceMapper expenseInvoiceMapper;
    private final ExpensePaymentMapper expensePaymentMapper;
    private final ExpenseIdGenerator expenseIdGenerator;
    private final InvoiceValidator invoiceValidator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Expense createExpense(ExpenseCreateRequest request) {
        // 1. 验证发票唯一性
        for (ExpenseCreateRequest.ExpenseInvoiceRequest invoiceReq : request.getInvoices()) {
            if (!invoiceValidator.validateUnique(invoiceReq.getInvoiceNumber())) {
                throw new BusinessException(4001, "发票号 " + invoiceReq.getInvoiceNumber() + " 已被使用");
            }
        }

        // 2. 验证发票金额一致性
        BigDecimal invoiceTotal = request.getInvoices().stream()
                .map(ExpenseCreateRequest.ExpenseInvoiceRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal itemsTotal = request.getItems().stream()
                .map(ExpenseCreateRequest.ExpenseItemRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (invoiceTotal.compareTo(itemsTotal) < 0) {
            throw new BusinessException(4002, "发票总金额不能小于明细总金额");
        }

        // 3. 验证报销金额限制
        ExpenseType expenseType = ExpenseType.fromCode(request.getType());
        if (itemsTotal.compareTo(expenseType.getApprovalThreshold()) > 0) {
            // 检查是否需要加签
            checkLargeAmountApproval(request.getApplicantId(), itemsTotal);
        }

        // 4. 生成报销单号
        String expenseId = expenseIdGenerator.generate();

        // 5. 创建报销单
        Expense expense = new Expense();
        expense.setId(expenseId);
        expense.setApplicantId(SecurityUtils.getCurrentUserId());
        expense.setDepartmentId(SecurityUtils.getCurrentUserDepartmentId());
        expense.setType(request.getType());
        expense.setAmount(itemsTotal);
        expense.setReason(request.getReason());
        expense.setApplyDate(LocalDate.now());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setStatus(ExpenseStatus.DRAFT.getCode());
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());

        save(expense);

        // 6. 保存费用明细
        int sortOrder = 1;
        for (ExpenseCreateRequest.ExpenseItemRequest itemReq : request.getItems()) {
            ExpenseItem item = new ExpenseItem();
            item.setExpenseId(expenseId);
            item.setDescription(itemReq.getDescription());
            item.setAmount(itemReq.getAmount());
            item.setExpenseDate(itemReq.getExpenseDate());
            item.setCategory(itemReq.getCategory());
            item.setSortOrder(sortOrder++);
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            expenseItemMapper.insert(item);
        }

        // 7. 保存发票
        for (ExpenseCreateRequest.ExpenseInvoiceRequest invoiceReq : request.getInvoices()) {
            ExpenseInvoice invoice = new ExpenseInvoice();
            invoice.setExpenseId(expenseId);
            invoice.setInvoiceType(invoiceReq.getInvoiceType());
            invoice.setInvoiceNumber(invoiceReq.getInvoiceNumber());
            invoice.setAmount(invoiceReq.getAmount());
            invoice.setInvoiceDate(invoiceReq.getInvoiceDate());
            invoice.setImageUrl(invoiceReq.getImageUrl());
            invoice.setVerified(false);
            invoice.setCreatedAt(LocalDateTime.now());
            invoice.setUpdatedAt(LocalDateTime.now());
            expenseInvoiceMapper.insert(invoice);
        }

        log.info("创建报销单成功: {}", expenseId);
        return expense;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitExpense(String id) {
        Expense expense = getById(id);
        if (expense == null) {
            throw new BusinessException(4003, "报销单不存在");
        }

        // 检查状态
        ExpenseStatus status = ExpenseStatus.fromCode(expense.getStatus());
        if (!status.canSubmit()) {
            throw new BusinessException(4004, "当前状态不允许提交");
        }

        // 验证发票
        Long invoiceCount = expenseInvoiceMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ExpenseInvoice>()
                        .eq("expense_id", id)
        );
        if (invoiceCount == 0) {
            throw new BusinessException(4005, "请至少上传一张发票");
        }

        // 获取部门负责人
        String deptLeaderId = getDepartmentLeader(expense.getDepartmentId());

        // 更新状态为待部门审批
        expense.setStatus(ExpenseStatus.DEPT_PENDING.getCode());
        expense.setDeptApproverId(deptLeaderId);
        expense.setDeptApproverName(getEmployeeName(deptLeaderId));
        expense.setDeptApprovalStatus("pending");
        expense.setUpdatedAt(LocalDateTime.now());

        updateById(expense);

        // 发送通知
        // TODO: 实现通知功能

        log.info("提交报销单成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deptApprove(String id, ApprovalRequest request) {
        Expense expense = getById(id);
        if (expense == null) {
            throw new BusinessException(4003, "报销单不存在");
        }

        // 检查状态
        if (!ExpenseStatus.DEPT_PENDING.getCode().equals(expense.getStatus())) {
            throw new BusinessException(4006, "当前状态不允许部门审批");
        }

        // 检查审批权限
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!currentUserId.equals(expense.getDeptApproverId())) {
            throw new BusinessException(4007, "您不是该报销单的部门审批人");
        }

        LocalDateTime now = LocalDateTime.now();

        if ("approved".equals(request.getStatus())) {
            // 审批通过,进入财务审批
            // 获取财务审批人
            String financeApproverId = getFinanceApprover();

            expense.setStatus(ExpenseStatus.FINANCE_PENDING.getCode());
            expense.setDeptApproverId(currentUserId);
            expense.setDeptApproverName(getEmployeeName(currentUserId));
            expense.setDeptApprovalStatus("approved");
            expense.setDeptApprovalOpinion(request.getOpinion());
            expense.setDeptApprovalTime(now);
            expense.setFinanceApproverId(financeApproverId);
            expense.setFinanceApproverName(getEmployeeName(financeApproverId));
            expense.setFinanceApprovalStatus("pending");
        } else {
            // 驳回
            expense.setStatus(ExpenseStatus.REJECTED.getCode());
            expense.setDeptApproverId(currentUserId);
            expense.setDeptApproverName(getEmployeeName(currentUserId));
            expense.setDeptApprovalStatus("rejected");
            expense.setDeptApprovalOpinion(request.getOpinion());
            expense.setDeptApprovalTime(now);
        }

        expense.setUpdatedAt(now);
        updateById(expense);

        // 发送通知
        // TODO: 实现通知功能

        log.info("部门审批完成: {}, 结果: {}", id, request.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void financeApprove(String id, ApprovalRequest request) {
        Expense expense = getById(id);
        if (expense == null) {
            throw new BusinessException(4003, "报销单不存在");
        }

        // 检查状态
        if (!ExpenseStatus.FINANCE_PENDING.getCode().equals(expense.getStatus())) {
            throw new BusinessException(4008, "当前状态不允许财务审批");
        }

        // 检查审批权限
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!currentUserId.equals(expense.getFinanceApproverId())) {
            throw new BusinessException(4009, "您不是该报销单的财务审批人");
        }

        LocalDateTime now = LocalDateTime.now();

        if ("approved".equals(request.getStatus())) {
            // 审批通过,自动创建打款记录
            createPaymentRecord(expense);

            expense.setStatus(ExpenseStatus.PAID.getCode());
            expense.setFinanceApproverId(currentUserId);
            expense.setFinanceApproverName(getEmployeeName(currentUserId));
            expense.setFinanceApprovalStatus("approved");
            expense.setFinanceApprovalOpinion(request.getOpinion());
            expense.setFinanceApprovalTime(now);
        } else {
            // 驳回
            expense.setStatus(ExpenseStatus.REJECTED.getCode());
            expense.setFinanceApproverId(currentUserId);
            expense.setFinanceApproverName(getEmployeeName(currentUserId));
            expense.setFinanceApprovalStatus("rejected");
            expense.setFinanceApprovalOpinion(request.getOpinion());
            expense.setFinanceApprovalTime(now);
        }

        expense.setUpdatedAt(now);
        updateById(expense);

        // 发送通知
        // TODO: 实现通知功能

        log.info("财务审批完成: {}, 结果: {}", id, request.getStatus());
    }

    /**
     * 创建打款记录
     */
    private void createPaymentRecord(Expense expense) {
        // 获取员工银行账户
        String bankAccount = getEmployeeBankAccount(expense.getApplicantId());

        ExpensePayment payment = new ExpensePayment();
        payment.setExpenseId(expense.getId());
        payment.setAmount(expense.getAmount());
        payment.setPaymentMethod(PaymentMethod.BANK_TRANSFER.getCode());
        payment.setPaymentDate(LocalDate.now());
        payment.setBankAccount(bankAccount);
        payment.setStatus("pending");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        expensePaymentMapper.insert(payment);
    }

    /**
     * 检查大额审批
     */
    private void checkLargeAmountApproval(String applicantId, BigDecimal amount) {
        // 单笔金额检查
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            // 需要总经理+特别审批人
            // TODO: 实现大额加签逻辑
        } else if (amount.compareTo(new BigDecimal("5000")) > 0) {
            // 需要总经理审批
            // TODO: 实现大额加签逻辑
        }

        // 月度累计检查
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        BigDecimal monthlyTotal = getMonthlyTotalAmount(applicantId, monthStart, LocalDate.now());
        monthlyTotal = monthlyTotal.add(amount);

        if (monthlyTotal.compareTo(new BigDecimal("50000")) > 0) {
            // 超过5万需要特殊审批
            // TODO: 实现月度累计加签逻辑
        }
    }

    /**
     * 获取月度累计金额
     */
    private BigDecimal getMonthlyTotalAmount(String applicantId, LocalDate start, LocalDate end) {
        // 查询当月已支付的报销总金额
        return expenseMapper.selectMonthlyTotalAmount(applicantId, start, end);
    }

    /**
     * 获取部门负责人
     */
    private String getDepartmentLeader(String departmentId) {
        // TODO: 实现获取部门负责人逻辑
        return "DEPT_LEADER_ID";
    }

    /**
     * 获取财务审批人
     */
    private String getFinanceApprover() {
        // TODO: 实现获取财务审批人逻辑
        return "FINANCE_ID";
    }

    /**
     * 获取员工姓名
     */
    private String getEmployeeName(String employeeId) {
        // TODO: 实现获取员工姓名逻辑
        return "Employee Name";
    }

    /**
     * 获取员工银行账户
     */
    private String getEmployeeBankAccount(String employeeId) {
        // TODO: 实现获取员工银行账户逻辑
        return "6222021234567890123";
    }

    // 其他方法实现省略...
}
```

---

## 9. Controller 设计

### 9.1 ExpenseController.java

```java
package com.example.oa_system_backend.module.expense.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.expense.dto.*;
import com.example.oa_system_backend.module.expense.service.ExpenseService;
import com.example.oa_system_backend.module.expense.vo.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 费用报销Controller
 */
@Slf4j
@RestController
@RequestMapping("/expense")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * 分页查询报销单列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('expense:view')")
    public ApiResponse<IPage<ExpenseVO>> getExpenseList(ExpenseQueryRequest query) {
        IPage<ExpenseVO> result = expenseService.getExpenseList(query);
        return ApiResponse.success(result);
    }

    /**
     * 查询报销单详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('expense:view')")
    public ApiResponse<ExpenseDetailVO> getExpenseDetail(@PathVariable String id) {
        ExpenseDetailVO detail = expenseService.getExpenseDetail(id);
        return ApiResponse.success(detail);
    }

    /**
     * 创建报销单
     */
    @PostMapping
    @PreAuthorize("hasAuthority('expense:create')")
    public ApiResponse<Expense> createExpense(@Valid @RequestBody ExpenseCreateRequest request) {
        Expense expense = expenseService.createExpense(request);
        return ApiResponse.success(expense);
    }

    /**
     * 更新报销单
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('expense:edit')")
    public ApiResponse<Void> updateExpense(
            @PathVariable String id,
            @Valid @RequestBody ExpenseUpdateRequest request) {
        expenseService.updateExpense(id, request);
        return ApiResponse.success();
    }

    /**
     * 删除报销单
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('expense:delete')")
    public ApiResponse<Void> deleteExpense(@PathVariable String id) {
        expenseService.deleteExpense(id);
        return ApiResponse.success();
    }

    /**
     * 提交报销单
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('expense:submit')")
    public ApiResponse<Void> submitExpense(@PathVariable String id) {
        expenseService.submitExpense(id);
        return ApiResponse.success();
    }

    /**
     * 部门审批
     */
    @PostMapping("/{id}/dept-approval")
    @PreAuthorize("hasAuthority('expense:dept_approve')")
    public ApiResponse<Void> deptApprove(
            @PathVariable String id,
            @Valid @RequestBody ApprovalRequest request) {
        expenseService.deptApprove(id, request);
        return ApiResponse.success();
    }

    /**
     * 财务审批
     */
    @PostMapping("/{id}/finance-approval")
    @PreAuthorize("hasAuthority('expense:finance_approve')")
    public ApiResponse<Void> financeApprove(
            @PathVariable String id,
            @Valid @RequestBody ApprovalRequest request) {
        expenseService.financeApprove(id, request);
        return ApiResponse.success();
    }

    /**
     * 撤销报销单
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('expense:cancel')")
    public ApiResponse<Void> cancelExpense(@PathVariable String id) {
        expenseService.cancelExpense(id);
        return ApiResponse.success();
    }

    /**
     * 创建打款记录
     */
    @PostMapping("/{id}/payment")
    @PreAuthorize("hasAuthority('expense:payment')")
    public ApiResponse<Void> createPayment(@PathVariable String id) {
        expenseService.createPayment(id);
        return ApiResponse.success();
    }

    /**
     * 上传打款凭证
     */
    @PostMapping("/{id}/payment-proof")
    @PreAuthorize("hasAuthority('expense:payment')")
    public ApiResponse<Void> uploadPaymentProof(
            @PathVariable String id,
            @RequestParam String proofUrl) {
        expenseService.uploadPaymentProof(id, proofUrl);
        return ApiResponse.success();
    }

    /**
     * 查询待部门审批列表
     */
    @GetMapping("/pending/dept")
    @PreAuthorize("hasAuthority('expense:dept_approve')")
    public ApiResponse<IPage<ExpenseVO>> getPendingDeptApproval(ExpenseQueryRequest query) {
        IPage<ExpenseVO> result = expenseService.getPendingDeptApproval(query);
        return ApiResponse.success(result);
    }

    /**
     * 查询待财务审批列表
     */
    @GetMapping("/pending/finance")
    @PreAuthorize("hasAuthority('expense:finance_approve')")
    public ApiResponse<IPage<ExpenseVO>> getPendingFinanceApproval(ExpenseQueryRequest query) {
        IPage<ExpenseVO> result = expenseService.getPendingFinanceApproval(query);
        return ApiResponse.success(result);
    }

    /**
     * 验证发票唯一性
     */
    @GetMapping("/invoices/validate")
    public ApiResponse<Boolean> validateInvoice(@RequestParam String number) {
        boolean valid = expenseService.validateInvoiceUnique(number);
        return ApiResponse.success(valid);
    }

    /**
     * 发票OCR识别
     */
    @PostMapping("/invoices/ocr")
    public ApiResponse<Object> recognizeInvoice(@RequestBody InvoiceOCRRequest request) {
        Object result = expenseService.recognizeInvoice(request.getImageUrl());
        return ApiResponse.success(result);
    }

    /**
     * 按部门统计
     */
    @GetMapping("/stats/department")
    @PreAuthorize("hasAuthority('expense:stats')")
    public ApiResponse<List<ExpenseStatisticsVO>> getDepartmentStats(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) String departmentId) {
        List<ExpenseStatisticsVO> result = expenseService.getDepartmentStats(
                startDate, endDate, departmentId);
        return ApiResponse.success(result);
    }

    /**
     * 按类型统计
     */
    @GetMapping("/stats/type")
    @PreAuthorize("hasAuthority('expense:stats')")
    public ApiResponse<List<ExpenseStatisticsVO>> getTypeStats(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<ExpenseStatisticsVO> result = expenseService.getTypeStats(startDate, endDate);
        return ApiResponse.success(result);
    }

    /**
     * 按月度统计
     */
    @GetMapping("/stats/monthly")
    @PreAuthorize("hasAuthority('expense:stats')")
    public ApiResponse<List<ExpenseStatisticsVO>> getMonthlyStats(
            @RequestParam Integer year) {
        List<ExpenseStatisticsVO> result = expenseService.getMonthlyStats(year);
        return ApiResponse.success(result);
    }
}
```

---

## 10. 工具类设计

### 10.1 ExpenseIdGenerator.java - 报销单号生成器

```java
package com.example.oa_system_backend.module.expense.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 报销单号生成器
 * 格式: EXP+YYYYMMDD+序号
 */
@Component
public class ExpenseIdGenerator {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicInteger counter = new AtomicInteger(1);
    private String lastDate = "";

    /**
     * 生成报销单号
     */
    public synchronized String generate() {
        String currentDate = LocalDateTime.now().format(formatter);

        // 日期变化时重置计数器
        if (!currentDate.equals(lastDate)) {
            lastDate = currentDate;
            counter.set(1);
        }

        // 生成4位序号
        int sequence = counter.getAndIncrement();
        if (sequence > 9999) {
            throw new RuntimeException("当日报销单号已用完");
        }

        return String.format("EXP%s%04d", currentDate, sequence);
    }
}
```

### 10.2 InvoiceValidator.java - 发票验证器

```java
package com.example.oa_system_backend.module.expense.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.oa_system_backend.module.expense.entity.ExpenseInvoice;
import com.example.oa_system_backend.module.expense.mapper.ExpenseInvoiceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 发票验证器
 */
@Component
@RequiredArgsConstructor
public class InvoiceValidator {

    private final ExpenseInvoiceMapper expenseInvoiceMapper;

    /**
     * 发票号码格式
     */
    private static final Pattern INVOICE_NUMBER_PATTERN = Pattern.compile("^\\d{8}$|^\\d{20}$");

    /**
     * 验证发票号码格式
     */
    public boolean validateFormat(String invoiceNumber) {
        if (invoiceNumber == null || invoiceNumber.isEmpty()) {
            return false;
        }
        return INVOICE_NUMBER_PATTERN.matcher(invoiceNumber).matches();
    }

    /**
     * 验证发票唯一性
     */
    public boolean validateUnique(String invoiceNumber) {
        if (!validateFormat(invoiceNumber)) {
            return false;
        }

        Long count = expenseInvoiceMapper.selectCount(
                new QueryWrapper<ExpenseInvoice>()
                        .eq("invoice_number", invoiceNumber)
        );

        return count == 0;
    }
}
```

---

## 11. 业务规则验证

### 11.1 金额验证

```java
/**
 * 验证金额限制
 */
private void validateAmountLimit(String type, BigDecimal amount) {
    ExpenseType expenseType = ExpenseType.fromCode(type);
    BigDecimal limit = expenseType.getApprovalThreshold();

    if (amount.compareTo(limit) > 0) {
        throw new BusinessException(
                4010,
                String.format("%s单次报销不能超过%s元",
                        expenseType.getName(),
                        limit)
        );
    }
}
```

### 11.2 日期验证

```java
/**
 * 验证费用发生日期
 */
private void validateExpenseDate(LocalDate expenseDate) {
    if (expenseDate.isAfter(LocalDate.now())) {
        throw new BusinessException(4011, "费用发生日期不能晚于今天");
    }
}
```

### 11.3 发票验证

```java
/**
 * 验证发票信息
 */
private void validateInvoices(List<ExpenseInvoice> invoices, BigDecimal totalAmount) {
    if (invoices == null || invoices.isEmpty()) {
        throw new BusinessException(4012, "请至少上传一张发票");
    }

    // 验证发票号码格式
    for (ExpenseInvoice invoice : invoices) {
        if (!invoiceValidator.validateFormat(invoice.getInvoiceNumber())) {
            throw new BusinessException(4013,
                    "发票号码格式不正确: " + invoice.getInvoiceNumber());
        }

        // 验证唯一性
        if (!invoiceValidator.validateUnique(invoice.getInvoiceNumber())) {
            throw new BusinessException(4014,
                    "发票号已被使用: " + invoice.getInvoiceNumber());
        }
    }

    // 验证发票金额
    BigDecimal invoiceTotal = invoices.stream()
            .map(ExpenseInvoice::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (invoiceTotal.compareTo(totalAmount) < 0) {
        throw new BusinessException(4015,
                String.format("发票总金额(%s)小于报销金额(%s)",
                        invoiceTotal, totalAmount));
    }
}
```

---

## 12. 异常处理

### 12.1 自定义业务异常

```java
/**
 * 费用报销模块业务异常
 */
public class ExpenseException extends RuntimeException {

    private final Integer code;

    public ExpenseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
```

### 12.2 全局异常处理

```java
@RestControllerAdvice
public class ExpenseExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(ExpenseException.class)
    public ApiResponse<Void> handleExpenseException(ExpenseException e) {
        log.error("费用报销业务异常: {}", e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数验证异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("参数验证失败: {}", message);
        return ApiResponse.error(400, message);
    }
}
```

---

## 13. 权限控制

### 13.1 权限定义

```java
/**
 * 费用报销权限常量
 */
public class ExpensePermissions {

    /**
     * 查看报销单
     */
    public static final String VIEW = "expense:view";

    /**
     * 创建报销单
     */
    public static final String CREATE = "expense:create";

    /**
     * 编辑报销单
     */
    public static final String EDIT = "expense:edit";

    /**
     * 删除报销单
     */
    public static final String DELETE = "expense:delete";

    /**
     * 提交报销单
     */
    public static final String SUBMIT = "expense:submit";

    /**
     * 部门审批
     */
    public static final String DEPT_APPROVE = "expense:dept_approve";

    /**
     * 财务审批
     */
    public static final String FINANCE_APPROVE = "expense:finance_approve";

    /**
     * 打款操作
     */
    public static final String PAYMENT = "expense:payment";

    /**
     * 统计查看
     */
    public static final String STATS = "expense:stats";

    /**
     * 撤销报销单
     */
    public static final String CANCEL = "expense:cancel";
}
```

### 13.2 数据权限过滤

```java
/**
 * 数据权限过滤
 */
public class ExpenseDataPermissionFilter {

    /**
     * 过滤查询条件
     */
    public void filterQuery(ExpenseQueryRequest query) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        List<String> roles = SecurityUtils.getCurrentUserRoles();

        // 拥有查看所有权限
        if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_FINANCE")) {
            return;
        }

        // 部门负责人可以查看本部门
        if (roles.contains("ROLE_DEPT_LEADER")) {
            String departmentId = SecurityUtils.getCurrentUserDepartmentId();
            query.setDepartmentId(departmentId);
            return;
        }

        // 普通员工只能查看自己的
        query.setApplicantId(currentUserId);
    }
}
```

---

## 14. 配置文件

### 14.1 application.yml

```yaml
# 费用报销模块配置
expense:
  # 报销单号配置
  id:
    prefix: EXP
    date-format: yyyyMMdd

  # 大额审批阈值
  approval:
    thresholds:
      travel: 5000      # 差旅费
      hospitality: 2000  # 招待费
      office: 1000      # 办公费

  # 月度累计审批阈值
  monthly:
    level-1: 20000      # 第一级: 2万
    level-2: 50000      # 第二级: 5万

  # 文件上传配置
  upload:
    path: /data/oa/uploads/expense
    url-prefix: /uploads/expense
    max-size: 10485760  # 10MB

  # OCR配置
  ocr:
    enabled: true
    provider: baidu  # baidu/tencent/aliyun
    api-key: ${OCR_API_KEY}
    secret-key: ${OCR_SECRET_KEY}
```

---

## 15. 测试用例

### 15.1 Service测试

```java
@SpringBootTest
@Transactional
class ExpenseServiceTest {

    @Autowired
    private ExpenseService expenseService;

    @Test
    void testCreateExpense() {
        ExpenseCreateRequest request = new ExpenseCreateRequest();
        // 设置请求数据...

        Expense expense = expenseService.createExpense(request);

        assertNotNull(expense);
        assertNotNull(expense.getId());
        assertEquals("draft", expense.getStatus());
    }

    @Test
    void testSubmitExpense() {
        String expenseId = "EXP202601210001";

        expenseService.submitExpense(expenseId);

        Expense expense = expenseService.getById(expenseId);
        assertEquals("dept_pending", expense.getStatus());
    }

    @Test
    void testDeptApprove() {
        String expenseId = "EXP202601210001";

        ApprovalRequest request = new ApprovalRequest();
        request.setStatus("approved");
        request.setOpinion("同意");

        expenseService.deptApprove(expenseId, request);

        Expense expense = expenseService.getById(expenseId);
        assertEquals("finance_pending", expense.getStatus());
    }

    @Test
    void testValidateInvoiceUnique() {
        String invoiceNumber = "12345678";

        boolean valid = expenseService.validateInvoiceUnique(invoiceNumber);

        assertTrue(valid);
    }
}
```

### 15.2 Controller测试

```java
@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

    @Test
    void testGetExpenseList() throws Exception {
        mockMvc.perform(get("/expense")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testCreateExpense() throws Exception {
        ExpenseCreateRequest request = new ExpenseCreateRequest();
        // 设置请求数据...

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/expense")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

---

## 16. 部署说明

### 16.1 数据库初始化

1. 执行数据库建表脚本: `01_create_tables.sql`
2. 执行约束脚本: `02_create_constraints.sql`
3. 初始化数据字典: `04_init_data.sql`

### 16.2 配置修改

1. 修改 `application.yml` 中的数据库连接信息
2. 配置文件上传路径
3. 配置OCR API密钥

### 16.3 启动应用

```bash
mvn clean package
java -jar oa-system-backend.jar
```

---

## 17. API文档

完整的API文档请参考 Swagger UI:

```
http://localhost:8080/api/swagger-ui.html
http://localhost:8080/api/doc.html
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-21
