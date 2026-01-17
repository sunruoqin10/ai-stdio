package com.example.oa_system_backend.module.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.common.exception.BusinessException;
import com.example.oa_system_backend.module.asset.dto.request.*;
import com.example.oa_system_backend.module.asset.dto.response.AssetResponse;
import com.example.oa_system_backend.module.asset.dto.response.AssetStatisticsResponse;
import com.example.oa_system_backend.module.asset.entity.Asset;
import com.example.oa_system_backend.module.asset.entity.AssetBorrowRecord;
import com.example.oa_system_backend.module.asset.enums.*;
import com.example.oa_system_backend.module.asset.mapper.AssetBorrowRecordMapper;
import com.example.oa_system_backend.module.asset.mapper.AssetMapper;
import com.example.oa_system_backend.module.asset.service.AssetService;
import com.example.oa_system_backend.module.asset.util.AssetDepreciationUtil;
import com.example.oa_system_backend.module.asset.vo.AssetBorrowRecordVO;
import com.example.oa_system_backend.module.employee.entity.Employee;
import com.example.oa_system_backend.module.employee.mapper.EmployeeMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资产服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetMapper assetMapper;
    private final AssetBorrowRecordMapper assetBorrowRecordMapper;
    private final EmployeeMapper employeeMapper;
    private final ObjectMapper objectMapper;

    @Override
    public IPage<Asset> getAssetList(AssetQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Asset> wrapper = buildQueryWrapper(request);

        // 分页查询
        Page<Asset> page = new Page<>(request.getPageNum(), request.getPageSize());
        return assetMapper.selectPage(page, wrapper);
    }

    @Override
    public AssetResponse getAssetById(String id) {
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }
        return convertToResponse(asset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetResponse createAsset(AssetCreateRequest request) {
        // 验证借用人是否存在
        if (request.getUserId() != null) {
            validateEmployeeExists(request.getUserId());
        }

        // 创建资产实体
        Asset asset = new Asset();
        BeanUtils.copyProperties(request, asset);
        asset.setId(generateAssetId());
        asset.setStatus(AssetStatusEnum.STOCK.getCode());
        asset.setCurrentValue(request.getPurchasePrice());

        // 处理图片列表
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                asset.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                log.error("图片列表JSON序列化失败", e);
                throw new BusinessException("图片数据格式错误");
            }
        }

        // 计算折旧
        calculateAssetCurrentValue(asset);

        // 插入数据库
        assetMapper.insert(asset);

        log.info("创建资产成功, assetId: {}", asset.getId());

        return getAssetById(asset.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetResponse updateAsset(String id, AssetUpdateRequest request) {
        // 查询现有资产
        Asset existingAsset = assetMapper.selectById(id);
        if (existingAsset == null) {
            throw new BusinessException("资产不存在");
        }

        // 验证乐观锁
        if (!existingAsset.getVersion().equals(request.getVersion())) {
            throw new BusinessException("数据已被修改,请刷新后重试");
        }

        // 验证状态转换是否合法
        if (request.getStatus() != null) {
            AssetStatusEnum currentStatus = AssetStatusEnum.fromCode(existingAsset.getStatus());
            AssetStatusEnum newStatus = AssetStatusEnum.fromCode(request.getStatus());
            if (!currentStatus.canTransitionTo(newStatus)) {
                throw new BusinessException("不能从" + currentStatus.getDescription()
                        + "状态转换到" + newStatus.getDescription() + "状态");
            }
        }

        // 验证借用人是否存在
        if (request.getUserId() != null) {
            validateEmployeeExists(request.getUserId());
        }

        // 更新资产信息
        Asset asset = new Asset();
        BeanUtils.copyProperties(request, asset);
        asset.setId(id);

        // 处理图片列表
        if (request.getImages() != null) {
            try {
                asset.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                log.error("图片列表JSON序列化失败", e);
                throw new BusinessException("图片数据格式错误");
            }
        }

        // 重新计算折旧
        calculateAssetCurrentValue(asset);

        // 更新数据库
        int rows = assetMapper.updateById(asset);
        if (rows == 0) {
            throw new BusinessException("更新资产失败");
        }

        log.info("更新资产成功, assetId: {}", id);

        return getAssetById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAsset(String id) {
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }

        // 检查资产是否可以删除 (只有在库状态才能删除)
        if (!AssetStatusEnum.STOCK.getCode().equals(asset.getStatus())) {
            throw new BusinessException("只有" + AssetStatusEnum.STOCK.getDescription()
                    + "状态的资产才能删除");
        }

        // 逻辑删除
        assetMapper.deleteById(id);

        log.info("删除资产成功, assetId: {}", id);
    }

    @Override
    public AssetStatisticsResponse getStatistics() {
        // 查询所有资产
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Asset::getIsDeleted, 0);
        List<Asset> assets = assetMapper.selectList(wrapper);

        AssetStatisticsResponse response = new AssetStatisticsResponse();

        // 总数
        response.setTotalCount((long) assets.size());

        // 总购置金额和当前价值
        BigDecimal totalPurchaseValue = assets.stream()
                .map(Asset::getPurchasePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalPurchaseValue(totalPurchaseValue);

        BigDecimal totalCurrentValue = assets.stream()
                .map(Asset::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalCurrentValue(totalCurrentValue);

        BigDecimal totalDepreciationAmount = totalPurchaseValue.subtract(totalCurrentValue);
        response.setTotalDepreciationAmount(totalDepreciationAmount);

        // 按类别统计
        List<AssetStatisticsResponse.CategoryStatistics> categoryStatistics = assets.stream()
                .collect(Collectors.groupingBy(Asset::getCategory))
                .entrySet().stream()
                .map(entry -> {
                    AssetStatisticsResponse.CategoryStatistics cs = new AssetStatisticsResponse.CategoryStatistics();
                    cs.setCategory(entry.getKey());
                    cs.setCategoryName(AssetCategoryEnum.getDescriptionByCode(entry.getKey()));
                    List<Asset> categoryAssets = entry.getValue();
                    cs.setCount((long) categoryAssets.size());
                    cs.setPurchaseValue(categoryAssets.stream()
                            .map(Asset::getPurchasePrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    cs.setCurrentValue(categoryAssets.stream()
                            .map(Asset::getCurrentValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    return cs;
                })
                .collect(Collectors.toList());
        response.setCategoryStatistics(categoryStatistics);

        // 按状态统计
        List<AssetStatisticsResponse.StatusStatistics> statusStatistics = assets.stream()
                .collect(Collectors.groupingBy(Asset::getStatus))
                .entrySet().stream()
                .map(entry -> {
                    AssetStatisticsResponse.StatusStatistics ss = new AssetStatisticsResponse.StatusStatistics();
                    ss.setStatus(entry.getKey());
                    ss.setStatusName(AssetStatusEnum.getDescriptionByCode(entry.getKey()));
                    List<Asset> statusAssets = entry.getValue();
                    ss.setCount((long) statusAssets.size());
                    ss.setValue(statusAssets.stream()
                            .map(Asset::getCurrentValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    return ss;
                })
                .collect(Collectors.toList());
        response.setStatusStatistics(statusStatistics);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void borrowAsset(String id, AssetBorrowRequest request) {
        // 查询资产
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }

        // 验证资产状态
        AssetStatusEnum currentStatus = AssetStatusEnum.fromCode(asset.getStatus());
        if (!currentStatus.canTransitionTo(AssetStatusEnum.BORROWED)) {
            throw new BusinessException("资产当前状态不能借出");
        }

        // 验证借用人
        Employee borrower = employeeMapper.selectById(request.getBorrowerId());
        if (borrower == null) {
            throw new BusinessException("借用人不存在");
        }

        // 检查借用数量限制
        long activeBorrowCount = assetBorrowRecordMapper.selectCount(
                new LambdaQueryWrapper<AssetBorrowRecord>()
                        .eq(AssetBorrowRecord::getBorrowerId, request.getBorrowerId())
                        .eq(AssetBorrowRecord::getStatus, BorrowStatusEnum.ACTIVE.getCode())
        );
        if (activeBorrowCount >= 5) {
            throw new BusinessException("该员工已借用5件资产, 达到借用上限");
        }

        // 验证归还日期
        if (request.getExpectedReturnDate().isBefore(request.getBorrowDate())) {
            throw new BusinessException("预计归还日期必须晚于借出日期");
        }

        // 计算借用天数 (最多90天)
        long borrowDays = java.time.temporal.ChronoUnit.DAYS.between(
                request.getBorrowDate(),
                request.getExpectedReturnDate()
        );
        if (borrowDays > 90) {
            throw new BusinessException("借用期限不能超过90天");
        }

        // 更新资产状态
        asset.setStatus(AssetStatusEnum.BORROWED.getCode());
        asset.setUserId(request.getBorrowerId());
        asset.setBorrowDate(request.getBorrowDate());
        asset.setExpectedReturnDate(request.getExpectedReturnDate());
        assetMapper.updateById(asset);

        // 创建借用记录
        AssetBorrowRecord record = new AssetBorrowRecord();
        record.setAssetId(id);
        record.setAssetName(asset.getName());
        record.setBorrowerId(request.getBorrowerId());
        record.setBorrowerName(borrower.getName());
        record.setBorrowDate(request.getBorrowDate());
        record.setExpectedReturnDate(request.getExpectedReturnDate());
        record.setStatus(BorrowStatusEnum.ACTIVE.getCode());
        record.setNotes(request.getNotes());
        assetBorrowRecordMapper.insert(record);

        log.info("资产借出成功, assetId: {}, borrowerId: {}", id, request.getBorrowerId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnAsset(String id, AssetReturnRequest request) {
        // 查询资产
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }

        // 验证资产状态
        if (!AssetStatusEnum.BORROWED.getCode().equals(asset.getStatus())) {
            throw new BusinessException("资产当前不是借出状态");
        }

        // 查询有效的借用记录
        LambdaQueryWrapper<AssetBorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetBorrowRecord::getAssetId, id)
                .eq(AssetBorrowRecord::getStatus, BorrowStatusEnum.ACTIVE.getCode())
                .orderByDesc(AssetBorrowRecord::getBorrowDate)
                .last("LIMIT 1");

        AssetBorrowRecord record = assetBorrowRecordMapper.selectOne(wrapper);
        if (record == null) {
            throw new BusinessException("未找到有效的借用记录");
        }

        // 更新借用记录
        record.setActualReturnDate(request.getActualReturnDate());
        record.setStatus(BorrowStatusEnum.RETURNED.getCode());
        if (request.getNotes() != null) {
            record.setNotes(record.getNotes() + "; 归还备注: " + request.getNotes());
        }
        assetBorrowRecordMapper.updateById(record);

        // 更新资产状态
        asset.setStatus(AssetStatusEnum.STOCK.getCode());
        asset.setUserId(null);
        asset.setActualReturnDate(request.getActualReturnDate());
        assetMapper.updateById(asset);

        log.info("资产归还成功, assetId: {}", id);
    }

    @Override
    public IPage<AssetBorrowRecordVO> getBorrowHistory(String id, Integer pageNum, Integer pageSize) {
        // 查询资产的借用记录
        LambdaQueryWrapper<AssetBorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetBorrowRecord::getAssetId, id)
                .orderByDesc(AssetBorrowRecord::getBorrowDate);

        Page<AssetBorrowRecord> page = new Page<>(pageNum, pageSize);
        IPage<AssetBorrowRecord> recordPage = assetBorrowRecordMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<AssetBorrowRecordVO> voPage = new Page<>();
        voPage.setRecords(recordPage.getRecords().stream()
                .map(record -> {
                    AssetBorrowRecordVO vo = new AssetBorrowRecordVO();
                    BeanUtils.copyProperties(record, vo);
                    vo.setStatusName(BorrowStatusEnum.getDescriptionByCode(record.getStatus()));
                    return vo;
                })
                .collect(Collectors.toList()));
        voPage.setTotal(recordPage.getTotal());
        voPage.setCurrent(recordPage.getCurrent());
        voPage.setSize(recordPage.getSize());

        return voPage;
    }

    @Override
    public void calculateAssetCurrentValue(String assetId) {
        Asset asset = assetMapper.selectById(assetId);
        if (asset != null) {
            calculateAssetCurrentValue(asset);
            assetMapper.updateById(asset);
        }
    }

    @Override
    public void batchCalculateAllAssetCurrentValue() {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Asset::getStatus, AssetStatusEnum.SCRAPPED.getCode());

        List<Asset> assets = assetMapper.selectList(wrapper);
        for (Asset asset : assets) {
            calculateAssetCurrentValue(asset);
            assetMapper.updateById(asset);
        }

        log.info("批量计算资产折旧完成, 共{}个资产", assets.size());
    }

    @Override
    public String generateAssetId() {
        // 查询当前最大ID
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Asset::getId).last("LIMIT 1");
        Asset lastAsset = assetMapper.selectOne(wrapper);

        int sequence = 1;
        if (lastAsset != null && lastAsset.getId() != null && lastAsset.getId().startsWith("ASSET")) {
            String lastSequence = lastAsset.getId().substring(5);
            try {
                sequence = Integer.parseInt(lastSequence) + 1;
            } catch (NumberFormatException e) {
                log.warn("解析最后资产ID失败: {}", lastAsset.getId());
                sequence = 1;
            }
        }

        return String.format("ASSET%06d", sequence);
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Asset> buildQueryWrapper(AssetQueryRequest request) {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Asset::getName, request.getKeyword())
                    .or().like(Asset::getId, request.getKeyword())
                    .or().like(Asset::getBrandModel, request.getKeyword()));
        }

        // 类别过滤
        if (request.getCategory() != null) {
            wrapper.eq(Asset::getCategory, request.getCategory());
        }

        // 状态过滤
        if (request.getStatus() != null) {
            wrapper.eq(Asset::getStatus, request.getStatus());
        }

        // 使用人过滤
        if (request.getUserId() != null) {
            wrapper.eq(Asset::getUserId, request.getUserId());
        }

        // 购置日期范围
        if (request.getPurchaseDateStart() != null) {
            wrapper.ge(Asset::getPurchaseDate, request.getPurchaseDateStart());
        }
        if (request.getPurchaseDateEnd() != null) {
            wrapper.le(Asset::getPurchaseDate, request.getPurchaseDateEnd());
        }

        // 购置金额范围
        if (request.getPurchasePriceMin() != null) {
            wrapper.ge(Asset::getPurchasePrice, request.getPurchasePriceMin());
        }
        if (request.getPurchasePriceMax() != null) {
            wrapper.le(Asset::getPurchasePrice, request.getPurchasePriceMax());
        }

        // 存放位置
        if (request.getLocation() != null) {
            wrapper.like(Asset::getLocation, request.getLocation());
        }

        // 排序
        if ("asc".equalsIgnoreCase(request.getSortOrder())) {
            wrapper.orderByAsc(Asset::getCreatedAt);
        } else {
            wrapper.orderByDesc(Asset::getCreatedAt);
        }

        return wrapper;
    }

    /**
     * 计算资产当前价值(折旧)
     */
    private void calculateAssetCurrentValue(Asset asset) {
        BigDecimal currentValue = AssetDepreciationUtil.calculateDepreciation(
                asset.getCategory(),
                asset.getPurchaseDate(),
                asset.getPurchasePrice()
        );
        asset.setCurrentValue(currentValue);
    }

    /**
     * 验证员工是否存在
     */
    private void validateEmployeeExists(String employeeId) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
    }

    /**
     * 转换为响应对象
     */
    private AssetResponse convertToResponse(Asset asset) {
        AssetResponse response = new AssetResponse();
        BeanUtils.copyProperties(asset, response);

        // 设置枚举描述
        response.setCategoryName(AssetCategoryEnum.getDescriptionByCode(asset.getCategory()));
        response.setStatusName(AssetStatusEnum.getDescriptionByCode(asset.getStatus()));

        // 计算折旧信息
        BigDecimal depreciationAmount = asset.getPurchasePrice()
                .subtract(asset.getCurrentValue());
        response.setDepreciationAmount(depreciationAmount);

        BigDecimal depreciationRate = asset.getPurchasePrice().compareTo(BigDecimal.ZERO) > 0
                ? depreciationAmount.divide(asset.getPurchasePrice(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;
        response.setDepreciationRate(depreciationRate);

        // 计算持有年限
        int ownedYears = Period.between(asset.getPurchaseDate(), LocalDate.now()).getYears();
        response.setOwnedYears(ownedYears);

        // 判断是否超期
        if (asset.getExpectedReturnDate() != null
                && AssetStatusEnum.BORROWED.getCode().equals(asset.getStatus())) {
            boolean isOverdue = LocalDate.now().isAfter(asset.getExpectedReturnDate());
            response.setIsOverdue(isOverdue);
            if (isOverdue) {
                int overdueDays = Period.between(asset.getExpectedReturnDate(), LocalDate.now()).getDays();
                response.setOverdueDays(overdueDays);
            }
        }

        // 解析图片列表
        if (asset.getImages() != null && !asset.getImages().isEmpty()) {
            try {
                List<String> imageList = objectMapper.readValue(asset.getImages(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                response.setImages(imageList);
            } catch (JsonProcessingException e) {
                log.error("图片列表JSON反序列化失败", e);
            }
        }

        // 获取借用人信息
        if (asset.getUserId() != null) {
            Employee employee = employeeMapper.selectById(asset.getUserId());
            if (employee != null) {
                response.setUserName(employee.getName());
                response.setUserAvatar(employee.getAvatar());
                response.setDepartmentId(employee.getDepartmentId());
            }
        }

        return response;
    }
}
