package com.example.oa_system_backend.module.department.vo;

import lombok.Data;

import java.util.List;

/**
 * 部门列表响应VO
 */
@Data
public class DepartmentListVO {
    /**
     * 部门列表
     */
    private List<DepartmentVO> list;

    /**
     * 总数
     */
    private Long total;

    /**
     * 页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer pageSize;
}
