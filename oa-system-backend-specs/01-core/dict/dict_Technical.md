# 数据字典后端技术实现规范

> **对应前端规范**: [dict_Technical.md](../../../oa-system-frontend-specs/core/dict/dict_Technical.md)
> **数据库规范**: [dict.md](../../../oa-system-database-specs/01-core/dict.md)
> **技术栈**: Spring Boot 3.x + MyBatis Plus + MySQL 8.0
> **版本**: v1.0.0
> **创建日期**: 2026-01-14

---

## 📋 目录

- [1. 项目结构](#1-项目结构)
- [2. 实体类设计](#2-实体类设计)
- [3. DTO设计](#3-dto设计)
- [4. VO设计](#4-vo设计)
- [5. Mapper接口](#5-mapper接口)
- [6. Service层设计](#6-service层设计)
- [7. Controller层设计](#7-controller层设计)
- [8. 业务逻辑实现](#8-业务逻辑实现)
- [9. 数据验证](#9-数据验证)
- [10. 权限控制](#10-权限控制)
- [11. 异常处理](#11-异常处理)
- [12. 缓存策略](#12-缓存策略)

---

## 1. 项目结构

```
oa-system-backend/src/main/java/com/example/oa_system_backend/module/dict/
├── controller/
│   └── DictController.java              # 数据字典控制器
├── service/
│   ├── DictService.java                 # 数据字典服务接口
│   └── impl/
│       └── DictServiceImpl.java         # 数据字典服务实现
├── mapper/
│   ├── DictTypeMapper.java              # 字典类型Mapper
│   └── DictItemMapper.java              # 字典项Mapper
├── entity/
│   ├── DictType.java                    # 字典类型实体
│   └── DictItem.java                    # 字典项实体
├── dto/
│   ├── DictTypeCreateRequest.java       # 创建字典类型DTO
│   ├── DictTypeUpdateRequest.java       # 更新字典类型DTO
│   ├── DictTypeQueryRequest.java        # 查询字典类型DTO
│   ├── DictItemCreateRequest.java       # 创建字典项DTO
│   ├── DictItemUpdateRequest.java       # 更新字典项DTO
│   ├── DictItemQueryRequest.java        # 查询字典项DTO
│   ├── DictItemSortUpdateRequest.java   # 排序更新DTO
│   └── DictBatchOperationRequest.java   # 批量操作DTO
└── vo/
    ├── DictTypeVO.java                  # 字典类型视图对象
    ├── DictItemVO.java                  # 字典项视图对象
    ├── DictTreeVO.java                  # 字典树视图对象
    └── DictDataVO.java                  # 字典数据视图对象
```

---

## 2. 实体类设计

### 2.1 DictType实体类

```java
package com.example.oa_system_backend.module.dict.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典类型实体类
 * 对应表: sys_dict_type
 */
@Data
@TableName("sys_dict_type")
public class DictType {

    /**
     * 主键: 字典类型ID
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 字典编码(唯一)
     * 格式: module_entity_property
     */
    private String code;

    /**
     * 字典名称
     */
    private String name;

    /**
     * 字典描述
     */
    private String description;

    /**
     * 字典类别
     * system-系统字典, business-业务字典
     */
    private String category;

    /**
     * 字典项数量
     */
    @TableField("item_count")
    private Integer itemCount;

    /**
     * 状态
     * enabled-启用, disabled-禁用
     */
    private String status;

    /**
     * 排序序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 扩展属性(JSON格式)
     */
    @TableField("ext_props")
    private String extProps;

    /**
     * 备注
     */
    private String remark;

    // ========== 审计字段 ==========

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private String updatedBy;

    /**
     * 逻辑删除标记
     * 0-未删除, 1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 删除时间
     */
    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 删除人ID
     */
    @TableField("deleted_by")
    private String deletedBy;
}
```

### 2.2 DictItem实体类

```java
package com.example.oa_system_backend.module.dict.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典项实体类
 * 对应表: sys_dict_item
 */
@Data
@TableName("sys_dict_item")
public class DictItem {

    /**
     * 主键: 字典项ID
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 所属字典类型ID
     * 外键: sys_dict_type.id
     */
    @TableField("dict_type_id")
    private String dictTypeId;

    /**
     * 字典类型编码(冗余字段,方便查询)
     */
    @TableField("dict_type_code")
    private String dictTypeCode;

    /**
     * 项标签(显示文本)
     */
    private String label;

    /**
     * 项值(实际值)
     */
    private String value;

    /**
     * 颜色类型
     * primary/success/warning/danger/info
     */
    @TableField("color_type")
    private String colorType;

    /**
     * 自定义颜色(如: #409EFF)
     */
    private String color;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态
     * enabled-启用, disabled-禁用
     */
    private String status;

    /**
     * 扩展属性(JSON格式)
     */
    @TableField("ext_props")
    private String extProps;

    /**
     * 备注
     */
    private String remark;

    // ========== 审计字段 ==========

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private String updatedBy;

    /**
     * 逻辑删除标记
     * 0-未删除, 1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 删除时间
     */
    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 删除人ID
     */
    @TableField("deleted_by")
    private String deletedBy;
}
```

---

## 3. DTO设计

### 3.1 DictTypeCreateRequest

```java
package com.example.oa_system_backend.module.dict.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 创建字典类型请求DTO
 */
@Data
public class DictTypeCreateRequest {

    /**
     * 字典编码
     */
    @NotBlank(message = "字典编码不能为空")
    @Size(min = 2, max = 100, message = "字典编码长度在2-100个字符之间")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$",
             message = "字典编码必须以小写字母开头,只能包含小写字母、数字和下划线")
    private String code;

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    @Size(min = 2, max = 100, message = "字典名称长度在2-100个字符之间")
    private String name;

    /**
     * 字典描述
     */
    @Size(max = 500, message = "字典描述长度不能超过500个字符")
    private String description;

    /**
     * 字典类别
     */
    @NotBlank(message = "字典类别不能为空")
    @Pattern(regexp = "^(system|business)$", message = "字典类别必须是system或business")
    private String category;

    /**
     * 状态
     */
    @Pattern(regexp = "^(enabled|disabled)$", message = "状态必须是enabled或disabled")
    private String status;

    /**
     * 排序序号
     */
    @Min(value = 0, message = "排序序号不能小于0")
    private Integer sortOrder;

    /**
     * 扩展属性(JSON格式)
     */
    private String extProps;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
```

### 3.2 DictTypeUpdateRequest

```java
package com.example.oa_system_backend.module.dict.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 更新字典类型请求DTO
 */
@Data
public class DictTypeUpdateRequest {

    @Size(min = 2, max = 100, message = "字典名称长度在2-100个字符之间")
    private String name;

    @Size(max = 500, message = "字典描述长度不能超过500个字符")
    private String description;

    @Pattern(regexp = "^(system|business)$", message = "字典类别必须是system或business")
    private String category;

    @Pattern(regexp = "^(enabled|disabled)$", message = "状态必须是enabled或disabled")
    private String status;

    @Min(value = 0, message = "排序序号不能小于0")
    private Integer sortOrder;

    private String extProps;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
```

### 3.3 DictTypeQueryRequest

```java
package com.example.oa_system_backend.module.dict.dto;

import lombok.Data;

/**
 * 字典类型查询请求DTO
 */
@Data
public class DictTypeQueryRequest {

    /**
     * 关键词搜索(编码/名称)
     */
    private String keyword;

    /**
     * 字典类别
     */
    private String category;

    /**
     * 状态
     */
    private String status;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 20;
}
```

### 3.4 DictItemCreateRequest

```java
package com.example.oa_system_backend.module.dict.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 创建字典项请求DTO
 */
@Data
public class DictItemCreateRequest {

    /**
     * 所属字典类型ID
     */
    @NotBlank(message = "字典类型ID不能为空")
    private String dictTypeId;

    /**
     * 项标签
     */
    @NotBlank(message = "项标签不能为空")
    @Size(min = 1, max = 200, message = "项标签长度在1-200个字符之间")
    private String label;

    /**
     * 项值
     */
    @NotBlank(message = "项值不能为空")
    @Size(min = 1, max = 200, message = "项值长度在1-200个字符之间")
    private String value;

    /**
     * 颜色类型
     */
    @Pattern(regexp = "^(primary|success|warning|danger|info)?$",
             message = "颜色类型必须是primary、success、warning、danger或info")
    private String colorType;

    /**
     * 自定义颜色
     */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$",
             message = "自定义颜色格式不正确,应为#开头的6位十六进制颜色值")
    private String color;

    /**
     * 图标
     */
    @Size(max = 100, message = "图标长度不能超过100个字符")
    private String icon;

    /**
     * 排序序号
     */
    @Min(value = 0, message = "排序序号不能小于0")
    private Integer sortOrder;

    /**
     * 状态
     */
    @Pattern(regexp = "^(enabled|disabled)$", message = "状态必须是enabled或disabled")
    private String status;

    /**
     * 扩展属性
     */
    private String extProps;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
```

### 3.5 DictItemUpdateRequest

```java
package com.example.oa_system_backend.module.dict.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 更新字典项请求DTO
 */
@Data
public class DictItemUpdateRequest {

    @Size(min = 1, max = 200, message = "项标签长度在1-200个字符之间")
    private String label;

    @Size(min = 1, max = 200, message = "项值长度在1-200个字符之间")
    private String value;

    @Pattern(regexp = "^(primary|success|warning|danger|info)?$",
             message = "颜色类型必须是primary、success、warning、danger或info")
    private String colorType;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$",
             message = "自定义颜色格式不正确,应为#开头的6位十六进制颜色值")
    private String color;

    @Size(max = 100, message = "图标长度不能超过100个字符")
    private String icon;

    @Min(value = 0, message = "排序序号不能小于0")
    private Integer sortOrder;

    @Pattern(regexp = "^(enabled|disabled)$", message = "状态必须是enabled或disabled")
    private String status;

    private String extProps;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
```

### 3.6 DictItemQueryRequest

```java
package com.example.oa_system_backend.module.dict.dto;

import lombok.Data;

/**
 * 字典项查询请求DTO
 */
@Data
public class DictItemQueryRequest {

    /**
     * 关键词搜索(标签/值)
     */
    private String keyword;

    /**
     * 字典类型ID
     */
    private String dictTypeId;

    /**
     * 字典类型编码
     */
    private String dictTypeCode;

    /**
     * 状态
     */
    private String status;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 20;
}
```

### 3.7 DictItemSortUpdateRequest

```java
package com.example.oa_system_backend.module.dict.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 字典项排序更新请求DTO
 */
@Data
public class DictItemSortUpdateRequest {

    /**
     * 字典类型ID
     */
    @NotBlank(message = "字典类型ID不能为空")
    private String dictTypeId;

    /**
     * 排序项列表
     */
    @NotEmpty(message = "排序项列表不能为空")
    @Size(min = 1, message = "至少需要一项")
    private List<SortItem> items;

    /**
     * 排序项
     */
    @Data
    public static class SortItem {
        /**
         * 字典项ID
         */
        @NotBlank(message = "字典项ID不能为空")
        private String id;

        /**
         * 排序序号
         */
        @NotNull(message = "排序序号不能为空")
        @Min(value = 0, message = "排序序号不能小于0")
        private Integer sortOrder;
    }
}
```

### 3.8 DictBatchOperationRequest

```java
package com.example.oa_system_backend.module.dict.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 批量操作请求DTO
 */
@Data
public class DictBatchOperationRequest {

    /**
     * ID列表
     */
    @NotEmpty(message = "ID列表不能为空")
    @Size(min = 1, message = "至少需要一个ID")
    private List<@NotBlank(message = "ID不能为空") String> ids;

    /**
     * 状态(用于批量启用/禁用)
     */
    @Pattern(regexp = "^(enabled|disabled)$", message = "状态必须是enabled或disabled")
    private String status;
}
```

---

## 4. VO设计

### 4.1 DictTypeVO

```java
package com.example.oa_system_backend.module.dict.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典类型视图对象
 */
@Data
public class DictTypeVO {

    /**
     * 字典类型ID
     */
    private String id;

    /**
     * 字典编码
     */
    private String code;

    /**
     * 字典名称
     */
    private String name;

    /**
     * 字典描述
     */
    private String description;

    /**
     * 字典类别
     */
    private String category;

    /**
     * 字典项数量
     */
    private Integer itemCount;

    /**
     * 状态
     */
    private String status;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 扩展属性
     */
    private String extProps;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
```

### 4.2 DictItemVO

```java
package com.example.oa_system_backend.module.dict.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典项视图对象
 */
@Data
public class DictItemVO {

    /**
     * 字典项ID
     */
    private String id;

    /**
     * 所属字典类型ID
     */
    private String dictTypeId;

    /**
     * 字典类型编码
     */
    private String dictTypeCode;

    /**
     * 项标签
     */
    private String label;

    /**
     * 项值
     */
    private String value;

    /**
     * 颜色类型
     */
    private String colorType;

    /**
     * 自定义颜色
     */
    private String color;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private String status;

    /**
     * 扩展属性
     */
    private String extProps;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
```

### 4.3 DictTreeVO

```java
package com.example.oa_system_backend.module.dict.vo;

import lombok.Data;

import java.util.List;

/**
 * 字典树视图对象
 */
@Data
public class DictTreeVO {

    /**
     * 字典类型ID
     */
    private String id;

    /**
     * 字典编码
     */
    private String code;

    /**
     * 字典名称
     */
    private String name;

    /**
     * 字典描述
     */
    private String description;

    /**
     * 字典类别
     */
    private String category;

    /**
     * 字典项数量
     */
    private Integer itemCount;

    /**
     * 状态
     */
    private String status;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 扩展属性
     */
    private String extProps;

    /**
     * 备注
     */
    private String remark;

    /**
     * 字典项列表
     */
    private List<DictItemVO> items;
}
```

### 4.4 DictDataVO

```java
package com.example.oa_system_backend.module.dict.vo;

import lombok.Data;

import java.util.List;

/**
 * 字典数据视图对象(用于前端使用)
 */
@Data
public class DictDataVO {

    /**
     * 字典类型编码
     */
    private String dictType;

    /**
     * 字典项列表
     */
    private List<DictItemVO> items;
}
```

---

## 5. Mapper接口

### 5.1 DictTypeMapper

```java
package com.example.oa_system_backend.module.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.dict.entity.DictType;
import com.example.oa_system_backend.module.dict.dto.DictTypeQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 字典类型Mapper接口
 */
@Mapper
public interface DictTypeMapper extends BaseMapper<DictType> {

    /**
     * 分页查询字典类型列表
     */
    @Select("<script>" +
            "SELECT * FROM sys_dict_type " +
            "WHERE is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (code LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='category != null and category != \"\"'>" +
            "  AND category = #{category} " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "  AND status = #{status} " +
            "</if>" +
            "ORDER BY sort_order, id" +
            "</script>")
    IPage<DictType> selectPageByQuery(
        Page<DictType> page,
        @Param("keyword") String keyword,
        @Param("category") String category,
        @Param("status") String status
    );

    /**
     * 检查字典编码是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_dict_type " +
            "WHERE code = #{code} " +
            "AND is_deleted = 0 " +
            "<if test='excludeId != null'>" +
            "AND id != #{excludeId} " +
            "</if>")
    Integer countByCode(@Param("code") String code,
                       @Param("excludeId") String excludeId);

    /**
     * 根据编码查询字典类型
     */
    @Select("SELECT * FROM sys_dict_type " +
            "WHERE code = #{code} " +
            "AND is_deleted = 0")
    DictType selectByCode(@Param("code") String code);

    /**
     * 更新字典项数量
     */
    @Select("UPDATE sys_dict_type " +
            "SET item_count = (" +
            "  SELECT COUNT(*) FROM sys_dict_item " +
            "  WHERE dict_type_id = #{dictTypeId} " +
            "  AND is_deleted = 0" +
            ") " +
            "WHERE id = #{dictTypeId}")
    void updateItemCount(@Param("dictTypeId") String dictTypeId);
}
```

### 5.2 DictItemMapper

```java
package com.example.oa_system_backend.module.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.dict.entity.DictItem;
import com.example.oa_system_backend.module.dict.dto.DictItemQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 字典项Mapper接口
 */
@Mapper
public interface DictItemMapper extends BaseMapper<DictItem> {

    /**
     * 分页查询字典项列表
     */
    @Select("<script>" +
            "SELECT * FROM sys_dict_item " +
            "WHERE is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (label LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR value LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='dictTypeId != null and dictTypeId != \"\"'>" +
            "  AND dict_type_id = #{dictTypeId} " +
            "</if>" +
            "<if test='dictTypeCode != null and dictTypeCode != \"\"'>" +
            "  AND dict_type_code = #{dictTypeCode} " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "  AND status = #{status} " +
            "</if>" +
            "ORDER BY sort_order, id" +
            "</script>")
    IPage<DictItem> selectPageByQuery(
        Page<DictItem> page,
        @Param("keyword") String keyword,
        @Param("dictTypeId") String dictTypeId,
        @Param("dictTypeCode") String dictTypeCode,
        @Param("status") String status
    );

    /**
     * 根据字典类型编码查询启用的字典项
     */
    @Select("SELECT * FROM sys_dict_item " +
            "WHERE dict_type_code = #{dictTypeCode} " +
            "AND is_deleted = 0 " +
            "AND status = 'enabled' " +
            "ORDER BY sort_order, id")
    List<DictItem> selectEnabledByDictTypeCode(@Param("dictTypeCode") String dictTypeCode);

    /**
     * 检查字典项值是否存在(在同一字典类型下)
     */
    @Select("SELECT COUNT(*) FROM sys_dict_item " +
            "WHERE dict_type_id = #{dictTypeId} " +
            "AND value = #{value} " +
            "AND is_deleted = 0 " +
            "<if test='excludeId != null'>" +
            "AND id != #{excludeId} " +
            "</if>")
    Integer countByValue(@Param("dictTypeId") String dictTypeId,
                        @Param("value") String value,
                        @Param("excludeId") String excludeId);

    /**
     * 检查字典类型是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_dict_type " +
            "WHERE id = #{dictTypeId} " +
            "AND is_deleted = 0")
    Integer countByDictTypeId(@Param("dictTypeId") String dictTypeId);

    /**
     * 批量更新排序
     */
    void batchUpdateSort(@Param("dictTypeId") String dictTypeId,
                        @Param("items") List<SortItem> items);

    /**
     * 排序项内部类
     */
    class SortItem {
        private String id;
        private Integer sortOrder;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }
    }
}
```

对应的Mapper XML文件:

**DictItemMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.oa_system_backend.module.dict.mapper.DictItemMapper">

    <update id="batchUpdateSort">
        <foreach collection="items" item="item" separator=";">
            UPDATE sys_dict_item
            SET sort_order = #{item.sortOrder},
                updated_at = NOW()
            WHERE id = #{item.id}
              AND dict_type_id = #{dictTypeId}
              AND is_deleted = 0
        </foreach>
    </update>

</mapper>
```

---

## 6. Service层设计

### 6.1 DictService接口

```java
package com.example.oa_system_backend.module.dict.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.dict.dto.*;
import com.example.oa_system_backend.module.dict.entity.DictType;
import com.example.oa_system_backend.module.dict.entity.DictItem;
import com.example.oa_system_backend.module.dict.vo.*;

import java.util.List;

/**
 * 数据字典服务接口
 */
public interface DictService {

    // ========== 字典类型操作 ==========

    /**
     * 分页查询字典类型列表
     */
    IPage<DictTypeVO> getDictTypeList(DictTypeQueryRequest request);

    /**
     * 根据ID获取字典类型详情
     */
    DictTypeVO getDictTypeById(String id);

    /**
     * 根据编码获取字典类型
     */
    DictTypeVO getDictTypeByCode(String code);

    /**
     * 创建字典类型
     */
    DictType createDictType(DictTypeCreateRequest request);

    /**
     * 更新字典类型
     */
    DictType updateDictType(String id, DictTypeUpdateRequest request);

    /**
     * 删除字典类型(级联删除字典项)
     */
    void deleteDictType(String id);

    /**
     * 检查字典编码是否存在
     */
    boolean checkDictCodeExists(String code, String excludeId);

    // ========== 字典项操作 ==========

    /**
     * 分页查询字典项列表
     */
    IPage<DictItemVO> getDictItemList(DictItemQueryRequest request);

    /**
     * 根据ID获取字典项详情
     */
    DictItemVO getDictItemById(String id);

    /**
     * 创建字典项
     */
    DictItem createDictItem(DictItemCreateRequest request);

    /**
     * 更新字典项
     */
    DictItem updateDictItem(String id, DictItemUpdateRequest request);

    /**
     * 删除字典项
     */
    void deleteDictItem(String id);

    /**
     * 批量删除字典项
     */
    void batchDeleteDictItems(List<String> ids);

    /**
     * 批量更新字典项状态
     */
    void batchUpdateDictItemStatus(List<String> ids, String status);

    /**
     * 批量更新字典项排序
     */
    void batchUpdateDictItemSort(DictItemSortUpdateRequest request);

    /**
     * 检查字典项值是否存在
     */
    boolean checkDictValueExists(String dictTypeId, String value, String excludeId);

    // ========== 字典树和数据 ==========

    /**
     * 获取字典树
     */
    List<DictTreeVO> getDictTree(String category, String status);

    /**
     * 根据字典类型编码获取字典数据(带缓存)
     */
    DictDataVO getDictData(String dictTypeCode);

    /**
     * 清除字典缓存
     */
    void clearDictCache(String dictTypeCode);
}
```

---

## 7. Controller层设计

```java
package com.example.oa_system_backend.module.dict.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.dict.dto.*;
import com.example.oa_system_backend.module.dict.service.DictService;
import com.example.oa_system_backend.module.dict.vo.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据字典控制器
 */
@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    // ========== 字典类型接口 ==========

    /**
     * 获取字典类型列表
     * GET /api/dict/types
     */
    @GetMapping("/types")
    public ApiResponse<IPage<DictTypeVO>> getDictTypeList(DictTypeQueryRequest request) {
        IPage<DictTypeVO> result = dictService.getDictTypeList(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取字典类型详情
     * GET /api/dict/types/{id}
     */
    @GetMapping("/types/{id}")
    public ApiResponse<DictTypeVO> getDictTypeById(@PathVariable String id) {
        DictTypeVO dictType = dictService.getDictTypeById(id);
        return ApiResponse.success(dictType);
    }

    /**
     * 创建字典类型
     * POST /api/dict/types
     */
    @PostMapping("/types")
    public ApiResponse<DictType> createDictType(
            @Valid @RequestBody DictTypeCreateRequest request) {
        DictType dictType = dictService.createDictType(request);
        return ApiResponse.success("创建成功", dictType);
    }

    /**
     * 更新字典类型
     * PUT /api/dict/types/{id}
     */
    @PutMapping("/types/{id}")
    public ApiResponse<DictType> updateDictType(
            @PathVariable String id,
            @Valid @RequestBody DictTypeUpdateRequest request) {
        DictType dictType = dictService.updateDictType(id, request);
        return ApiResponse.success("更新成功", dictType);
    }

    /**
     * 删除字典类型
     * DELETE /api/dict/types/{id}
     */
    @DeleteMapping("/types/{id}")
    public ApiResponse<Void> deleteDictType(@PathVariable String id) {
        dictService.deleteDictType(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 检查字典编码是否存在
     * GET /api/dict/types/check-code
     */
    @GetMapping("/types/check-code")
    public ApiResponse<Boolean> checkDictCodeExists(
            @RequestParam String code,
            @RequestParam(required = false) String excludeId) {
        boolean exists = dictService.checkDictCodeExists(code, excludeId);
        return ApiResponse.success(exists);
    }

    // ========== 字典项接口 ==========

    /**
     * 获取字典项列表
     * GET /api/dict/items
     */
    @GetMapping("/items")
    public ApiResponse<IPage<DictItemVO>> getDictItemList(DictItemQueryRequest request) {
        IPage<DictItemVO> result = dictService.getDictItemList(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取字典项详情
     * GET /api/dict/items/{id}
     */
    @GetMapping("/items/{id}")
    public ApiResponse<DictItemVO> getDictItemById(@PathVariable String id) {
        DictItemVO dictItem = dictService.getDictItemById(id);
        return ApiResponse.success(dictItem);
    }

    /**
     * 创建字典项
     * POST /api/dict/items
     */
    @PostMapping("/items")
    public ApiResponse<DictItem> createDictItem(
            @Valid @RequestBody DictItemCreateRequest request) {
        DictItem dictItem = dictService.createDictItem(request);
        return ApiResponse.success("创建成功", dictItem);
    }

    /**
     * 更新字典项
     * PUT /api/dict/items/{id}
     */
    @PutMapping("/items/{id}")
    public ApiResponse<DictItem> updateDictItem(
            @PathVariable String id,
            @Valid @RequestBody DictItemUpdateRequest request) {
        DictItem dictItem = dictService.updateDictItem(id, request);
        return ApiResponse.success("更新成功", dictItem);
    }

    /**
     * 删除字典项
     * DELETE /api/dict/items/{id}
     */
    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> deleteDictItem(@PathVariable String id) {
        dictService.deleteDictItem(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 批量删除字典项
     * DELETE /api/dict/items/batch
     */
    @DeleteMapping("/items/batch")
    public ApiResponse<Void> batchDeleteDictItems(
            @RequestBody DictBatchOperationRequest request) {
        dictService.batchDeleteDictItems(request.getIds());
        return ApiResponse.success("批量删除成功", null);
    }

    /**
     * 批量更新字典项状态
     * PUT /api/dict/items/batch/status
     */
    @PutMapping("/items/batch/status")
    public ApiResponse<Void> batchUpdateDictItemStatus(
            @RequestBody DictBatchOperationRequest request) {
        dictService.batchUpdateDictItemStatus(request.getIds(), request.getStatus());
        return ApiResponse.success("批量更新状态成功", null);
    }

    /**
     * 批量更新字典项排序
     * PUT /api/dict/items/sort
     */
    @PutMapping("/items/sort")
    public ApiResponse<Void> batchUpdateDictItemSort(
            @Valid @RequestBody DictItemSortUpdateRequest request) {
        dictService.batchUpdateDictItemSort(request);
        return ApiResponse.success("批量更新排序成功", null);
    }

    /**
     * 检查字典项值是否存在
     * GET /api/dict/items/check-value
     */
    @GetMapping("/items/check-value")
    public ApiResponse<Boolean> checkDictValueExists(
            @RequestParam String dictTypeId,
            @RequestParam String value,
            @RequestParam(required = false) String excludeId) {
        boolean exists = dictService.checkDictValueExists(dictTypeId, value, excludeId);
        return ApiResponse.success(exists);
    }

    // ========== 字典树和数据接口 ==========

    /**
     * 获取字典树
     * GET /api/dict/tree
     */
    @GetMapping("/tree")
    public ApiResponse<List<DictTreeVO>> getDictTree(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        List<DictTreeVO> tree = dictService.getDictTree(category, status);
        return ApiResponse.success(tree);
    }

    /**
     * 根据字典类型编码获取字典数据
     * GET /api/dict/{code}
     */
    @GetMapping("/{code}")
    public ApiResponse<DictDataVO> getDictData(@PathVariable String code) {
        DictDataVO dictData = dictService.getDictData(code);
        return ApiResponse.success(dictData);
    }

    /**
     * 清除字典缓存
     * DELETE /api/dict/cache/{code}
     */
    @DeleteMapping("/cache/{code}")
    public ApiResponse<Void> clearDictCache(@PathVariable String code) {
        dictService.clearDictCache(code);
        return ApiResponse.success("清除缓存成功", null);
    }
}
```

---

## 8. 业务逻辑实现

### 8.1 创建字典类型

```java
@Override
@Transactional
public DictType createDictType(DictTypeCreateRequest request) {
    // 1. 验证字典编码唯一性
    if (dictTypeMapper.countByCode(request.getCode(), null) > 0) {
        throw new BusinessException("字典编码已存在: " + request.getCode());
    }

    // 2. 验证字典类别
    if (!request.getCategory().equals("system") &&
        !request.getCategory().equals("business")) {
        throw new BusinessException("字典类别必须是system或business");
    }

    // 3. 验证状态
    if (request.getStatus() != null &&
        !request.getStatus().equals("enabled") &&
        !request.getStatus().equals("disabled")) {
        throw new BusinessException("状态必须是enabled或disabled");
    }

    // 4. 生成字典类型ID
    String dictTypeId = generateDictTypeId();

    // 5. 构建DictType实体
    DictType dictType = new DictType();
    BeanUtils.copyProperties(request, dictType);
    dictType.setId(dictTypeId);
    dictType.setItemCount(0);
    dictType.setStatus(request.getStatus() != null ? request.getStatus() : "enabled");
    dictType.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
    dictType.setCreatedAt(LocalDateTime.now());
    dictType.setUpdatedAt(LocalDateTime.now());

    // 6. 保存到数据库
    dictTypeMapper.insert(dictType);

    return dictType;
}

/**
 * 生成字典类型ID
 */
private String generateDictTypeId() {
    return "DICT_" + System.currentTimeMillis();
}
```

### 8.2 创建字典项

```java
@Override
@Transactional
public DictItem createDictItem(DictItemCreateRequest request) {
    // 1. 验证字典类型存在性
    if (dictItemMapper.countByDictTypeId(request.getDictTypeId()) == 0) {
        throw new BusinessException("指定的字典类型不存在");
    }

    // 2. 验证字典项值唯一性(在同一字典类型下)
    if (dictItemMapper.countByValue(request.getDictTypeId(),
                                   request.getValue(), null) > 0) {
        throw new BusinessException("字典项值已存在: " + request.getValue());
    }

    // 3. 查询字典类型以获取编码
    DictType dictType = dictTypeMapper.selectById(request.getDictTypeId());
    if (dictType == null) {
        throw new BusinessException("指定的字典类型不存在");
    }

    // 4. 验证颜色类型
    if (request.getColorType() != null) {
        String[] validColorTypes = {"primary", "success", "warning", "danger", "info"};
        boolean isValid = false;
        for (String colorType : validColorTypes) {
            if (colorType.equals(request.getColorType())) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            throw new BusinessException("颜色类型必须是primary、success、warning、danger或info");
        }
    }

    // 5. 生成字典项ID
    String dictItemId = generateDictItemId();

    // 6. 构建DictItem实体
    DictItem dictItem = new DictItem();
    BeanUtils.copyProperties(request, dictItem);
    dictItem.setId(dictItemId);
    dictItem.setDictTypeCode(dictType.getCode());
    dictItem.setStatus(request.getStatus() != null ? request.getStatus() : "enabled");
    dictItem.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
    dictItem.setCreatedAt(LocalDateTime.now());
    dictItem.setUpdatedAt(LocalDateTime.now());

    // 7. 保存到数据库
    dictItemMapper.insert(dictItem);

    // 8. 更新字典类型的item_count
    updateDictTypeItemCount(request.getDictTypeId());

    return dictItem;
}

/**
 * 生成字典项ID
 */
private String generateDictItemId() {
    return "ITEM_" + System.currentTimeMillis();
}

/**
 * 更新字典类型的item_count
 */
private void updateDictTypeItemCount(String dictTypeId) {
    // 触发器会自动更新,这里也可以手动调用
    dictTypeMapper.updateItemCount(dictTypeId);
}
```

### 8.3 删除字典类型(级联删除)

```java
@Override
@Transactional
public void deleteDictType(String id) {
    // 1. 检查字典类型是否存在
    DictType dictType = dictTypeMapper.selectById(id);
    if (dictType == null) {
        throw new ResourceNotFoundException("字典类型不存在");
    }

    // 2. 检查是否为系统字典
    if ("system".equals(dictType.getCategory())) {
        throw new BusinessException("系统字典不允许删除");
    }

    // 3. 软删除字典类型
    dictTypeMapper.deleteById(id);

    // 4. 级联软删除所有关联的字典项
    QueryWrapper<DictItem> wrapper = new QueryWrapper<>();
    wrapper.eq("dict_type_id", id);
    List<DictItem> items = dictItemMapper.selectList(wrapper);

    for (DictItem item : items) {
        dictItemMapper.deleteById(item.getId());
    }
}
```

### 8.4 批量更新排序

```java
@Override
@Transactional
public void batchUpdateDictItemSort(DictItemSortUpdateRequest request) {
    // 1. 验证字典类型存在性
    if (dictItemMapper.countByDictTypeId(request.getDictTypeId()) == 0) {
        throw new BusinessException("指定的字典类型不存在");
    }

    // 2. 转换排序项
    List<DictItemMapper.SortItem> sortItems = request.getItems().stream()
        .map(item -> {
            DictItemMapper.SortItem sortItem = new DictItemMapper.SortItem();
            sortItem.setId(item.getId());
            sortItem.setSortOrder(item.getSortOrder());
            return sortItem;
        })
        .collect(Collectors.toList());

    // 3. 批量更新排序
    dictItemMapper.batchUpdateSort(request.getDictTypeId(), sortItems);

    // 4. 清除缓存
    DictType dictType = dictTypeMapper.selectById(request.getDictTypeId());
    if (dictType != null) {
        clearDictCache(dictType.getCode());
    }
}
```

### 8.5 获取字典数据(带缓存)

```java
@Override
public DictDataVO getDictData(String dictTypeCode) {
    // 1. 尝试从缓存获取
    String cacheKey = "dict:data:" + dictTypeCode;
    String cachedData = redisTemplate.opsForValue().get(cacheKey);

    if (cachedData != null) {
        return JSON.parseObject(cachedData, DictDataVO.class);
    }

    // 2. 查询数据库
    List<DictItem> items = dictItemMapper.selectEnabledByDictTypeCode(dictTypeCode);

    // 3. 转换为VO
    List<DictItemVO> itemVOs = items.stream()
        .map(item -> {
            DictItemVO vo = new DictItemVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        })
        .collect(Collectors.toList());

    // 4. 构建返回数据
    DictDataVO dictData = new DictDataVO();
    dictData.setDictType(dictTypeCode);
    dictData.setItems(itemVOs);

    // 5. 设置缓存(30分钟)
    redisTemplate.opsForValue().set(
        cacheKey,
        JSON.toJSONString(dictData),
        30,
        TimeUnit.MINUTES
    );

    return dictData;
}
```

---

## 9. 数据验证

### 9.1 外键约束验证

```java
/**
 * 验证字典类型存在性
 */
private void validateDictTypeId(String dictTypeId) {
    if (dictItemMapper.countByDictTypeId(dictTypeId) == 0) {
        throw new BusinessException("指定的字典类型不存在");
    }
}

/**
 * 验证字典类型存在性(根据ID)
 */
private void validateDictTypeExists(String dictTypeId) {
    DictType dictType = dictTypeMapper.selectById(dictTypeId);
    if (dictType == null) {
        throw new BusinessException("指定的字典类型不存在");
    }
}
```

### 9.2 唯一性约束验证

```java
/**
 * 验证字典编码唯一性
 */
private void validateDictCodeUnique(String code, String excludeId) {
    if (dictTypeMapper.countByCode(code, excludeId) > 0) {
        throw new BusinessException("字典编码已存在: " + code);
    }
}

/**
 * 验证字典项值唯一性
 */
private void validateDictValueUnique(String dictTypeId, String value, String excludeId) {
    if (dictItemMapper.countByValue(dictTypeId, value, excludeId) > 0) {
        throw new BusinessException("字典项值已存在: " + value);
    }
}
```

### 9.3 数据完整性验证

```java
/**
 * 验证字典类别
 */
private void validateCategory(String category) {
    if (!category.equals("system") && !category.equals("business")) {
        throw new BusinessException("字典类别必须是system或business");
    }
}

/**
 * 验证状态
 */
private void validateStatus(String status) {
    if (!status.equals("enabled") && !status.equals("disabled")) {
        throw new BusinessException("状态必须是enabled或disabled");
    }
}

/**
 * 验证颜色类型
 */
private void validateColorType(String colorType) {
    if (colorType != null) {
        String[] validColorTypes = {"primary", "success", "warning", "danger", "info"};
        boolean isValid = false;
        for (String validType : validColorTypes) {
            if (validType.equals(colorType)) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            throw new BusinessException("颜色类型必须是primary、success、warning、danger或info");
        }
    }
}

/**
 * 验证排序序号
 */
private void validateSortOrder(Integer sortOrder) {
    if (sortOrder != null && sortOrder < 0) {
        throw new BusinessException("排序序号不能小于0");
    }
}
```

---

## 10. 权限控制

### 10.1 Spring Security配置

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 数据字典权限
                .requestMatchers(HttpMethod.GET, "/api/dict/**").hasAnyAuthority(
                    "dict:view", "dict:view_all", "dict:view_system", "dict:view_business"
                )
                .requestMatchers(HttpMethod.POST, "/api/dict/types").hasAnyAuthority(
                    "dict:create", "dict:create_system", "dict:create_business"
                )
                .requestMatchers(HttpMethod.POST, "/api/dict/items").hasAnyAuthority(
                    "dict:create", "dict:create_system", "dict:create_business"
                )
                .requestMatchers(HttpMethod.PUT, "/api/dict/**").hasAnyAuthority(
                    "dict:edit", "dict:edit_all"
                )
                .requestMatchers(HttpMethod.DELETE, "/api/dict/**").hasAnyAuthority(
                    "dict:delete", "dict:delete_all"
                )
                // 其他请求...
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
```

### 10.2 字典类别权限过滤

```java
/**
 * 获取当前用户的字典类别权限
 */
private Set<String> getDictCategoryPermissions() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
        return Collections.emptySet();
    }

    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(auth -> auth.startsWith("dict:"))
        .collect(Collectors.toSet());
}

/**
 * 检查是否有系统字典权限
 */
private boolean hasSystemDictPermission() {
    Set<String> permissions = getDictCategoryPermissions();
    return permissions.contains("dict:view_all") ||
           permissions.contains("dict:view_system") ||
           permissions.contains("dict:create_system") ||
           permissions.contains("dict:edit_system") ||
           permissions.contains("dict:delete_system");
}

/**
 * 检查是否有业务字典权限
 */
private boolean hasBusinessDictPermission() {
    Set<String> permissions = getDictCategoryPermissions();
    return permissions.contains("dict:view_all") ||
           permissions.contains("dict:view_business") ||
           permissions.contains("dict:create_business") ||
           permissions.contains("dict:edit_business") ||
           permissions.contains("dict:delete_business");
}
```

### 10.3 字典类型操作权限验证

```java
/**
 * 创建字典类型前权限验证
 */
private void validateCreatePermission(String category) {
    if ("system".equals(category)) {
        if (!hasSystemDictPermission()) {
            throw new AccessDeniedException("没有创建系统字典的权限");
        }
    } else if ("business".equals(category)) {
        if (!hasBusinessDictPermission()) {
            throw new AccessDeniedException("没有创建业务字典的权限");
        }
    }
}

/**
 * 删除字典类型前权限验证
 */
private void validateDeletePermission(DictType dictType) {
    // 系统字典需要特殊权限才能删除
    if ("system".equals(dictType.getCategory())) {
        if (!hasSystemDictPermission()) {
            throw new AccessDeniedException("没有删除系统字典的权限");
        }
    }

    // 检查是否有删除权限
    Set<String> permissions = getDictCategoryPermissions();
    if (!permissions.contains("dict:delete_all")) {
        throw new AccessDeniedException("没有删除字典的权限");
    }
}
```

---

## 11. 异常处理

### 11.1 全局异常处理器

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return ApiResponse.error(e.getMessage());
    }

    /**
     * 参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", "));
        log.error("参数验证失败: {}", message);
        return ApiResponse.error("参数验证失败: " + message);
    }

    /**
     * 资源不存在异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResponse<Void> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("资源不存在: {}", e.getMessage());
        return ApiResponse.error(e.getMessage());
    }

    /**
     * 权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.error("权限不足: {}", e.getMessage());
        return ApiResponse.error(403, "权限不足: " + e.getMessage());
    }

    /**
     * 数据完整性约束异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResponse<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("数据完整性约束异常: {}", e.getMessage());
        String message = "数据完整性约束异常";

        // 判断是否为唯一键冲突
        if (e.getMessage() != null) {
            if (e.getMessage().contains("Duplicate entry")) {
                message = "数据已存在,请检查唯一性约束";
            } else if (e.getMessage().contains("foreign key constraint")) {
                message = "违反外键约束,请检查关联数据";
            }
        }

        return ApiResponse.error(message);
    }
}
```

### 11.2 自定义异常类

```java
/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * 资源不存在异常
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

---

## 12. 缓存策略

### 12.1 Redis缓存配置

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<String> serializer =
            new Jackson2JsonRedisSerializer<>(String.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        serializer.setObjectMapper(mapper);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30)) // 默认缓存30分钟
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)))
            .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .build();
    }
}
```

### 12.2 缓存Key设计

```java
/**
 * 缓存Key常量
 */
public class DictCacheKeys {

    /**
     * 字典数据缓存Key
     * 格式: dict:data:{dictTypeCode}
     */
    public static final String DICT_DATA_KEY = "dict:data:%s";

    /**
     * 字典树缓存Key
     * 格式: dict:tree:{category}:{status}
     */
    public static final String DICT_TREE_KEY = "dict:tree:%s:%s";

    /**
     * 字典类型缓存Key
     * 格式: dict:type:{id}
     */
    public static final String DICT_TYPE_KEY = "dict:type:%s";

    /**
     * 字典项列表缓存Key
     * 格式: dict:items:{dictTypeId}
     */
    public static final String DICT_ITEMS_KEY = "dict:items:%s";
}
```

### 12.3 缓存实现

```java
@Override
public void clearDictCache(String dictTypeCode) {
    // 1. 清除字典数据缓存
    String dataKey = String.format(DictCacheKeys.DICT_DATA_KEY, dictTypeCode);
    redisTemplate.delete(dataKey);

    // 2. 清除字典树缓存(所有分类和状态的组合)
    Set<String> treeKeys = redisTemplate.keys("dict:tree:*");
    if (treeKeys != null && !treeKeys.isEmpty()) {
        redisTemplate.delete(treeKeys);
    }

    log.info("清除字典缓存: dictTypeCode={}", dictTypeCode);
}

/**
 * 清除所有相关缓存
 */
private void clearAllRelatedCache(String dictTypeId) {
    // 1. 获取字典类型信息
    DictType dictType = dictTypeMapper.selectById(dictTypeId);
    if (dictType != null) {
        // 2. 清除字典数据缓存
        clearDictCache(dictType.getCode());

        // 3. 清除字典类型缓存
        String typeKey = String.format(DictCacheKeys.DICT_TYPE_KEY, dictTypeId);
        redisTemplate.delete(typeKey);

        // 4. 清除字典项列表缓存
        String itemsKey = String.format(DictCacheKeys.DICT_ITEMS_KEY, dictTypeId);
        redisTemplate.delete(itemsKey);
    }
}
```

---

## 附录

### A. 外键约束实现

虽然数据库层面定义了外键约束,但为了提高性能和灵活性,在应用层也需要实现相应的验证逻辑:

#### A.1 字典项外键验证

```java
/**
 * 验证字典项的dict_type_id外键
 */
private void validateDictItemForeignKey(DictItem dictItem) {
    // 检查dict_type_id是否存在
    DictType dictType = dictTypeMapper.selectById(dictItem.getDictTypeId());
    if (dictType == null) {
        throw new BusinessException("字典类型不存在,dictTypeId=" + dictItem.getDictTypeId());
    }

    // 确保dict_type_code与dict_type_id一致
    if (!dictType.getCode().equals(dictItem.getDictTypeCode())) {
        throw new BusinessException("字典类型编码不匹配");
    }
}
```

### B. 检查约束实现

将数据库的检查约束逻辑在应用层也进行验证:

#### B.1 字典类型检查约束

```java
/**
 * 字典类型检查约束验证
 */
private void validateDictTypeConstraints(DictType dictType) {
    // 1. 字典类别检查
    if (!"system".equals(dictType.getCategory()) &&
        !"business".equals(dictType.getCategory())) {
        throw new BusinessException("字典类别必须是system或business");
    }

    // 2. 字典状态检查
    if (!"enabled".equals(dictType.getStatus()) &&
        !"disabled".equals(dictType.getStatus())) {
        throw new BusinessException("字典状态必须是enabled或disabled");
    }

    // 3. 字典项数量非负
    if (dictType.getItemCount() != null && dictType.getItemCount() < 0) {
        throw new BusinessException("字典项数量不能为负数");
    }

    // 4. 排序序号非负
    if (dictType.getSortOrder() != null && dictType.getSortOrder() < 0) {
        throw new BusinessException("排序序号不能为负数");
    }
}
```

#### B.2 字典项检查约束

```java
/**
 * 字典项检查约束验证
 */
private void validateDictItemConstraints(DictItem dictItem) {
    // 1. 字典项状态检查
    if (!"enabled".equals(dictItem.getStatus()) &&
        !"disabled".equals(dictItem.getStatus())) {
        throw new BusinessException("字典项状态必须是enabled或disabled");
    }

    // 2. 颜色类型检查
    if (dictItem.getColorType() != null) {
        String[] validColorTypes = {"primary", "success", "warning", "danger", "info"};
        boolean isValid = false;
        for (String colorType : validColorTypes) {
            if (colorType.equals(dictItem.getColorType())) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            throw new BusinessException("颜色类型必须是primary、success、warning、danger或info");
        }
    }

    // 3. 排序序号非负
    if (dictItem.getSortOrder() != null && dictItem.getSortOrder() < 0) {
        throw new BusinessException("排序序号不能为负数");
    }
}
```

### C. 性能优化建议

1. **索引优化**: 确保常用查询字段都有索引
   - `sys_dict_type.code` (唯一索引)
   - `sys_dict_type.category`
   - `sys_dict_type.status`
   - `sys_dict_item.dict_type_id`
   - `sys_dict_item.dict_type_code`
   - `sys_dict_item.status`

2. **缓存策略**: 对字典数据进行缓存
   - 字典数据缓存30分钟
   - 字典树缓存30分钟
   - 使用Redis作为缓存中心

3. **分页查询**: 使用MyBatis Plus的分页插件
   - 默认每页20条
   - 最大每页100条

4. **批量操作**: 使用批量插入/更新减少数据库交互
   - 批量删除
   - 批量更新状态
   - 批量更新排序

### D. 测试要点

- [ ] 字典类型CRUD功能完整性
- [ ] 字典项CRUD功能完整性
- [ ] 字典编码唯一性验证
- [ ] 字典项值唯一性验证(同一字典类型下)
- [ ] 外键约束验证
- [ ] 检查约束验证
- [ ] 权限控制有效性
- [ ] 缓存功能正常工作
- [ ] 级联删除功能正常
- [ ] 批量操作功能正常
- [ ] 字典树查询正确性
- [ ] 字典数据查询正确性

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-14
