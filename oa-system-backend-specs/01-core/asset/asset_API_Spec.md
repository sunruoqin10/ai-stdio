# 资产管理后端API规范

> **对应前端规范**: [oa-system-frontend-specs/core/asset/asset_Technical.md](../../../oa-system-frontend-specs/core/asset/asset_Technical.md)
> **对应数据库设计**: [asset.md](../../../oa-system-database-specs/01-core/asset.md)
> **技术栈**: Spring Boot 3.x + MyBatis-Plus 3.5.x + MySQL 8.0+
> **版本**: v1.0.0

---

## 1. 模块架构

### 1.1 包结构

```
com.company.oa.asset
├── controller          # 控制器层
│   ├── AssetController.java
│   ├── AssetBorrowController.java
│   └── AssetMaintenanceController.java
├── service             # 服务层接口
│   ├── AssetService.java
│   ├── AssetBorrowService.java
│   └── AssetMaintenanceService.java
├── service.impl        # 服务层实现
│   ├── AssetServiceImpl.java
│   ├── AssetBorrowServiceImpl.java
│   └── AssetMaintenanceServiceImpl.java
├── mapper              # 数据访问层
│   ├── AssetMapper.java
│   ├── AssetBorrowRecordMapper.java
│   └── AssetMaintenanceMapper.java
├── entity              # 实体类
│   ├── Asset.java
│   ├── AssetBorrowRecord.java
│   └── AssetMaintenance.java
├── dto                 # 数据传输对象
│   ├── request
│   │   ├── AssetCreateRequest.java
│   │   ├── AssetUpdateRequest.java
│   │   ├── AssetQueryRequest.java
│   │   ├── AssetBorrowRequest.java
│   │   └── AssetReturnRequest.java
│   └── response
│       ├── AssetResponse.java
│       ├── AssetStatisticsResponse.java
│       └── AssetBorrowRecordResponse.java
├── vo                  # 视图对象
│   ├── AssetVO.java
│   ├── AssetBorrowRecordVO.java
│   └── AssetMaintenanceVO.java
├── enums               # 枚举类
│   ├── AssetCategoryEnum.java
│   ├── AssetStatusEnum.java
│   └── BorrowStatusEnum.java
├── config              # 配置类
│   └── AssetConfig.java
└── util                # 工具类
    └── AssetDepreciationUtil.java
```

### 1.2 依赖配置

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.5</version>
    </dependency>

    <!-- MySQL Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## 2. 实体类设计

### 2.1 Asset (资产实体)

```java
package com.company.oa.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产实体类
 * 对应表: biz_asset
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("biz_asset")
public class Asset {

    /**
     * 资产编号 (格式: ASSET+序号)
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /**
     * 资产名称
     */
    private String name;

    /**
     * 资产类别: electronic(电子设备), furniture(家具), book(图书), other(其他)
     */
    private String category;

    /**
     * 品牌/型号
     */
    private String brandModel;

    /**
     * 购置日期
     */
    private LocalDate purchaseDate;

    /**
     * 购置金额(元)
     */
    private BigDecimal purchasePrice;

    /**
     * 当前价值(自动计算折旧)
     */
    private BigDecimal currentValue;

    /**
     * 资产状态: stock(在库), in_use(使用中), borrowed(借出), maintenance(维护中), scrapped(报废)
     */
    private String status;

    /**
     * 使用人/保管人ID
     */
    private String userId;

    /**
     * 存放位置
     */
    private String location;

    /**
     * 借出日期
     */
    private LocalDate borrowDate;

    /**
     * 预计归还日期
     */
    private LocalDate expectedReturnDate;

    /**
     * 实际归还日期
     */
    private LocalDate actualReturnDate;

    /**
     * 维护记录(JSON数组)
     */
    @TableField(typeHandler = JsonListTypeHandler.class)
    private List<MaintenanceRecord> maintenanceRecord;

    /**
     * 资产图片URL数组
     */
    @TableField(typeHandler = JsonListTypeHandler.class)
    private List<String> images;

    /**
     * 备注信息
     */
    private String notes;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /**
     * 是否删除(0否1是)
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 删除时间
     */
    private LocalDateTime deletedAt;

    /**
     * 删除人ID
     */
    private String deletedBy;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 维护记录内部类
     */
    @Data
    public static class MaintenanceRecord {
        private String type;
        private String description;
        private BigDecimal cost;
        private LocalDate startDate;
        private LocalDate endDate;
        private String operatorId;
        private String operatorName;
    }
}
```

### 2.2 AssetBorrowRecord (借还记录实体)

```java
package com.company.oa.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资产借还记录实体类
 * 对应表: biz_asset_borrow_record
 */
@Data
@TableName("biz_asset_borrow_record")
public class AssetBorrowRecord {

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 资产ID
     */
    private String assetId;

    /**
     * 资产名称(快照)
     */
    private String assetName;

    /**
     * 借用人ID
     */
    private String borrowerId;

    /**
     * 借用人姓名(快照)
     */
    private String borrowerName;

    /**
     * 借出日期
     */
    private LocalDate borrowDate;

    /**
     * 预计归还日期
     */
    private LocalDate expectedReturnDate;

    /**
     * 实际归还日期
     */
    private LocalDate actualReturnDate;

    /**
     * 记录状态: active(借出中), returned(已归还), overdue(已超期)
     */
    private String status;

    /**
     * 备注
     */
    private String notes;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

### 2.3 AssetMaintenance (维护记录实体)

```java
package com.company.oa.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资产维护记录实体类
 * 对应表: biz_asset_maintenance
 */
@Data
@TableName("biz_asset_maintenance")
public class AssetMaintenance {

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 资产ID
     */
    private String assetId;

    /**
     * 类型: repair(修理), maintenance(保养), check(检查)
     */
    private String type;

    /**
     * 描述
     */
    private String description;

    /**
     * 费用
     */
    private BigDecimal cost;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

---

## 3. 枚举类设计

### 3.1 AssetCategoryEnum (资产类别枚举)

```java
package com.company.oa.asset.enums;

import lombok.Getter;

/**
 * 资产类别枚举
 */
@Getter
public enum AssetCategoryEnum {

    ELECTRONIC("electronic", "电子设备"),
    FURNITURE("furniture", "家具"),
    BOOK("book", "图书"),
    OTHER("other", "其他");

    private final String code;
    private final String description;

    AssetCategoryEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AssetCategoryEnum fromCode(String code) {
        for (AssetCategoryEnum category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid asset category code: " + code);
    }
}
```

### 3.2 AssetStatusEnum (资产状态枚举)

```java
package com.company.oa.asset.enums;

import lombok.Getter;

/**
 * 资产状态枚举
 */
@Getter
public enum AssetStatusEnum {

    STOCK("stock", "在库"),
    IN_USE("in_use", "使用中"),
    BORROWED("borrowed", "借出"),
    MAINTENANCE("maintenance", "维护中"),
    SCRAPPED("scrapped", "报废");

    private final String code;
    private final String description;

    AssetStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AssetStatusEnum fromCode(String code) {
        for (AssetStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid asset status code: " + code);
    }

    /**
     * 验证状态转换是否合法
     */
    public boolean canTransitionTo(AssetStatusEnum newStatus) {
        return switch (this) {
            case STOCK -> newStatus == IN_USE || newStatus == BORROWED
                    || newStatus == MAINTENANCE || newStatus == SCRAPPED;
            case IN_USE -> newStatus == STOCK || newStatus == MAINTENANCE
                    || newStatus == SCRAPPED;
            case BORROWED -> newStatus == STOCK;
            case MAINTENANCE -> newStatus == STOCK || newStatus == IN_USE
                    || newStatus == SCRAPPED;
            case SCRAPPED -> false; // 报废状态不能转换
        };
    }
}
```

### 3.3 BorrowStatusEnum (借还状态枚举)

```java
package com.company.oa.asset.enums;

import lombok.Getter;

/**
 * 借还状态枚举
 */
@Getter
public enum BorrowStatusEnum {

    ACTIVE("active", "借出中"),
    RETURNED("returned", "已归还"),
    OVERDUE("overdue", "已超期");

    private final String code;
    private final String description;

    BorrowStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static BorrowStatusEnum fromCode(String code) {
        for (BorrowStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid borrow status code: " + code);
    }
}
```

---

## 4. DTO设计

### 4.1 请求DTO

#### AssetCreateRequest (资产创建请求)

```java
package com.company.oa.asset.dto.request;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 资产创建请求
 */
@Data
public class AssetCreateRequest {

    /**
     * 资产名称
     */
    @NotBlank(message = "资产名称不能为空")
    @Size(max = 200, message = "资产名称长度不能超过200字符")
    private String name;

    /**
     * 资产类别
     */
    @NotBlank(message = "资产类别不能为空")
    @Pattern(regexp = "^(electronic|furniture|book|other)$",
             message = "资产类别必须是electronic、furniture、book或other")
    private String category;

    /**
     * 品牌/型号
     */
    @Size(max = 200, message = "品牌/型号长度不能超过200字符")
    private String brandModel;

    /**
     * 购置日期
     */
    @NotNull(message = "购置日期不能为空")
    @PastOrPresent(message = "购置日期不能是未来日期")
    private LocalDate purchaseDate;

    /**
     * 购置金额
     */
    @NotNull(message = "购置金额不能为空")
    @DecimalMin(value = "0.01", message = "购置金额必须大于0")
    @Digits(integer = 8, fraction = 2, message = "购置金额格式不正确")
    private BigDecimal purchasePrice;

    /**
     * 使用人ID (可选)
     */
    private String userId;

    /**
     * 存放位置
     */
    @Size(max = 200, message = "存放位置长度不能超过200字符")
    private String location;

    /**
     * 资产图片URL列表
     */
    @Size(max = 10, message = "最多上传10张图片")
    private List<@URL(message = "图片URL格式不正确") String> images;

    /**
     * 备注
     */
    @Size(max = 1000, message = "备注长度不能超过1000字符")
    private String notes;
}
```

#### AssetUpdateRequest (资产更新请求)

```java
package com.company.oa.asset.dto.request;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 资产更新请求
 */
@Data
public class AssetUpdateRequest {

    /**
     * 资产ID
     */
    @NotBlank(message = "资产ID不能为空")
    private String id;

    /**
     * 资产名称
     */
    @NotBlank(message = "资产名称不能为空")
    @Size(max = 200, message = "资产名称长度不能超过200字符")
    private String name;

    /**
     * 资产类别
     */
    @NotBlank(message = "资产类别不能为空")
    @Pattern(regexp = "^(electronic|furniture|book|other)$",
             message = "资产类别必须是electronic、furniture、book或other")
    private String category;

    /**
     * 品牌/型号
     */
    @Size(max = 200, message = "品牌/型号长度不能超过200字符")
    private String brandModel;

    /**
     * 购置日期
     */
    @NotNull(message = "购置日期不能为空")
    @PastOrPresent(message = "购置日期不能是未来日期")
    private LocalDate purchaseDate;

    /**
     * 购置金额
     */
    @NotNull(message = "购置金额不能为空")
    @DecimalMin(value = "0.01", message = "购置金额必须大于0")
    @Digits(integer = 8, fraction = 2, message = "购置金额格式不正确")
    private BigDecimal purchasePrice;

    /**
     * 资产状态
     */
    @Pattern(regexp = "^(stock|in_use|borrowed|maintenance|scrapped)$",
             message = "资产状态不合法")
    private String status;

    /**
     * 使用人ID
     */
    private String userId;

    /**
     * 存放位置
     */
    @Size(max = 200, message = "存放位置长度不能超过200字符")
    private String location;

    /**
     * 资产图片URL列表
     */
    @Size(max = 10, message = "最多上传10张图片")
    private List<@URL(message = "图片URL格式不正确") String> images;

    /**
     * 备注
     */
    @Size(max = 1000, message = "备注长度不能超过1000字符")
    private String notes;

    /**
     * 乐观锁版本号
     */
    @NotNull(message = "版本号不能为空")
    private Integer version;
}
```

#### AssetQueryRequest (资产查询请求)

```java
package com.company.oa.asset.dto.request;

import lombok.Data;

/**
 * 资产查询请求
 */
@Data
public class AssetQueryRequest {

    /**
     * 页码 (从1开始)
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer pageSize = 10;

    /**
     * 关键词搜索 (资产名称/ID/品牌型号)
     */
    private String keyword;

    /**
     * 资产类别
     */
    private String category;

    /**
     * 资产状态
     */
    private String status;

    /**
     * 使用人ID
     */
    private String userId;

    /**
     * 购置日期-开始
     */
    private LocalDate purchaseDateStart;

    /**
     * 购置日期-结束
     */
    private LocalDate purchaseDateEnd;

    /**
     * 购置金额-最小值
     */
    private BigDecimal purchasePriceMin;

    /**
     * 购置金额-最大值
     */
    private BigDecimal purchasePriceMax;

    /**
     * 存放位置
     */
    private String location;

    /**
     * 排序字段
     */
    private String sortField = "created_at";

    /**
     * 排序方向 (asc/desc)
     */
    private String sortOrder = "desc";
}
```

#### AssetBorrowRequest (资产借用请求)

```java
package com.company.oa.asset.dto.request;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * 资产借用请求
 */
@Data
public class AssetBorrowRequest {

    /**
     * 资产ID
     */
    @NotBlank(message = "资产ID不能为空")
    private String assetId;

    /**
     * 借用人ID
     */
    @NotBlank(message = "借用人ID不能为空")
    private String borrowerId;

    /**
     * 借出日期
     */
    @NotNull(message = "借出日期不能为空")
    @PastOrPresent(message = "借出日期不能是未来日期")
    private LocalDate borrowDate;

    /**
     * 预计归还日期
     */
    @NotNull(message = "预计归还日期不能为空")
    @Future(message = "预计归还日期必须是未来日期")
    private LocalDate expectedReturnDate;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String notes;
}
```

#### AssetReturnRequest (资产归还请求)

```java
package com.company.oa.asset.dto.request;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * 资产归还请求
 */
@Data
public class AssetReturnRequest {

    /**
     * 资产ID
     */
    @NotBlank(message = "资产ID不能为空")
    private String assetId;

    /**
     * 实际归还日期
     */
    @NotNull(message = "归还日期不能为空")
    @PastOrPresent(message = "归还日期不能是未来日期")
    private LocalDate actualReturnDate;

    /**
     * 归还备注
     */
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String notes;
}
```

### 4.2 响应DTO

#### AssetResponse (资产响应)

```java
package com.company.oa.asset.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产响应
 */
@Data
public class AssetResponse {

    /**
     * 资产ID
     */
    private String id;

    /**
     * 资产名称
     */
    private String name;

    /**
     * 资产类别
     */
    private String category;

    /**
     * 资产类别名称
     */
    private String categoryName;

    /**
     * 品牌/型号
     */
    private String brandModel;

    /**
     * 购置日期
     */
    private LocalDate purchaseDate;

    /**
     * 购置金额
     */
    private BigDecimal purchasePrice;

    /**
     * 当前价值
     */
    private BigDecimal currentValue;

    /**
     * 折旧金额
     */
    private BigDecimal depreciationAmount;

    /**
     * 折旧率
     */
    private BigDecimal depreciationRate;

    /**
     * 资产状态
     */
    private String status;

    /**
     * 资产状态名称
     */
    private String statusName;

    /**
     * 使用人ID
     */
    private String userId;

    /**
     * 使用人姓名
     */
    private String userName;

    /**
     * 使用人头像
     */
    private String userAvatar;

    /**
     * 部门ID
     */
    private String departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 存放位置
     */
    private String location;

    /**
     * 借出日期
     */
    private LocalDate borrowDate;

    /**
     * 预计归还日期
     */
    private LocalDate expectedReturnDate;

    /**
     * 实际归还日期
     */
    private LocalDate actualReturnDate;

    /**
     * 资产图片URL列表
     */
    private List<String> images;

    /**
     * 备注
     */
    private String notes;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 是否超期
     */
    private Boolean isOverdue;

    /**
     * 超期天数
     */
    private Integer overdueDays;

    /**
     * 持有年限
     */
    private Integer ownedYears;
}
```

#### AssetStatisticsResponse (资产统计响应)

```java
package com.company.oa.asset.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 资产统计响应
 */
@Data
public class AssetStatisticsResponse {

    /**
     * 总资产数量
     */
    private Long totalCount;

    /**
     * 总购置金额
     */
    private BigDecimal totalPurchaseValue;

    /**
     * 总当前价值
     */
    private BigDecimal totalCurrentValue;

    /**
     * 总折旧金额
     */
    private BigDecimal totalDepreciationAmount;

    /**
     * 按类别统计
     */
    private List<CategoryStatistics> categoryStatistics;

    /**
     * 按状态统计
     */
    private List<StatusStatistics> statusStatistics;

    /**
     * 按月份统计的折旧趋势
     */
    private List<DepreciationTrend> depreciationTrend;

    /**
     * 按月份统计的借用趋势
     */
    private List<BorrowTrend> borrowTrend;

    /**
     * 类别统计内部类
     */
    @Data
    public static class CategoryStatistics {
        private String category;
        private String categoryName;
        private Long count;
        private BigDecimal purchaseValue;
        private BigDecimal currentValue;
    }

    /**
     * 状态统计内部类
     */
    @Data
    public static class StatusStatistics {
        private String status;
        private String statusName;
        private Long count;
        private BigDecimal value;
    }

    /**
     * 折旧趋势内部类
     */
    @Data
    public static class DepreciationTrend {
        private String month;
        private BigDecimal depreciationAmount;
        private BigDecimal totalValue;
    }

    /**
     * 借用趋势内部类
     */
    @Data
    public static class BorrowTrend {
        private String month;
        private Long borrowCount;
        private Long returnCount;
    }
}
```

---

## 5. Controller层设计

### 5.1 AssetController (资产控制器)

```java
package com.company.oa.asset.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.oa.asset.dto.request.*;
import com.company.oa.asset.dto.response.AssetResponse;
import com.company.oa.asset.dto.response.AssetStatisticsResponse;
import com.company.oa.asset.service.AssetService;
import com.company.oa.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 资产管理控制器
 */
@Tag(name = "资产管理", description = "资产管理相关接口")
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@Validated
public class AssetController {

    private final AssetService assetService;

    /**
     * 分页查询资产列表
     */
    @Operation(summary = "分页查询资产列表")
    @GetMapping
    public Result<Page<AssetResponse>> getAssetList(AssetQueryRequest request) {
        Page<AssetResponse> page = assetService.getAssetList(request);
        return Result.success(page);
    }

    /**
     * 根据ID查询资产详情
     */
    @Operation(summary = "查询资产详情")
    @GetMapping("/{id}")
    public Result<AssetResponse> getAssetById(@PathVariable String id) {
        AssetResponse response = assetService.getAssetById(id);
        return Result.success(response);
    }

    /**
     * 创建资产
     */
    @Operation(summary = "创建资产")
    @PostMapping
    @PreAuthorize("hasAuthority('asset:create')")
    public Result<AssetResponse> createAsset(@Valid @RequestBody AssetCreateRequest request) {
        AssetResponse response = assetService.createAsset(request);
        return Result.success(response);
    }

    /**
     * 更新资产
     */
    @Operation(summary = "更新资产")
    @PutMapping
    @PreAuthorize("hasAuthority('asset:update')")
    public Result<AssetResponse> updateAsset(@Valid @RequestBody AssetUpdateRequest request) {
        AssetResponse response = assetService.updateAsset(request);
        return Result.success(response);
    }

    /**
     * 删除资产
     */
    @Operation(summary = "删除资产")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('asset:delete')")
    public Result<Void> deleteAsset(@PathVariable String id) {
        assetService.deleteAsset(id);
        return Result.success();
    }

    /**
     * 获取资产统计信息
     */
    @Operation(summary = "获取资产统计信息")
    @GetMapping("/statistics")
    public Result<AssetStatisticsResponse> getStatistics() {
        AssetStatisticsResponse response = assetService.getStatistics();
        return Result.success(response);
    }

    /**
     * 借用资产
     */
    @Operation(summary = "借用资产")
    @PostMapping("/{id}/borrow")
    @PreAuthorize("hasAuthority('asset:borrow')")
    public Result<Void> borrowAsset(@PathVariable String id,
                                    @Valid @RequestBody AssetBorrowRequest request) {
        request.setAssetId(id);
        assetService.borrowAsset(request);
        return Result.success();
    }

    /**
     * 归还资产
     */
    @Operation(summary = "归还资产")
    @PostMapping("/{id}/return")
    @PreAuthorize("hasAuthority('asset:return')")
    public Result<Void> returnAsset(@PathVariable String id,
                                    @Valid @RequestBody AssetReturnRequest request) {
        request.setAssetId(id);
        assetService.returnAsset(request);
        return Result.success();
    }

    /**
     * 获取资产借用历史
     */
    @Operation(summary = "获取资产借用历史")
    @GetMapping("/{id}/borrow-history")
    public Result<Page<AssetBorrowRecordVO>> getBorrowHistory(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 实现逻辑
        return Result.success();
    }
}
```

---

## 6. Service层设计

### 6.1 AssetService接口

```java
package com.company.oa.asset.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.oa.asset.dto.request.*;
import com.company.oa.asset.dto.response.AssetResponse;
import com.company.oa.asset.dto.response.AssetStatisticsResponse;

/**
 * 资产服务接口
 */
public interface AssetService {

    /**
     * 分页查询资产列表
     */
    Page<AssetResponse> getAssetList(AssetQueryRequest request);

    /**
     * 根据ID查询资产详情
     */
    AssetResponse getAssetById(String id);

    /**
     * 创建资产
     */
    AssetResponse createAsset(AssetCreateRequest request);

    /**
     * 更新资产
     */
    AssetResponse updateAsset(AssetUpdateRequest request);

    /**
     * 删除资产
     */
    void deleteAsset(String id);

    /**
     * 获取资产统计信息
     */
    AssetStatisticsResponse getStatistics();

    /**
     * 借用资产
     */
    void borrowAsset(AssetBorrowRequest request);

    /**
     * 归还资产
     */
    void returnAsset(AssetReturnRequest request);

    /**
     * 计算资产当前价值(折旧)
     */
    void calculateAssetCurrentValue(String assetId);

    /**
     * 批量计算所有资产当前价值
     */
    void batchCalculateAllAssetCurrentValue();
}
```

### 6.2 AssetServiceImpl实现类

```java
package com.company.oa.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.oa.asset.dto.request.*;
import com.company.oa.asset.dto.response.AssetResponse;
import com.company.oa.asset.dto.response.AssetStatisticsResponse;
import com.company.oa.asset.entity.Asset;
import com.company.oa.asset.enums.AssetStatusEnum;
import com.company.oa.asset.mapper.AssetMapper;
import com.company.oa.asset.service.AssetService;
import com.company.oa.asset.util.AssetDepreciationUtil;
import com.company.oa.common.exception.BusinessException;
import com.company.oa.system.entity.Employee;
import com.company.oa.system.feign.EmployeeFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资产服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetMapper assetMapper;
    private final EmployeeFeignClient employeeFeignClient;

    @Override
    public Page<AssetResponse> getAssetList(AssetQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Asset> wrapper = buildQueryWrapper(request);

        // 分页查询
        Page<Asset> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<Asset> assetPage = assetMapper.selectPage(page, wrapper);

        // 转换为响应对象
        Page<AssetResponse> responsePage = new Page<>();
        responsePage.setRecords(assetPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));
        responsePage.setTotal(assetPage.getTotal());
        responsePage.setCurrent(assetPage.getCurrent());
        responsePage.setSize(assetPage.getSize());

        return responsePage;
    }

    @Override
    public AssetResponse getAssetById(String id) {
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }
        return convertToResponse(asset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetResponse createAsset(AssetCreateRequest request) {
        // 验证借用人是否存在
        if (request.getUserId() != null) {
            validateEmployeeExists(request.getUserId());
        }

        // 生成资产ID (格式: ASSET+6位序号)
        String assetId = generateAssetId();

        // 创建资产实体
        Asset asset = new Asset();
        BeanUtils.copyProperties(request, asset);
        asset.setId(assetId);
        asset.setStatus(AssetStatusEnum.STOCK.getCode());
        asset.setCurrentValue(request.getPurchasePrice());

        // 计算折旧
        calculateAssetCurrentValue(asset);

        // 插入数据库
        assetMapper.insert(asset);

        log.info("创建资产成功, assetId: {}", assetId);

        return getAssetById(assetId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetResponse updateAsset(AssetUpdateRequest request) {
        // 查询现有资产
        Asset existingAsset = assetMapper.selectById(request.getId());
        if (existingAsset == null) {
            throw new BusinessException("资产不存在");
        }

        // 验证乐观锁
        if (!existingAsset.getVersion().equals(request.getVersion())) {
            throw new BusinessException("数据已被修改,请刷新后重试");
        }

        // 验证状态转换是否合法
        if (request.getStatus() != null) {
            AssetStatusEnum currentStatus = AssetStatusEnum.fromCode(existingAsset.getStatus());
            AssetStatusEnum newStatus = AssetStatusEnum.fromCode(request.getStatus());
            if (!currentStatus.canTransitionTo(newStatus)) {
                throw new BusinessException("不能从" + currentStatus.getDescription()
                        + "状态转换到" + newStatus.getDescription() + "状态");
            }
        }

        // 验证借用人是否存在
        if (request.getUserId() != null) {
            validateEmployeeExists(request.getUserId());
        }

        // 更新资产信息
        Asset asset = new Asset();
        BeanUtils.copyProperties(request, asset);

        // 重新计算折旧
        calculateAssetCurrentValue(asset);

        // 更新数据库
        int rows = assetMapper.updateById(asset);
        if (rows == 0) {
            throw new BusinessException("更新资产失败");
        }

        log.info("更新资产成功, assetId: {}", request.getId());

        return getAssetById(request.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAsset(String id) {
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }

        // 检查资产是否可以删除 (只有在库状态才能删除)
        if (!AssetStatusEnum.STOCK.getCode().equals(asset.getStatus())) {
            throw new BusinessException("只有" + AssetStatusEnum.STOCK.getDescription()
                    + "状态的资产才能删除");
        }

        // 逻辑删除
        assetMapper.deleteById(id);

        log.info("删除资产成功, assetId: {}", id);
    }

    @Override
    public AssetStatisticsResponse getStatistics() {
        // 调用Mapper层统计方法
        return assetMapper.getStatistics();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void borrowAsset(AssetBorrowRequest request) {
        // 查询资产
        Asset asset = assetMapper.selectById(request.getAssetId());
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }

        // 验证资产状态
        AssetStatusEnum currentStatus = AssetStatusEnum.fromCode(asset.getStatus());
        if (!currentStatus.canTransitionTo(AssetStatusEnum.BORROWED)) {
            throw new BusinessException("资产当前状态不能借出");
        }

        // 验证借用人
        validateEmployeeExists(request.getBorrowerId());

        // 验证归还日期
        if (request.getExpectedReturnDate().isBefore(request.getBorrowDate())) {
            throw new BusinessException("预计归还日期必须晚于借出日期");
        }

        // 更新资产状态
        asset.setStatus(AssetStatusEnum.BORROWED.getCode());
        asset.setUserId(request.getBorrowerId());
        asset.setBorrowDate(request.getBorrowDate());
        asset.setExpectedReturnDate(request.getExpectedReturnDate());
        assetMapper.updateById(asset);

        // 创建借用记录
        AssetBorrowRecord record = new AssetBorrowRecord();
        record.setAssetId(request.getAssetId());
        record.setAssetName(asset.getName());
        record.setBorrowerId(request.getBorrowerId());
        record.setBorrowDate(request.getBorrowDate());
        record.setExpectedReturnDate(request.getExpectedReturnDate());
        record.setStatus(BorrowStatusEnum.ACTIVE.getCode());
        record.setNotes(request.getNotes());
        assetBorrowRecordMapper.insert(record);

        log.info("资产借出成功, assetId: {}, borrowerId: {}",
                request.getAssetId(), request.getBorrowerId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnAsset(AssetReturnRequest request) {
        // 查询资产
        Asset asset = assetMapper.selectById(request.getAssetId());
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }

        // 验证资产状态
        if (!AssetStatusEnum.BORROWED.getCode().equals(asset.getStatus())) {
            throw new BusinessException("资产当前不是借出状态");
        }

        // 查询有效的借用记录
        LambdaQueryWrapper<AssetBorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetBorrowRecord::getAssetId, request.getAssetId())
                .eq(AssetBorrowRecord::getStatus, BorrowStatusEnum.ACTIVE.getCode())
                .orderByDesc(AssetBorrowRecord::getBorrowDate)
                .last("LIMIT 1");

        AssetBorrowRecord record = assetBorrowRecordMapper.selectOne(wrapper);
        if (record == null) {
            throw new BusinessException("未找到有效的借用记录");
        }

        // 更新借用记录
        record.setActualReturnDate(request.getActualReturnDate());
        record.setStatus(BorrowStatusEnum.RETURNED.getCode());
        record.setNotes(request.getNotes());
        assetBorrowRecordMapper.updateById(record);

        // 更新资产状态
        asset.setStatus(AssetStatusEnum.STOCK.getCode());
        asset.setUserId(null);
        asset.setActualReturnDate(request.getActualReturnDate());
        assetMapper.updateById(asset);

        log.info("资产归还成功, assetId: {}", request.getAssetId());
    }

    @Override
    public void calculateAssetCurrentValue(String assetId) {
        Asset asset = assetMapper.selectById(assetId);
        if (asset != null) {
            calculateAssetCurrentValue(asset);
            assetMapper.updateById(asset);
        }
    }

    @Override
    public void batchCalculateAllAssetCurrentValue() {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Asset::getStatus, AssetStatusEnum.SCRAPPED.getCode());

        List<Asset> assets = assetMapper.selectList(wrapper);
        for (Asset asset : assets) {
            calculateAssetCurrentValue(asset);
            assetMapper.updateById(asset);
        }

        log.info("批量计算资产折旧完成, 共{}个资产", assets.size());
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Asset> buildQueryWrapper(AssetQueryRequest request) {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (request.getKeyword() != null) {
            wrapper.and(w -> w.like(Asset::getName, request.getKeyword())
                    .or().like(Asset::getId, request.getKeyword())
                    .or().like(Asset::getBrandModel, request.getKeyword()));
        }

        // 类别过滤
        wrapper.eq(request.getCategory() != null, Asset::getCategory, request.getCategory());

        // 状态过滤
        wrapper.eq(request.getStatus() != null, Asset::getStatus, request.getStatus());

        // 使用人过滤
        wrapper.eq(request.getUserId() != null, Asset::getUserId, request.getUserId());

        // 购置日期范围
        wrapper.ge(request.getPurchaseDateStart() != null,
                Asset::getPurchaseDate, request.getPurchaseDateStart());
        wrapper.le(request.getPurchaseDateEnd() != null,
                Asset::getPurchaseDate, request.getPurchaseDateEnd());

        // 购置金额范围
        wrapper.ge(request.getPurchasePriceMin() != null,
                Asset::getPurchasePrice, request.getPurchasePriceMin());
        wrapper.le(request.getPurchasePriceMax() != null,
                Asset::getPurchasePrice, request.getPurchasePriceMax());

        // 存放位置
        wrapper.eq(request.getLocation() != null, Asset::getLocation, request.getLocation());

        // 排序
        if ("asc".equalsIgnoreCase(request.getSortOrder())) {
            wrapper.orderByAsc(Asset::getCreatedAt);
        } else {
            wrapper.orderByDesc(Asset::getCreatedAt);
        }

        return wrapper;
    }

    /**
     * 生成资产ID
     */
    private String generateAssetId() {
        // 查询当前最大ID
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Asset::getId).last("LIMIT 1");
        Asset lastAsset = assetMapper.selectOne(wrapper);

        int sequence = 1;
        if (lastAsset != null && lastAsset.getId().startsWith("ASSET")) {
            String lastSequence = lastAsset.getId().substring(5);
            sequence = Integer.parseInt(lastSequence) + 1;
        }

        return String.format("ASSET%06d", sequence);
    }

    /**
     * 计算资产当前价值(折旧)
     */
    private void calculateAssetCurrentValue(Asset asset) {
        BigDecimal currentValue = AssetDepreciationUtil.calculateDepreciation(
                asset.getCategory(),
                asset.getPurchaseDate(),
                asset.getPurchasePrice()
        );
        asset.setCurrentValue(currentValue);
    }

    /**
     * 验证员工是否存在
     */
    private void validateEmployeeExists(String employeeId) {
        Employee employee = employeeFeignClient.getEmployeeById(employeeId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
    }

    /**
     * 转换为响应对象
     */
    private AssetResponse convertToResponse(Asset asset) {
        AssetResponse response = new AssetResponse();
        BeanUtils.copyProperties(asset, response);

        // 计算折旧信息
        BigDecimal depreciationAmount = asset.getPurchasePrice()
                .subtract(asset.getCurrentValue());
        response.setDepreciationAmount(depreciationAmount);

        BigDecimal depreciationRate = asset.getPurchasePrice().compareTo(BigDecimal.ZERO) > 0
                ? depreciationAmount.divide(asset.getPurchasePrice(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;
        response.setDepreciationRate(depreciationRate);

        // 计算持有年限
        int ownedYears = Period.between(asset.getPurchaseDate(), LocalDate.now()).getYears();
        response.setOwnedYears(ownedYears);

        // 判断是否超期
        if (asset.getExpectedReturnDate() != null
                && AssetStatusEnum.BORROWED.getCode().equals(asset.getStatus())) {
            boolean isOverdue = LocalDate.now().isAfter(asset.getExpectedReturnDate());
            response.setIsOverdue(isOverdue);
            if (isOverdue) {
                int overdueDays = Period.between(asset.getExpectedReturnDate(), LocalDate.now()).getDays();
                response.setOverdueDays(overdueDays);
            }
        }

        // 获取借用人信息
        if (asset.getUserId() != null) {
            try {
                Employee employee = employeeFeignClient.getEmployeeById(asset.getUserId());
                if (employee != null) {
                    response.setUserName(employee.getName());
                    response.setUserAvatar(employee.getAvatar());
                    response.setDepartmentId(employee.getDepartmentId());
                    // 获取部门名称
                    if (employee.getDepartmentId() != null) {
                        Department department = departmentFeignClient.getDepartmentById(employee.getDepartmentId());
                        if (department != null) {
                            response.setDepartmentName(department.getName());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("获取员工信息失败, userId: {}", asset.getUserId(), e);
            }
        }

        return response;
    }
}
```

---

## 7. Mapper层设计

### 7.1 AssetMapper

```java
package com.company.oa.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.oa.asset.dto.response.AssetStatisticsResponse;
import com.company.oa.asset.entity.Asset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 资产Mapper接口
 */
@Mapper
public interface AssetMapper extends BaseMapper<Asset> {

    /**
     * 获取资产统计信息
     */
    @Select("SELECT " +
            "  COUNT(*) AS totalCount, " +
            "  SUM(purchase_price) AS totalPurchaseValue, " +
            "  SUM(current_value) AS totalCurrentValue " +
            "FROM biz_asset " +
            "WHERE is_deleted = 0")
    AssetStatisticsResponse getStatistics();
}
```

### 7.2 AssetBorrowRecordMapper

```java
package com.company.oa.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.oa.asset.entity.AssetBorrowRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资产借还记录Mapper接口
 */
@Mapper
public interface AssetBorrowRecordMapper extends BaseMapper<AssetBorrowRecord> {
}
```

### 7.3 AssetMaintenanceMapper

```java
package com.company.oa.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.oa.asset.entity.AssetMaintenance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资产维护记录Mapper接口
 */
@Mapper
public interface AssetMaintenanceMapper extends BaseMapper<AssetMaintenance> {
}
```

---

## 8. 工具类设计

### 8.1 AssetDepreciationUtil (资产折旧工具类)

```java
package com.company.oa.asset.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

/**
 * 资产折旧计算工具类
 */
public class AssetDepreciationUtil {

    /**
     * 折旧配置
     */
    private static final class DepreciationConfig {
        /**
         * 电子设备: 3年折旧, 每年33%
         */
        static final BigDecimal ELECTRONIC_RATE = new BigDecimal("0.33");
        static final int ELECTRONIC_YEARS = 3;

        /**
         * 家具: 5年折旧, 每年20%
         */
        static final BigDecimal FURNITURE_RATE = new BigDecimal("0.20");
        static final int FURNITURE_YEARS = 5;

        /**
         * 图书: 不折旧
         */
        static final BigDecimal BOOK_RATE = BigDecimal.ZERO;
        static final int BOOK_YEARS = 0;

        /**
         * 其他: 3年折旧, 每年33%
         */
        static final BigDecimal OTHER_RATE = new BigDecimal("0.33");
        static final int OTHER_YEARS = 3;
    }

    /**
     * 计算资产折旧后的当前价值
     *
     * @param category     资产类别
     * @param purchaseDate 购置日期
     * @param purchasePrice 购置价格
     * @return 当前价值
     */
    public static BigDecimal calculateDepreciation(String category,
                                                    LocalDate purchaseDate,
                                                    BigDecimal purchasePrice) {
        if (purchasePrice == null || purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (purchaseDate == null) {
            return purchasePrice;
        }

        // 计算已使用年限
        int years = Period.between(purchaseDate, LocalDate.now()).getYears();

        return switch (category) {
            case "electronic" -> calculateDepreciationByRate(
                    purchasePrice, years, DepreciationConfig.ELECTRONIC_RATE
            );
            case "furniture" -> calculateDepreciationByRate(
                    purchasePrice, years, DepreciationConfig.FURNITURE_RATE
            );
            case "book" -> purchasePrice; // 图书不折旧
            case "other" -> calculateDepreciationByRate(
                    purchasePrice, years, DepreciationConfig.OTHER_RATE
            );
            default -> purchasePrice;
        };
    }

    /**
     * 按折旧率计算当前价值
     *
     * @param purchasePrice 购置价格
     * @param years        使用年限
     * @param rate         年折旧率
     * @return 当前价值
     */
    private static BigDecimal calculateDepreciationByRate(BigDecimal purchasePrice,
                                                          int years,
                                                          BigDecimal rate) {
        if (years <= 0) {
            return purchasePrice;
        }

        // 公式: current_value = purchase_price * (1 - rate) ^ years
        BigDecimal remainingRate = BigDecimal.ONE.subtract(rate);
        BigDecimal depreciationFactor = remainingRate.pow(years);

        BigDecimal currentValue = purchasePrice.multiply(depreciationFactor)
                .setScale(2, RoundingMode.HALF_UP);

        // 确保不为负数
        return currentValue.max(BigDecimal.ZERO);
    }
}
```

---

## 9. 定时任务设计

### 9.1 AssetScheduledTask (资产定时任务)

```java
package com.company.oa.asset.task;

import com.company.oa.asset.service.AssetService;
import com.company.oa.asset.service.AssetBorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 资产模块定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssetScheduledTask {

    private final AssetService assetService;
    private final AssetBorrowService assetBorrowService;

    /**
     * 自动计算折旧
     * 每月1号凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void calculateDepreciation() {
        log.info("开始执行资产折旧计算任务");
        try {
            assetService.batchCalculateAllAssetCurrentValue();
            log.info("资产折旧计算任务执行成功");
        } catch (Exception e) {
            log.error("资产折旧计算任务执行失败", e);
        }
    }

    /**
     * 超期借用检查
     * 每天早上9点执行
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkOverdueBorrows() {
        log.info("开始执行超期借用检查任务");
        try {
            assetBorrowService.checkAndUpdateOverdueRecords();
            log.info("超期借用检查任务执行成功");
        } catch (Exception e) {
            log.error("超期借用检查任务执行失败", e);
        }
    }
}
```

---

## 10. 配置类设计

### 10.1 AssetConfig (资产配置类)

```java
package com.company.oa.asset.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 资产模块配置类
 */
@Configuration
@EnableScheduling
public class AssetConfig {
    // 可以在这里添加资产模块相关的配置
}
```

---

## 11. 异常处理

### 11.1 业务异常定义

```java
package com.company.oa.asset.exception;

import com.company.oa.common.exception.BusinessException;

/**
 * 资产模块业务异常
 */
public class AssetException extends BusinessException {

    public static final int ASSET_NOT_FOUND = 10001;
    public static final int ASSET_STATUS_INVALID = 10002;
    public static final int ASSET_CANNOT_BORROW = 10003;
    public static final int ASSET_ALREADY_BORROWED = 10004;
    public static final int ASSET_RETURN_INVALID = 10005;
    public static final int ASSET_DELETE_INVALID = 10006;

    public AssetException(int code, String message) {
        super(code, message);
    }

    public static AssetException assetNotFound() {
        return new AssetException(ASSET_NOT_FOUND, "资产不存在");
    }

    public static AssetException assetStatusInvalid(String current, String target) {
        return new AssetException(ASSET_STATUS_INVALID,
                "资产当前状态为[" + current + "], 不能转换为[" + target + "]");
    }

    public static AssetException assetCannotBorrow() {
        return new AssetException(ASSET_CANNOT_BORROW, "资产当前状态不能借出");
    }

    public static AssetException assetDeleteInvalid(String status) {
        return new AssetException(ASSET_DELETE_INVALID,
                "只有[在库]状态的资产才能删除, 当前状态: " + status);
    }
}
```

---

## 12. 业务逻辑约束检查

### 12.1 外键约束检查

```java
/**
 * 验证员工是否存在
 */
private void validateEmployeeExists(String employeeId) {
    if (employeeId == null || employeeId.trim().isEmpty()) {
        throw new BusinessException("员工ID不能为空");
    }

    Employee employee = employeeFeignClient.getEmployeeById(employeeId);
    if (employee == null) {
        throw new BusinessException("员工不存在: " + employeeId);
    }

    // 检查员工是否在职
    if ("terminated".equals(employee.getStatus())) {
        throw new BusinessException("员工已离职, 不能操作资产: " + employeeId);
    }
}

/**
 * 验证部门是否存在
 */
private void validateDepartmentExists(String departmentId) {
    if (departmentId != null && !departmentId.trim().isEmpty()) {
        Department department = departmentFeignClient.getDepartmentById(departmentId);
        if (department == null) {
            throw new BusinessException("部门不存在: " + departmentId);
        }
    }
}
```

### 12.2 状态转换约束检查

```java
/**
 * 验证资产状态转换是否合法
 */
private void validateAssetStatusTransition(Asset asset, String newStatus) {
    AssetStatusEnum currentStatus = AssetStatusEnum.fromCode(asset.getStatus());
    AssetStatusEnum targetStatus = AssetStatusEnum.fromCode(newStatus);

    if (!currentStatus.canTransitionTo(targetStatus)) {
        throw AssetException.assetStatusInvalid(
                currentStatus.getDescription(),
                targetStatus.getDescription()
        );
    }

    // 额外的业务规则检查
    if (targetStatus == AssetStatusEnum.BORROWED) {
        // 借出时必须指定借用人
        if (asset.getUserId() == null) {
            throw new BusinessException("借出资产必须指定借用人");
        }
    }

    if (targetStatus == AssetStatusEnum.SCRAPPED) {
        // 报废时资产当前价值应该为0或很小
        if (asset.getCurrentValue().compareTo(new BigDecimal("100")) > 0) {
            log.warn("资产报废时当前价值仍然较高: {}元, assetId: {}",
                    asset.getCurrentValue(), asset.getId());
        }
    }
}
```

### 12.3 数据完整性约束检查

```java
/**
 * 验证资产数据完整性
 */
private void validateAssetData(AssetCreateRequest request) {
    // 验证购置日期不能是未来日期
    if (request.getPurchaseDate().isAfter(LocalDate.now())) {
        throw new BusinessException("购置日期不能是未来日期");
    }

    // 验证购置金额必须大于0
    if (request.getPurchasePrice().compareTo(BigDecimal.ZERO) <= 0) {
        throw new BusinessException("购置金额必须大于0");
    }

    // 验证图片URL数量
    if (request.getImages() != null && request.getImages().size() > 10) {
        throw new BusinessException("最多上传10张图片");
    }

    // 验证资产名称
    if (request.getName() == null || request.getName().trim().isEmpty()) {
        throw new BusinessException("资产名称不能为空");
    }

    // 验证资产类别
    try {
        AssetCategoryEnum.fromCode(request.getCategory());
    } catch (IllegalArgumentException e) {
        throw new BusinessException("资产类别不合法");
    }
}

/**
 * 验证借用数据完整性
 */
private void validateBorrowData(AssetBorrowRequest request, Asset asset) {
    // 验证归还日期必须晚于借出日期
    if (request.getExpectedReturnDate().isBefore(request.getBorrowDate())
            || request.getExpectedReturnDate().isEqual(request.getBorrowDate())) {
        throw new BusinessException("预计归还日期必须晚于借出日期");
    }

    // 验证借用期限 (最多90天)
    long borrowDays = ChronoUnit.DAYS.between(
            request.getBorrowDate(),
            request.getExpectedReturnDate()
    );
    if (borrowDays > 90) {
        throw new BusinessException("借用期限不能超过90天");
    }

    // 验证资产是否已被借出
    if (AssetStatusEnum.BORROWED.getCode().equals(asset.getStatus())) {
        throw new BusinessException("资产已被借出, 不能重复借出");
    }
}

/**
 * 验证归还数据完整性
 */
private void validateReturnData(AssetReturnRequest request, Asset asset) {
    // 验证资产是否处于借出状态
    if (!AssetStatusEnum.BORROWED.getCode().equals(asset.getStatus())) {
        throw new BusinessException("资产当前不是借出状态, 不能归还");
    }

    // 验证归还日期必须晚于借出日期
    if (request.getActualReturnDate().isBefore(asset.getBorrowDate())) {
        throw new BusinessException("归还日期不能早于借出日期");
    }
}
```

### 12.4 借用记录约束检查

```java
/**
 * 检查是否有未归还的借用记录
 */
private boolean hasActiveBorrowRecord(String assetId) {
    LambdaQueryWrapper<AssetBorrowRecord> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(AssetBorrowRecord::getAssetId, assetId)
            .eq(AssetBorrowRecord::getStatus, BorrowStatusEnum.ACTIVE.getCode());

    Long count = assetBorrowRecordMapper.selectCount(wrapper);
    return count > 0;
}

/**
 * 检查员工借用数量限制
 */
private void checkEmployeeBorrowLimit(String employeeId) {
    LambdaQueryWrapper<AssetBorrowRecord> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(AssetBorrowRecord::getBorrowerId, employeeId)
            .eq(AssetBorrowRecord::getStatus, BorrowStatusEnum.ACTIVE.getCode());

    Long count = assetBorrowRecordMapper.selectCount(wrapper);

    // 每个员工最多同时借5件资产
    if (count >= 5) {
        throw new BusinessException("该员工已借用5件资产, 达到借用上限");
    }
}
```

---

## 13. MyBatis XML Mapper示例

### 13.1 AssetMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.company.oa.asset.mapper.AssetMapper">

    <!-- 结果映射 -->
    <resultMap id="AssetVOMap" type="com.company.oa.asset.vo.AssetVO">
        <id property="id" column="id"/>
        <result property="name" column="name"/>
        <result property="category" column="category"/>
        <result property="brandModel" column="brand_model"/>
        <result property="purchaseDate" column="purchase_date"/>
        <result property="purchasePrice" column="purchase_price"/>
        <result property="currentValue" column="current_value"/>
        <result property="status" column="status"/>
        <result property="userId" column="user_id"/>
        <result property="location" column="location"/>
        <result property="borrowDate" column="borrow_date"/>
        <result property="expectedReturnDate" column="expected_return_date"/>
        <result property="actualReturnDate" column="actual_return_date"/>
        <result property="images" column="images"
                typeHandler="com.company.oa.common.handler.JsonListTypeHandler"/>
        <result property="notes" column="notes"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>

        <!-- 关联查询员工信息 -->
        <association property="userName" column="user_id"
                     javaType="string"
                     select="com.company.oa.system.mapper.EmployeeMapper.selectNameById"/>

        <!-- 关联查询部门信息 -->
        <association property="departmentName" column="department_id"
                     javaType="string"
                     select="com.company.oa.system.mapper.DepartmentMapper.selectNameById"/>
    </resultMap>

    <!-- 查询资产列表 -->
    <select id="selectAssetPage" resultMap="AssetVOMap">
        SELECT
            a.*,
            e.name AS user_name,
            d.name AS department_name
        FROM biz_asset a
        LEFT JOIN sys_employee e ON a.user_id = e.id
        LEFT JOIN sys_department d ON e.department_id = d.id
        WHERE a.is_deleted = 0
        <if test="keyword != null and keyword != ''">
            AND (
                a.name LIKE CONCAT('%', #{keyword}, '%')
                OR a.id LIKE CONCAT('%', #{keyword}, '%')
                OR a.brand_model LIKE CONCAT('%', #{keyword}, '%')
            )
        </if>
        <if test="category != null and category != ''">
            AND a.category = #{category}
        </if>
        <if test="status != null and status != ''">
            AND a.status = #{status}
        </if>
        <if test="userId != null and userId != ''">
            AND a.user_id = #{userId}
        </if>
        <if test="purchaseDateStart != null">
            AND a.purchase_date >= #{purchaseDateStart}
        </if>
        <if test="purchaseDateEnd != null">
            AND a.purchase_date &lt;= #{purchaseDateEnd}
        </if>
        <if test="purchasePriceMin != null">
            AND a.purchase_price >= #{purchasePriceMin}
        </if>
        <if test="purchasePriceMax != null">
            AND a.purchase_price &lt;= #{purchasePriceMax}
        </if>
        <if test="location != null and location != ''">
            AND a.location LIKE CONCAT('%', #{location}, '%')
        </if>
        ORDER BY ${sortField} ${sortOrder}
        LIMIT #{pageSize} OFFSET ${(pageNum - 1) * pageSize}
    </select>

    <!-- 统计查询 -->
    <select id="selectStatistics"
            resultType="com.company.oa.asset.dto.response.AssetStatisticsResponse">
        SELECT
            COUNT(*) AS totalCount,
            SUM(CASE WHEN category = 'electronic' THEN 1 ELSE 0 END) AS electronicCount,
            SUM(CASE WHEN category = 'furniture' THEN 1 ELSE 0 END) AS furnitureCount,
            SUM(CASE WHEN category = 'book' THEN 1 ELSE 0 END) AS bookCount,
            SUM(CASE WHEN category = 'other' THEN 1 ELSE 0 END) AS otherCount,
            SUM(CASE WHEN status = 'stock' THEN 1 ELSE 0 END) AS stockCount,
            SUM(CASE WHEN status = 'in_use' THEN 1 ELSE 0 END) AS inUseCount,
            SUM(CASE WHEN status = 'borrowed' THEN 1 ELSE 0 END) AS borrowedCount,
            SUM(CASE WHEN status = 'maintenance' THEN 1 ELSE 0 END) AS maintenanceCount,
            SUM(CASE WHEN status = 'scrapped' THEN 1 ELSE 0 END) AS scrappedCount,
            SUM(purchase_price) AS totalPurchaseValue,
            SUM(current_value) AS totalCurrentValue
        FROM biz_asset
        WHERE is_deleted = 0
    </select>

</mapper>
```

---

## 14. 接口测试示例

### 14.1 创建资产

```bash
POST /api/assets
Content-Type: application/json

{
  "name": "MacBook Pro 16寸",
  "category": "electronic",
  "brandModel": "Apple M3 Max",
  "purchaseDate": "2024-01-15",
  "purchasePrice": 24999.00,
  "location": "A座3楼办公室",
  "images": [
    "https://example.com/asset1.jpg",
    "https://example.com/asset2.jpg"
  ],
  "notes": "研发部专用设备"
}
```

### 14.2 查询资产列表

```bash
GET /api/assets?pageNum=1&pageSize=10&category=electronic&status=stock
```

### 14.3 借用资产

```bash
POST /api/assets/ASSET000001/borrow
Content-Type: application/json

{
  "borrowerId": "EMP000001",
  "borrowDate": "2024-01-20",
  "expectedReturnDate": "2024-02-20",
  "notes": "项目使用"
}
```

### 14.4 归还资产

```bash
POST /api/assets/ASSET000001/return
Content-Type: application/json

{
  "actualReturnDate": "2024-02-18",
  "notes": "项目完成,提前归还"
}
```

---

## 15. 部署配置

### 15.1 application.yml配置

```yaml
# 资产模块配置
oa:
  asset:
    # 折旧配置
    depreciation:
      electronic-rate: 0.33  # 电子设备年折旧率
      furniture-rate: 0.20   # 家具年折旧率
      book-rate: 0.00        # 图书年折旧率
      other-rate: 0.33       # 其他年折旧率

    # 借用配置
    borrow:
      max-days: 90           # 最大借用天数
      max-per-employee: 5    # 每个员工最大借用数量

    # 图片配置
    image:
      max-count: 10          # 最大图片数量
      max-size: 5242880      # 最大图片大小(5MB)

    # 定时任务配置
    scheduled:
      depreciation-cron: "0 0 1 1 * ?"     # 折旧计算cron表达式
      overdue-check-cron: "0 0 9 * * ?"    # 超期检查cron表达式
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-17
