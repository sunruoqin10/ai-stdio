package com.example.oa_system_backend.module.expense.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InvoiceOCRRequest {

    @NotBlank(message = "发票图片URL不能为空")
    private String imageUrl;
}
