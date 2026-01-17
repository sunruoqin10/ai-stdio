package com.example.oa_system_backend.module.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oa_system_backend.module.asset.entity.AssetBorrowRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资产借还记录Mapper接口
 */
@Mapper
public interface AssetBorrowRecordMapper extends BaseMapper<AssetBorrowRecord> {
}
