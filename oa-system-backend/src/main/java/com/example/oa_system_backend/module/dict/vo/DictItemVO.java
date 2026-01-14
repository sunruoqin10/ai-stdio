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
