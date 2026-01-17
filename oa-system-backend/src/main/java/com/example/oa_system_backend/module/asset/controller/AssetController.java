package com.example.oa_system_backend.module.asset.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.asset.dto.request.*;
import com.example.oa_system_backend.module.asset.dto.response.AssetResponse;
import com.example.oa_system_backend.module.asset.dto.response.AssetStatisticsResponse;
import com.example.oa_system_backend.module.asset.entity.Asset;
import com.example.oa_system_backend.module.asset.service.AssetService;
import com.example.oa_system_backend.module.asset.vo.AssetBorrowRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 资产管理控制器
 */
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    /**
     * 分页查询资产列表
     * GET /api/assets
     */
    @GetMapping
    public ApiResponse<IPage<Asset>> getAssetList(AssetQueryRequest request) {
        IPage<Asset> result = assetService.getAssetList(request);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID查询资产详情
     * GET /api/assets/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<AssetResponse> getAssetById(@PathVariable String id) {
        AssetResponse response = assetService.getAssetById(id);
        return ApiResponse.success(response);
    }

    /**
     * 创建资产
     * POST /api/assets
     */
    @PostMapping
    public ApiResponse<AssetResponse> createAsset(@Valid @RequestBody AssetCreateRequest request) {
        AssetResponse response = assetService.createAsset(request);
        return ApiResponse.success("创建成功", response);
    }

    /**
     * 更新资产
     * PUT /api/assets/{id}
     */
    @PutMapping("/{id}")
    public ApiResponse<AssetResponse> updateAsset(
            @PathVariable String id,
            @Valid @RequestBody AssetUpdateRequest request) {
        AssetResponse response = assetService.updateAsset(id, request);
        return ApiResponse.success("更新成功", response);
    }

    /**
     * 删除资产
     * DELETE /api/assets/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAsset(@PathVariable String id) {
        assetService.deleteAsset(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 获取资产统计信息
     * GET /api/assets/statistics
     */
    @GetMapping("/statistics")
    public ApiResponse<AssetStatisticsResponse> getStatistics() {
        AssetStatisticsResponse response = assetService.getStatistics();
        return ApiResponse.success(response);
    }

    /**
     * 借用资产
     * POST /api/assets/{id}/borrow
     */
    @PostMapping("/{id}/borrow")
    public ApiResponse<AssetResponse> borrowAsset(
            @PathVariable String id,
            @Valid @RequestBody AssetBorrowRequest request) {
        AssetResponse response = assetService.borrowAsset(id, request);
        return ApiResponse.success("借出成功", response);
    }

    /**
     * 归还资产
     * POST /api/assets/{id}/return
     */
    @PostMapping("/{id}/return")
    public ApiResponse<AssetResponse> returnAsset(
            @PathVariable String id,
            @Valid @RequestBody AssetReturnRequest request) {
        AssetResponse response = assetService.returnAsset(id, request);
        return ApiResponse.success("归还成功", response);
    }

    /**
     * 获取资产借用历史
     * GET /api/assets/{id}/borrow-history
     */
    @GetMapping("/{id}/borrow-history")
    public ApiResponse<IPage<AssetBorrowRecordVO>> getBorrowHistory(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<AssetBorrowRecordVO> result = assetService.getBorrowHistory(id, pageNum, pageSize);
        return ApiResponse.success(result);
    }
}
