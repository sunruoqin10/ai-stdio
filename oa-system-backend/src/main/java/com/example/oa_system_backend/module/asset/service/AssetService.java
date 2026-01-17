package com.example.oa_system_backend.module.asset.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.module.asset.dto.request.*;
import com.example.oa_system_backend.module.asset.dto.response.AssetResponse;
import com.example.oa_system_backend.module.asset.dto.response.AssetStatisticsResponse;
import com.example.oa_system_backend.module.asset.entity.Asset;
import com.example.oa_system_backend.module.asset.vo.AssetBorrowRecordVO;

/**
 * 资产服务接口
 */
public interface AssetService {

    /**
     * 分页查询资产列表
     */
    IPage<Asset> getAssetList(AssetQueryRequest request);

    /**
     * 根据ID查询资产详情
     */
    AssetResponse getAssetById(String id);

    /**
     * 创建资产
     */
    AssetResponse createAsset(AssetCreateRequest request);

    /**
     * 更新资产
     */
    AssetResponse updateAsset(String id, AssetUpdateRequest request);

    /**
     * 删除资产
     */
    void deleteAsset(String id);

    /**
     * 获取资产统计信息
     */
    AssetStatisticsResponse getStatistics();

    /**
     * 借用资产
     */
    AssetResponse borrowAsset(String id, AssetBorrowRequest request);

    /**
     * 归还资产
     */
    AssetResponse returnAsset(String id, AssetReturnRequest request);

    /**
     * 获取资产借用历史
     */
    IPage<AssetBorrowRecordVO> getBorrowHistory(String id, Integer pageNum, Integer pageSize);

    /**
     * 计算资产当前价值(折旧)
     */
    void calculateAssetCurrentValue(String assetId);

    /**
     * 批量计算所有资产当前价值
     */
    void batchCalculateAllAssetCurrentValue();

    /**
     * 生成资产ID
     */
    String generateAssetId();
}
