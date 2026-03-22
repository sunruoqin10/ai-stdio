package com.example.oa_system_backend.module.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.dict.entity.DictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典类型Mapper接口
 */
@Mapper
public interface DictTypeMapper extends BaseMapper<DictType> {

    IPage<DictType> selectPageByQuery(
        Page<DictType> page,
        @Param("keyword") String keyword,
        @Param("category") String category,
        @Param("status") String status
    );

    Integer countByCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    DictType selectByCode(@Param("code") String code);

    void updateItemCount(@Param("dictTypeId") Long dictTypeId);

    List<DictType> selectAll();
}
