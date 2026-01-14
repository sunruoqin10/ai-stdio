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
