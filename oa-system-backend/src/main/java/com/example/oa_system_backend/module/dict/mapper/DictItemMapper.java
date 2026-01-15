package com.example.oa_system_backend.module.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.dict.entity.DictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 字典项Mapper接口
 */
@Mapper
public interface DictItemMapper extends BaseMapper<DictItem> {

    /**
     * 分页查询字典项列表
     */
    @Select("<script>" +
            "SELECT * FROM sys_dict_item " +
            "WHERE is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (label LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR value LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='dictTypeId != null and dictTypeId != \"\"'>" +
            "  AND dict_type_id = #{dictTypeId} " +
            "</if>" +
            "<if test='dictTypeCode != null and dictTypeCode != \"\"'>" +
            "  AND dict_type_code = #{dictTypeCode} " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "  AND status = #{status} " +
            "</if>" +
            "ORDER BY sort, id" +
            "</script>")
    IPage<DictItem> selectPageByQuery(
        Page<DictItem> page,
        @Param("keyword") String keyword,
        @Param("dictTypeId") String dictTypeId,
        @Param("dictTypeCode") String dictTypeCode,
        @Param("status") String status
    );

    /**
     * 根据字典类型编码查询启用的字典项
     */
    @Select("SELECT * FROM sys_dict_item " +
            "WHERE dict_type_code = #{dictTypeCode} " +
            "AND is_deleted = 0 " +
            "AND status = 'enabled' " +
            "ORDER BY sort, id")
    List<DictItem> selectEnabledByDictTypeCode(@Param("dictTypeCode") String dictTypeCode);

    /**
     * 根据字典类型ID查询所有字典项
     */
    @Select("SELECT * FROM sys_dict_item " +
            "WHERE dict_type_id = #{dictTypeId} " +
            "AND is_deleted = 0 " +
            "ORDER BY sort, id")
    List<DictItem> selectByDictTypeId(@Param("dictTypeId") Long dictTypeId);

    /**
     * 检查字典项值是否存在(在同一字典类型下)
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_dict_item " +
            "WHERE dict_type_id = #{dictTypeId} " +
            "AND value = #{value} " +
            "AND is_deleted = 0 " +
            "<if test='excludeId != null'>" +
            "AND id != #{excludeId} " +
            "</if>" +
            "</script>")
    Integer countByValue(@Param("dictTypeId") Long dictTypeId,
                        @Param("value") String value,
                        @Param("excludeId") String excludeId);

    /**
     * 检查字典类型是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_dict_type " +
            "WHERE id = #{dictTypeId} " +
            "AND is_deleted = 0")
    Integer countByDictTypeId(@Param("dictTypeId") Long dictTypeId);

    /**
     * 批量更新排序
     */
    void batchUpdateSort(@Param("dictTypeId") Long dictTypeId,
                        @Param("items") List<SortItem> items);

    /**
     * 排序项内部类
     */
    class SortItem {
        private String id;
        private Integer sortOrder;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }
    }
}
