package com.example.oa_system_backend.module.department.dto;

import lombok.Data;

/**
 * 部门移动请求DTO
 */
@Data
public class DepartmentMoveRequest {

    /**
     * 新父部门ID
     */
    private String newParentId;

    /**
     * 乐观锁版本号（暂时不使用，保留字段供未来扩展）
     */
    // @NotNull(message = "版本号不能为空")  // 暂时移除验证
    private Integer version;
}
