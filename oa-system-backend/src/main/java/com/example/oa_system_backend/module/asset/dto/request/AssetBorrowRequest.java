package com.example.oa_system_backend.module.asset.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 资产借用请求
 */
@Data
public class AssetBorrowRequest {

    /**
     * 借用人ID
     */
    @NotBlank(message = "借用人ID不能为空")
    private String borrowerId;

    /**
     * 借出日期
     */
    @NotNull(message = "借出日期不能为空")
    @PastOrPresent(message = "借出日期不能是未来日期")
    private LocalDate borrowDate;

    /**
     * 预计归还日期
     */
    @NotNull(message = "预计归还日期不能为空")
    @Future(message = "预计归还日期必须是未来日期")
    private LocalDate expectedReturnDate;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String notes;
}
