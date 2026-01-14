package com.example.oa_system_backend.module.dict.vo;

import lombok.Data;

import java.util.List;

/**
 * 字典数据视图对象(用于前端使用)
 */
@Data
public class DictDataVO {

    /**
     * 字典类型编码
     */
    private String dictType;

    /**
     * 字典项列表
     */
    private List<DictItemVO> items;
}
