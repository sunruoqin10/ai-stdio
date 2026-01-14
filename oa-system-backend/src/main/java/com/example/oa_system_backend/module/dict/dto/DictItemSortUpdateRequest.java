package com.example.oa_system_backend.module.dict.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 字典项排序更新请求DTO
 */
@Data
public class DictItemSortUpdateRequest {

    /**
     * 字典类型ID
     */
    @NotBlank(message = "字典类型ID不能为空")
    private String dictTypeId;

    /**
     * 排序项列表
     */
    @NotEmpty(message = "排序项列表不能为空")
    @Size(min = 1, message = "至少需要一项")
    private List<SortItem> items;

    /**
     * 排序项
     */
    @Data
    public static class SortItem {
        /**
         * 字典项ID
         */
        @NotBlank(message = "字典项ID不能为空")
        private String id;

        /**
         * 排序序号
         */
        @NotNull(message = "排序序号不能为空")
        @Min(value = 0, message = "排序序号不能小于0")
        private Integer sortOrder;
    }
}
