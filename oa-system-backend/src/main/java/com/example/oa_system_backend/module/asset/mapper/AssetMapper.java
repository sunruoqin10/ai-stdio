package com.example.oa_system_backend.module.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.asset.entity.Asset;
import com.example.oa_system_backend.module.asset.vo.AssetVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 资产Mapper接口
 */
@Mapper
public interface AssetMapper extends BaseMapper<Asset> {

    /**
     * 分页查询资产视图对象
     *
     * @param page            分页对象
     * @param keyword         关键词
     * @param category        类别
     * @param status          状态
     * @param userId          使用人ID
     * @param purchaseDateStart 购置日期开始
     * @param purchaseDateEnd   购置日期结束
     * @param purchasePriceMin  购置金额最小值
     * @param purchasePriceMax  购置金额最大值
     * @param location       存放位置
     * @param sortOrder       排序方式
     * @return 资产视图对象分页
     */
    IPage<AssetVO> selectPageWithDetails(
            Page<AssetVO> page,
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("status") String status,
            @Param("userId") String userId,
            @Param("purchaseDateStart") String purchaseDateStart,
            @Param("purchaseDateEnd") String purchaseDateEnd,
            @Param("purchasePriceMin") BigDecimal purchasePriceMin,
            @Param("purchasePriceMax") BigDecimal purchasePriceMax,
            @Param("location") String location,
            @Param("sortOrder") String sortOrder
    );
}
