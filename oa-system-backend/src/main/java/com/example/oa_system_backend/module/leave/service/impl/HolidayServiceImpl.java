package com.example.oa_system_backend.module.leave.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oa_system_backend.common.exception.BusinessException;
import com.example.oa_system_backend.module.leave.dto.HolidayCreateRequest;
import com.example.oa_system_backend.module.leave.dto.HolidayQueryRequest;
import com.example.oa_system_backend.module.leave.entity.Holiday;
import com.example.oa_system_backend.module.leave.enums.HolidayType;
import com.example.oa_system_backend.module.leave.mapper.HolidayMapper;
import com.example.oa_system_backend.module.leave.service.HolidayService;
import com.example.oa_system_backend.module.leave.vo.HolidayVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayServiceImpl extends ServiceImpl<HolidayMapper, Holiday> implements HolidayService {

    private final HolidayMapper holidayMapper;
    private final com.example.oa_system_backend.module.leave.util.LeaveDictLabelUtil dictLabelUtil;

    @Override
    public List<HolidayVO> getHolidayList(HolidayQueryRequest query) {
        log.info("查询节假日列表,查询条件: {}", query);

        List<Holiday> list = holidayMapper.selectByCondition(
                query.getYear(),
                query.getType(),
                query.getStartDate(),
                query.getEndDate(),
                query.getKeyword()
        );

        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public HolidayVO getHolidayById(Long id) {
        Holiday holiday = holidayMapper.selectById(id);
        if (holiday == null) {
            return null;
        }
        return convertToVO(holiday);
    }

    @Override
    public List<HolidayVO> getHolidaysByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("查询日期范围内的节假日,开始日期: {}, 结束日期: {}", startDate, endDate);

        List<Holiday> list = holidayMapper.selectByCondition(
                null,
                null,
                startDate,
                endDate,
                null
        );

        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Holiday createHoliday(HolidayCreateRequest request) {
        log.info("添加节假日,创建信息: {}", request);

        Holiday existing = holidayMapper.selectByDate(request.getDate());
        if (existing != null) {
            throw new BusinessException("该日期已存在节假日记录");
        }

        Holiday holiday = new Holiday();
        BeanUtils.copyProperties(request, holiday);
        holidayMapper.insert(holiday);

        log.info("节假日添加成功");
        return holiday;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Holiday updateHoliday(Long id, HolidayCreateRequest request) {
        log.info("更新节假日,节假日ID: {}, 更新信息: {}", id, request);

        Holiday holiday = holidayMapper.selectById(id);
        if (holiday == null) {
            throw new BusinessException("节假日记录不存在");
        }

        Holiday existing = holidayMapper.selectByDate(request.getDate());
        if (existing != null && !existing.getId().equals(id)) {
            throw new BusinessException("该日期已存在节假日记录");
        }

        BeanUtils.copyProperties(request, holiday);
        holidayMapper.updateById(holiday);

        log.info("节假日更新成功");
        return holiday;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteHoliday(Long id) {
        log.info("删除节假日,节假日ID: {}", id);

        holidayMapper.deleteById(id);

        log.info("节假日删除成功");
    }

    @Override
    public Holiday getHolidayByDate(LocalDate date) {
        return holidayMapper.selectByDate(date);
    }

    public List<Holiday> getHolidaysByDate(LocalDate date) {
        Holiday holiday = holidayMapper.selectByDate(date);
        return holiday != null ? java.util.Collections.singletonList(holiday) : java.util.Collections.emptyList();
    }

    @Override
    public List<Holiday> getHolidaysByYear(Integer year) {
        return holidayMapper.selectByYear(year);
    }

    @Override
    public Long countByDateRange(LocalDate startDate, LocalDate endDate) {
        return holidayMapper.countByDateRange(startDate, endDate);
    }

    private HolidayVO convertToVO(Holiday holiday) {
        HolidayVO vo = new HolidayVO();
        BeanUtils.copyProperties(holiday, vo);
        dictLabelUtil.fillDictLabels(vo);
        vo.setIsWorkdayName(holiday.getIsWorkday() == 1 ? "是" : "否");
        return vo;
    }
}
