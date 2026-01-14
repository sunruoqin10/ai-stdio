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
