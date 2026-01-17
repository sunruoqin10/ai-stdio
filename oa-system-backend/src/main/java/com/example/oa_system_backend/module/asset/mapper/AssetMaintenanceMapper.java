package com.example.oa_system_backend.module.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oa_system_backend.module.asset.entity.AssetMaintenance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资产维护记录Mapper接口
 */
@Mapper
public interface AssetMaintenanceMapper extends BaseMapper<AssetMaintenance> {
}
