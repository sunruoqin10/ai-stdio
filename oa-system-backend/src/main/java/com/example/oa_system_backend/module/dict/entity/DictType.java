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
    @TableId(type = IdType.AUTO)
    private Long id;

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
