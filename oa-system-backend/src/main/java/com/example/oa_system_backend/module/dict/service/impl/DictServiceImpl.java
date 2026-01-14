package com.example.oa_system_backend.module.dict.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.common.exception.BusinessException;
import com.example.oa_system_backend.module.dict.dto.*;
import com.example.oa_system_backend.module.dict.entity.DictItem;
import com.example.oa_system_backend.module.dict.entity.DictType;
import com.example.oa_system_backend.module.dict.mapper.DictItemMapper;
import com.example.oa_system_backend.module.dict.mapper.DictTypeMapper;
import com.example.oa_system_backend.module.dict.service.DictService;
import com.example.oa_system_backend.module.dict.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据字典服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final DictTypeMapper dictTypeMapper;
    private final DictItemMapper dictItemMapper;

    // 使用内存缓存替代 Redis
    private final Map<String, DictDataVO> dictCache = new ConcurrentHashMap<>();

    // ========== 字典类型操作 ==========

    @Override
    public IPage<DictTypeVO> getDictTypeList(DictTypeQueryRequest request) {
        Page<DictType> page = new Page<>(request.getPage(), request.getPageSize());
        IPage<DictType> dictTypePage = dictTypeMapper.selectPageByQuery(
            page,
            request.getKeyword(),
            request.getCategory(),
            request.getStatus()
        );

        // 转换为VO
        return dictTypePage.convert(dictType -> {
            DictTypeVO vo = new DictTypeVO();
            BeanUtils.copyProperties(dictType, vo);
            return vo;
        });
    }

    @Override
    public DictTypeVO getDictTypeById(String id) {
        DictType dictType = dictTypeMapper.selectById(id);
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }

        DictTypeVO vo = new DictTypeVO();
        BeanUtils.copyProperties(dictType, vo);
        return vo;
    }

    @Override
    public DictTypeVO getDictTypeByCode(String code) {
        DictType dictType = dictTypeMapper.selectByCode(code);
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }

        DictTypeVO vo = new DictTypeVO();
        BeanUtils.copyProperties(dictType, vo);
        return vo;
    }

    @Override
    @Transactional
    public DictType createDictType(DictTypeCreateRequest request) {
        // 1. 验证字典编码唯一性
        if (dictTypeMapper.countByCode(request.getCode(), null) > 0) {
            throw new BusinessException("字典编码已存在: " + request.getCode());
        }

        // 2. 生成字典类型ID
        String dictTypeId = generateDictTypeId();

        // 3. 构建DictType实体
        DictType dictType = new DictType();
        BeanUtils.copyProperties(request, dictType);
        dictType.setId(dictTypeId);
        dictType.setItemCount(0);
        dictType.setStatus(request.getStatus() != null ? request.getStatus() : "enabled");
        dictType.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        dictType.setCreatedAt(LocalDateTime.now());
        dictType.setUpdatedAt(LocalDateTime.now());

        // 4. 保存到数据库
        dictTypeMapper.insert(dictType);

        return dictType;
    }

    @Override
    @Transactional
    public DictType updateDictType(String id, DictTypeUpdateRequest request) {
        // 1. 检查字典类型是否存在
        DictType dictType = dictTypeMapper.selectById(id);
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }

        // 2. 更新字段
        if (request.getName() != null) {
            dictType.setName(request.getName());
        }
        if (request.getDescription() != null) {
            dictType.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            dictType.setCategory(request.getCategory());
        }
        if (request.getStatus() != null) {
            dictType.setStatus(request.getStatus());
        }
        if (request.getSortOrder() != null) {
            dictType.setSortOrder(request.getSortOrder());
        }
        if (request.getExtProps() != null) {
            dictType.setExtProps(request.getExtProps());
        }
        if (request.getRemark() != null) {
            dictType.setRemark(request.getRemark());
        }
        dictType.setUpdatedAt(LocalDateTime.now());

        // 3. 保存更新
        dictTypeMapper.updateById(dictType);

        // 4. 清除缓存
        clearDictCache(dictType.getCode());

        return dictType;
    }

    @Override
    @Transactional
    public void deleteDictType(String id) {
        // 1. 检查字典类型是否存在
        DictType dictType = dictTypeMapper.selectById(id);
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }

        // 2. 检查是否为系统字典
        if ("system".equals(dictType.getCategory())) {
            throw new BusinessException("系统字典不允许删除");
        }

        // 3. 软删除字典类型
        dictTypeMapper.deleteById(id);

        // 4. 软删除所有关联的字典项
        List<DictItem> items = dictItemMapper.selectByDictTypeId(id);
        for (DictItem item : items) {
            dictItemMapper.deleteById(item.getId());
        }

        // 5. 清除缓存
        clearDictCache(dictType.getCode());
    }

    @Override
    public boolean checkDictCodeExists(String code, String excludeId) {
        return dictTypeMapper.countByCode(code, excludeId) > 0;
    }

    // ========== 字典项操作 ==========

    @Override
    public IPage<DictItemVO> getDictItemList(DictItemQueryRequest request) {
        Page<DictItem> page = new Page<>(request.getPage(), request.getPageSize());
        IPage<DictItem> dictItemPage = dictItemMapper.selectPageByQuery(
            page,
            request.getKeyword(),
            request.getDictTypeId(),
            request.getDictTypeCode(),
            request.getStatus()
        );

        // 转换为VO
        return dictItemPage.convert(dictItem -> {
            DictItemVO vo = new DictItemVO();
            BeanUtils.copyProperties(dictItem, vo);
            return vo;
        });
    }

    @Override
    public DictItemVO getDictItemById(String id) {
        DictItem dictItem = dictItemMapper.selectById(id);
        if (dictItem == null) {
            throw new BusinessException("字典项不存在");
        }

        DictItemVO vo = new DictItemVO();
        BeanUtils.copyProperties(dictItem, vo);
        return vo;
    }

    @Override
    @Transactional
    public DictItem createDictItem(DictItemCreateRequest request) {
        // 1. 验证字典类型存在性
        if (dictItemMapper.countByDictTypeId(request.getDictTypeId()) == 0) {
            throw new BusinessException("指定的字典类型不存在");
        }

        // 2. 验证字典项值唯一性(在同一字典类型下)
        if (dictItemMapper.countByValue(request.getDictTypeId(),
                                       request.getValue(), null) > 0) {
            throw new BusinessException("字典项值已存在: " + request.getValue());
        }

        // 3. 查询字典类型以获取编码
        DictType dictType = dictTypeMapper.selectById(request.getDictTypeId());
        if (dictType == null) {
            throw new BusinessException("指定的字典类型不存在");
        }

        // 4. 生成字典项ID
        String dictItemId = generateDictItemId();

        // 5. 构建DictItem实体
        DictItem dictItem = new DictItem();
        BeanUtils.copyProperties(request, dictItem);
        dictItem.setId(dictItemId);
        dictItem.setDictTypeCode(dictType.getCode());
        dictItem.setStatus(request.getStatus() != null ? request.getStatus() : "enabled");
        dictItem.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        dictItem.setCreatedAt(LocalDateTime.now());
        dictItem.setUpdatedAt(LocalDateTime.now());

        // 6. 保存到数据库
        dictItemMapper.insert(dictItem);

        // 7. 更新字典类型的item_count
        updateDictTypeItemCount(request.getDictTypeId());

        // 8. 清除缓存
        clearDictCache(dictType.getCode());

        return dictItem;
    }

    @Override
    @Transactional
    public DictItem updateDictItem(String id, DictItemUpdateRequest request) {
        // 1. 检查字典项是否存在
        DictItem dictItem = dictItemMapper.selectById(id);
        if (dictItem == null) {
            throw new BusinessException("字典项不存在");
        }

        // 2. 如果要更新value,验证唯一性
        if (request.getValue() != null && !request.getValue().equals(dictItem.getValue())) {
            if (dictItemMapper.countByValue(dictItem.getDictTypeId(),
                                           request.getValue(), id) > 0) {
                throw new BusinessException("字典项值已存在: " + request.getValue());
            }
        }

        // 3. 更新字段
        if (request.getLabel() != null) {
            dictItem.setLabel(request.getLabel());
        }
        if (request.getValue() != null) {
            dictItem.setValue(request.getValue());
        }
        if (request.getColorType() != null) {
            dictItem.setColorType(request.getColorType());
        }
        if (request.getColor() != null) {
            dictItem.setColor(request.getColor());
        }
        if (request.getIcon() != null) {
            dictItem.setIcon(request.getIcon());
        }
        if (request.getSortOrder() != null) {
            dictItem.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            dictItem.setStatus(request.getStatus());
        }
        if (request.getExtProps() != null) {
            dictItem.setExtProps(request.getExtProps());
        }
        if (request.getRemark() != null) {
            dictItem.setRemark(request.getRemark());
        }
        dictItem.setUpdatedAt(LocalDateTime.now());

        // 4. 保存更新
        dictItemMapper.updateById(dictItem);

        // 5. 清除缓存
        clearDictCache(dictItem.getDictTypeCode());

        return dictItem;
    }

    @Override
    @Transactional
    public void deleteDictItem(String id) {
        // 1. 检查字典项是否存在
        DictItem dictItem = dictItemMapper.selectById(id);
        if (dictItem == null) {
            throw new BusinessException("字典项不存在");
        }

        String dictTypeId = dictItem.getDictTypeId();
        String dictTypeCode = dictItem.getDictTypeCode();

        // 2. 软删除字典项
        dictItemMapper.deleteById(id);

        // 3. 更新字典类型的item_count
        updateDictTypeItemCount(dictTypeId);

        // 4. 清除缓存
        clearDictCache(dictTypeCode);
    }

    @Override
    @Transactional
    public void batchDeleteDictItems(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("ID列表不能为空");
        }

        String dictTypeCode = null;
        String dictTypeId = null;

        for (String id : ids) {
            DictItem dictItem = dictItemMapper.selectById(id);
            if (dictItem != null) {
                dictTypeCode = dictItem.getDictTypeCode();
                dictTypeId = dictItem.getDictTypeId();
                dictItemMapper.deleteById(id);
            }
        }

        // 更新字典类型的item_count
        if (dictTypeId != null) {
            updateDictTypeItemCount(dictTypeId);
        }

        // 清除缓存
        if (dictTypeCode != null) {
            clearDictCache(dictTypeCode);
        }
    }

    @Override
    @Transactional
    public void batchUpdateDictItemStatus(List<String> ids, String status) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("ID列表不能为空");
        }

        String dictTypeCode = null;

        for (String id : ids) {
            DictItem dictItem = dictItemMapper.selectById(id);
            if (dictItem != null) {
                dictItem.setStatus(status);
                dictItem.setUpdatedAt(LocalDateTime.now());
                dictItemMapper.updateById(dictItem);
                if (dictTypeCode == null) {
                    dictTypeCode = dictItem.getDictTypeCode();
                }
            }
        }

        // 清除缓存
        if (dictTypeCode != null) {
            clearDictCache(dictTypeCode);
        }
    }

    @Override
    @Transactional
    public void batchUpdateDictItemSort(DictItemSortUpdateRequest request) {
        // 1. 验证字典类型存在性
        if (dictItemMapper.countByDictTypeId(request.getDictTypeId()) == 0) {
            throw new BusinessException("指定的字典类型不存在");
        }

        // 2. 转换排序项
        List<DictItemMapper.SortItem> sortItems = request.getItems().stream()
            .map(item -> {
                DictItemMapper.SortItem sortItem = new DictItemMapper.SortItem();
                sortItem.setId(item.getId());
                sortItem.setSortOrder(item.getSortOrder());
                return sortItem;
            })
            .collect(Collectors.toList());

        // 3. 批量更新排序
        dictItemMapper.batchUpdateSort(request.getDictTypeId(), sortItems);

        // 4. 清除缓存
        DictType dictType = dictTypeMapper.selectById(request.getDictTypeId());
        if (dictType != null) {
            clearDictCache(dictType.getCode());
        }
    }

    @Override
    public boolean checkDictValueExists(String dictTypeId, String value, String excludeId) {
        return dictItemMapper.countByValue(dictTypeId, value, excludeId) > 0;
    }

    // ========== 字典树和数据 ==========

    @Override
    public List<DictTreeVO> getDictTree(String category, String status) {
        // 查询字典类型列表
        Page<DictType> page = new Page<>(1, 1000);
        IPage<DictType> dictTypePage = dictTypeMapper.selectPageByQuery(
            page,
            null,
            category,
            status
        );

        // 转换为树形结构
        return dictTypePage.getRecords().stream()
            .map(dictType -> {
                DictTreeVO treeVO = new DictTreeVO();
                BeanUtils.copyProperties(dictType, treeVO);

                // 查询该字典类型下的所有字典项
                List<DictItem> items = dictItemMapper.selectByDictTypeId(dictType.getId());
                List<DictItemVO> itemVOs = items.stream()
                    .map(item -> {
                        DictItemVO itemVO = new DictItemVO();
                        BeanUtils.copyProperties(item, itemVO);
                        return itemVO;
                    })
                    .collect(Collectors.toList());

                treeVO.setItems(itemVOs);
                // 动态设置itemCount为实际的字典项数量
                treeVO.setItemCount(items.size());
                return treeVO;
            })
            .collect(Collectors.toList());
    }

    @Override
    public DictDataVO getDictData(String dictTypeCode) {
        // 1. 尝试从内存缓存获取
        DictDataVO cachedData = dictCache.get(dictTypeCode);
        if (cachedData != null) {
            log.debug("从内存缓存获取字典数据: dictTypeCode={}", dictTypeCode);
            return cachedData;
        }

        // 2. 查询数据库
        List<DictItem> items = dictItemMapper.selectEnabledByDictTypeCode(dictTypeCode);

        // 3. 转换为VO
        List<DictItemVO> itemVOs = items.stream()
            .map(item -> {
                DictItemVO vo = new DictItemVO();
                BeanUtils.copyProperties(item, vo);
                return vo;
            })
            .collect(Collectors.toList());

        // 4. 构建返回数据
        DictDataVO dictData = new DictDataVO();
        dictData.setDictType(dictTypeCode);
        dictData.setItems(itemVOs);

        // 5. 设置内存缓存
        dictCache.put(dictTypeCode, dictData);
        log.debug("设置字典数据缓存: dictTypeCode={}, items={}", dictTypeCode, items.size());

        return dictData;
    }

    @Override
    public void clearDictCache(String dictTypeCode) {
        dictCache.remove(dictTypeCode);
        log.info("清除字典缓存: dictTypeCode={}", dictTypeCode);
    }

    // ========== 私有方法 ==========

    /**
     * 生成字典类型ID
     */
    private String generateDictTypeId() {
        return "DICT_" + System.currentTimeMillis();
    }

    /**
     * 生成字典项ID
     */
    private String generateDictItemId() {
        return "ITEM_" + System.currentTimeMillis();
    }

    /**
     * 更新字典类型的item_count
     */
    private void updateDictTypeItemCount(String dictTypeId) {
        dictTypeMapper.updateItemCount(dictTypeId);
    }
}
