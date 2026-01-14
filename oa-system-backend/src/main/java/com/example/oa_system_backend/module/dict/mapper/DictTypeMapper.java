package com.example.oa_system_backend.module.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.dict.entity.DictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 字典类型Mapper接口
 */
@Mapper
public interface DictTypeMapper extends BaseMapper<DictType> {

    /**
     * 分页查询字典类型列表
     */
    @Select("<script>" +
            "SELECT * FROM sys_dict_type " +
            "WHERE is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (code LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='category != null and category != \"\"'>" +
            "  AND category = #{category} " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "  AND status = #{status} " +
            "</if>" +
            "ORDER BY sort, id" +
            "</script>")
    IPage<DictType> selectPageByQuery(
        Page<DictType> page,
        @Param("keyword") String keyword,
        @Param("category") String category,
        @Param("status") String status
    );

    /**
     * 检查字典编码是否存在
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_dict_type " +
            "WHERE code = #{code} " +
            "AND is_deleted = 0 " +
            "<if test='excludeId != null'>" +
            "AND id != #{excludeId} " +
            "</if>" +
            "</script>")
    Integer countByCode(@Param("code") String code,
                       @Param("excludeId") String excludeId);

    /**
     * 根据编码查询字典类型
     */
    @Select("SELECT * FROM sys_dict_type " +
            "WHERE code = #{code} " +
            "AND is_deleted = 0")
    DictType selectByCode(@Param("code") String code);

    /**
     * 更新字典项数量
     */
    @Update("UPDATE sys_dict_type " +
            "SET item_count = (" +
            "  SELECT COUNT(*) FROM sys_dict_item " +
            "  WHERE dict_type_id = #{dictTypeId} " +
            "  AND is_deleted = 0" +
            ") " +
            "WHERE id = #{dictTypeId}")
    void updateItemCount(@Param("dictTypeId") String dictTypeId);
}
