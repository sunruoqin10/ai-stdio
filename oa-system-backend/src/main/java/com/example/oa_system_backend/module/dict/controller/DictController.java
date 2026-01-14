package com.example.oa_system_backend.module.dict.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.dict.dto.*;
import com.example.oa_system_backend.module.dict.entity.DictItem;
import com.example.oa_system_backend.module.dict.entity.DictType;
import com.example.oa_system_backend.module.dict.service.DictService;
import com.example.oa_system_backend.module.dict.vo.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据字典控制器
 */
@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    // ========== 字典类型接口 ==========

    /**
     * 获取字典类型列表
     * GET /api/dict/types
     */
    @GetMapping("/types")
    public ApiResponse<IPage<DictTypeVO>> getDictTypeList(DictTypeQueryRequest request) {
        IPage<DictTypeVO> result = dictService.getDictTypeList(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取字典类型详情
     * GET /api/dict/types/{id}
     */
    @GetMapping("/types/{id}")
    public ApiResponse<DictTypeVO> getDictTypeById(@PathVariable String id) {
        DictTypeVO dictType = dictService.getDictTypeById(id);
        return ApiResponse.success(dictType);
    }

    /**
     * 创建字典类型
     * POST /api/dict/types
     */
    @PostMapping("/types")
    public ApiResponse<DictType> createDictType(
            @Valid @RequestBody DictTypeCreateRequest request) {
        DictType dictType = dictService.createDictType(request);
        return ApiResponse.success("创建成功", dictType);
    }

    /**
     * 更新字典类型
     * PUT /api/dict/types/{id}
     */
    @PutMapping("/types/{id}")
    public ApiResponse<DictType> updateDictType(
            @PathVariable String id,
            @Valid @RequestBody DictTypeUpdateRequest request) {
        DictType dictType = dictService.updateDictType(id, request);
        return ApiResponse.success("更新成功", dictType);
    }

    /**
     * 删除字典类型
     * DELETE /api/dict/types/{id}
     */
    @DeleteMapping("/types/{id}")
    public ApiResponse<Void> deleteDictType(@PathVariable String id) {
        dictService.deleteDictType(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 检查字典编码是否存在
     * GET /api/dict/types/check-code
     */
    @GetMapping("/types/check-code")
    public ApiResponse<Boolean> checkDictCodeExists(
            @RequestParam String code,
            @RequestParam(required = false) String excludeId) {
        boolean exists = dictService.checkDictCodeExists(code, excludeId);
        return ApiResponse.success(exists);
    }

    // ========== 字典项接口 ==========

    /**
     * 获取字典项列表
     * GET /api/dict/items
     */
    @GetMapping("/items")
    public ApiResponse<IPage<DictItemVO>> getDictItemList(DictItemQueryRequest request) {
        IPage<DictItemVO> result = dictService.getDictItemList(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取字典项详情
     * GET /api/dict/items/{id}
     */
    @GetMapping("/items/{id}")
    public ApiResponse<DictItemVO> getDictItemById(@PathVariable String id) {
        DictItemVO dictItem = dictService.getDictItemById(id);
        return ApiResponse.success(dictItem);
    }

    /**
     * 创建字典项
     * POST /api/dict/items
     */
    @PostMapping("/items")
    public ApiResponse<DictItem> createDictItem(
            @Valid @RequestBody DictItemCreateRequest request) {
        DictItem dictItem = dictService.createDictItem(request);
        return ApiResponse.success("创建成功", dictItem);
    }

    /**
     * 更新字典项
     * PUT /api/dict/items/{id}
     */
    @PutMapping("/items/{id}")
    public ApiResponse<DictItem> updateDictItem(
            @PathVariable String id,
            @Valid @RequestBody DictItemUpdateRequest request) {
        DictItem dictItem = dictService.updateDictItem(id, request);
        return ApiResponse.success("更新成功", dictItem);
    }

    /**
     * 删除字典项
     * DELETE /api/dict/items/{id}
     */
    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> deleteDictItem(@PathVariable String id) {
        dictService.deleteDictItem(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 批量删除字典项
     * DELETE /api/dict/items/batch
     */
    @DeleteMapping("/items/batch")
    public ApiResponse<Void> batchDeleteDictItems(
            @RequestBody DictBatchOperationRequest request) {
        dictService.batchDeleteDictItems(request.getIds());
        return ApiResponse.success("批量删除成功", null);
    }

    /**
     * 批量更新字典项状态
     * PUT /api/dict/items/batch/status
     */
    @PutMapping("/items/batch/status")
    public ApiResponse<Void> batchUpdateDictItemStatus(
            @RequestBody DictBatchOperationRequest request) {
        dictService.batchUpdateDictItemStatus(request.getIds(), request.getStatus());
        return ApiResponse.success("批量更新状态成功", null);
    }

    /**
     * 批量更新字典项排序
     * PUT /api/dict/items/sort
     */
    @PutMapping("/items/sort")
    public ApiResponse<Void> batchUpdateDictItemSort(
            @Valid @RequestBody DictItemSortUpdateRequest request) {
        dictService.batchUpdateDictItemSort(request);
        return ApiResponse.success("批量更新排序成功", null);
    }

    /**
     * 检查字典项值是否存在
     * GET /api/dict/items/check-value
     */
    @GetMapping("/items/check-value")
    public ApiResponse<Boolean> checkDictValueExists(
            @RequestParam String dictTypeId,
            @RequestParam String value,
            @RequestParam(required = false) String excludeId) {
        boolean exists = dictService.checkDictValueExists(dictTypeId, value, excludeId);
        return ApiResponse.success(exists);
    }

    // ========== 字典树和数据接口 ==========

    /**
     * 获取字典树
     * GET /api/dict/tree
     */
    @GetMapping("/tree")
    public ApiResponse<List<DictTreeVO>> getDictTree(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        List<DictTreeVO> tree = dictService.getDictTree(category, status);
        return ApiResponse.success(tree);
    }

    /**
     * 根据字典类型编码获取字典数据
     * GET /api/dict/{code}
     */
    @GetMapping("/{code}")
    public ApiResponse<DictDataVO> getDictData(@PathVariable String code) {
        DictDataVO dictData = dictService.getDictData(code);
        return ApiResponse.success(dictData);
    }

    /**
     * 清除字典缓存
     * DELETE /api/dict/cache/{code}
     */
    @DeleteMapping("/cache/{code}")
    public ApiResponse<Void> clearDictCache(@PathVariable String code) {
        dictService.clearDictCache(code);
        return ApiResponse.success("清除缓存成功", null);
    }
}
