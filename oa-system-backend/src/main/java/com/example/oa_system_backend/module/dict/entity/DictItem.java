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
