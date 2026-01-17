package com.example.oa_system_backend.module.asset.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 资产归还请求
 */
@Data
public class AssetReturnRequest {

    /**
     * 实际归还日期
     */
    @NotNull(message = "归还日期不能为空")
    @PastOrPresent(message = "归还日期不能是未来日期")
    private LocalDate actualReturnDate;

    /**
     * 归还备注
     */
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String notes;
}
