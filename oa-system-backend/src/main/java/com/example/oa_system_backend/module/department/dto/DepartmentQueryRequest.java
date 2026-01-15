package com.example.oa_system_backend.module.department.dto;

import lombok.Data;

/**
 * 部门查询请求DTO
 */
@Data
public class DepartmentQueryRequest {

    /**
     * 返回类型: tree-树形, flat-扁平
     */
    private String type;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 状态: active-正常, disabled-停用
     */
    private String status;

    /**
     * 负责人ID
     */
    private String leaderId;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 20;
}
