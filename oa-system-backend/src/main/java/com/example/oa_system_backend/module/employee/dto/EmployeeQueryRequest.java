package com.example.oa_system_backend.module.employee.dto;

import lombok.Data;

/**
 * 员工查询请求DTO
 */
@Data
public class EmployeeQueryRequest {

    /**
     * 关键词搜索(姓名/工号/手机号)
     */
    private String keyword;

    /**
     * 员工状态
     */
    private String status;

    /**
     * 部门ID列表(逗号分隔)
     */
    private String departmentIds;

    /**
     * 职位
     */
    private String position;

    /**
     * 试用期状态
     */
    private String probationStatus;

    /**
     * 性别
     */
    private String gender;

    /**
     * 入职开始日期
     */
    private String joinDateStart;

    /**
     * 入职结束日期
     */
    private String joinDateEnd;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 20;
}
