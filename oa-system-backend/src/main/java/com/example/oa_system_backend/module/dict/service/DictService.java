package com.example.oa_system_backend.module.dict.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.module.dict.dto.*;
import com.example.oa_system_backend.module.dict.entity.DictItem;
import com.example.oa_system_backend.module.dict.entity.DictType;
import com.example.oa_system_backend.module.dict.vo.*;

import java.util.List;

/**
 * 数据字典服务接口
 */
public interface DictService {

    // ========== 字典类型操作 ==========

    /**
     * 分页查询字典类型列表
     */
    IPage<DictTypeVO> getDictTypeList(DictTypeQueryRequest request);

    /**
     * 根据ID获取字典类型详情
     */
    DictTypeVO getDictTypeById(String id);

    /**
     * 根据编码获取字典类型
     */
    DictTypeVO getDictTypeByCode(String code);

    /**
     * 创建字典类型
     */
    DictType createDictType(DictTypeCreateRequest request);

    /**
     * 更新字典类型
     */
    DictType updateDictType(String id, DictTypeUpdateRequest request);

    /**
     * 删除字典类型(级联删除字典项)
     */
    void deleteDictType(String id);

    /**
     * 检查字典编码是否存在
     */
    boolean checkDictCodeExists(String code, String excludeId);

    // ========== 字典项操作 ==========

    /**
     * 分页查询字典项列表
     */
    IPage<DictItemVO> getDictItemList(DictItemQueryRequest request);

    /**
     * 根据ID获取字典项详情
     */
    DictItemVO getDictItemById(String id);

    /**
     * 创建字典项
     */
    DictItem createDictItem(DictItemCreateRequest request);

    /**
     * 更新字典项
     */
    DictItem updateDictItem(String id, DictItemUpdateRequest request);

    /**
     * 删除字典项
     */
    void deleteDictItem(String id);

    /**
     * 批量删除字典项
     */
    void batchDeleteDictItems(List<String> ids);

    /**
     * 批量更新字典项状态
     */
    void batchUpdateDictItemStatus(List<String> ids, String status);

    /**
     * 批量更新字典项排序
     */
    void batchUpdateDictItemSort(DictItemSortUpdateRequest request);

    /**
     * 检查字典项值是否存在
     */
    boolean checkDictValueExists(String dictTypeId, String value, String excludeId);

    // ========== 字典树和数据 ==========

    /**
     * 获取字典树
     */
    List<DictTreeVO> getDictTree(String category, String status);

    /**
     * 根据字典类型编码获取字典数据(带缓存)
     */
    DictDataVO getDictData(String dictTypeCode);

    /**
     * 清除字典缓存
     */
    void clearDictCache(String dictTypeCode);
}
