package com.example.oa_system_backend.module.menu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuStatusUpdateRequest {

    @NotNull(message = "状态不能为空")
    private Boolean status;
}
