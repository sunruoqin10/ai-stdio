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
