package com.example.oa_system_backend.module.asset.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.oa_system_backend.module.asset.entity.AssetBorrowRecord;
import com.example.oa_system_backend.module.asset.enums.BorrowStatusEnum;
import com.example.oa_system_backend.module.asset.mapper.AssetBorrowRecordMapper;
import com.example.oa_system_backend.module.asset.service.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 资产模块定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssetScheduledTask {

    private final AssetService assetService;
    private final AssetBorrowRecordMapper assetBorrowRecordMapper;

    /**
     * 自动计算折旧
     * 每月1号凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void calculateDepreciation() {
        log.info("开始执行资产折旧计算任务");
        try {
            assetService.batchCalculateAllAssetCurrentValue();
            log.info("资产折旧计算任务执行成功");
        } catch (Exception e) {
            log.error("资产折旧计算任务执行失败", e);
        }
    }

    /**
     * 超期借用检查
     * 每天早上9点执行
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkOverdueBorrows() {
        log.info("开始执行超期借用检查任务");
        try {
            checkAndUpdateOverdueRecords();
            log.info("超期借用检查任务执行成功");
        } catch (Exception e) {
            log.error("超期借用检查任务执行失败", e);
        }
    }

    /**
     * 检查并更新超期借用记录
     */
    private void checkAndUpdateOverdueRecords() {
        // 查询所有借出中且已超期的借用记录
        LambdaQueryWrapper<AssetBorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetBorrowRecord::getStatus, BorrowStatusEnum.ACTIVE.getCode())
                .lt(AssetBorrowRecord::getExpectedReturnDate, LocalDate.now());

        List<AssetBorrowRecord> overdueRecords = assetBorrowRecordMapper.selectList(wrapper);

        if (!overdueRecords.isEmpty()) {
            log.info("发现{}条超期借用记录", overdueRecords.size());

            // 更新所有超期记录的状态
            for (AssetBorrowRecord record : overdueRecords) {
                record.setStatus(BorrowStatusEnum.OVERDUE.getCode());
                assetBorrowRecordMapper.updateById(record);
            }

            log.info("已更新{}条超期借用记录状态", overdueRecords.size());
        } else {
            log.info("没有发现超期借用记录");
        }
    }
}
