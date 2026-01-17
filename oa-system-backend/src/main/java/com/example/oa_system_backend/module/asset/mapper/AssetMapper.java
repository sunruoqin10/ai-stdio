package com.example.oa_system_backend.module.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.asset.entity.Asset;
import com.example.oa_system_backend.module.asset.vo.AssetVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 资产Mapper接口
 */
@Mapper
public interface AssetMapper extends BaseMapper<Asset> {

    /**
     * 分页查询资产视图对象
     *
     * @param page    分页对象
     * @param keyword 关键词
     * @return 资产视图对象分页
     */
    IPage<AssetVO> selectPageWithDetails(Page<AssetVO> page, @Param("keyword") String keyword);
}
