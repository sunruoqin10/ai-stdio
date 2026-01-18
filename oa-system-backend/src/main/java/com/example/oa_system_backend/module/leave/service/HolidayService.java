package com.example.oa_system_backend.module.leave.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.oa_system_backend.module.leave.dto.HolidayCreateRequest;
import com.example.oa_system_backend.module.leave.dto.HolidayQueryRequest;
import com.example.oa_system_backend.module.leave.entity.Holiday;
import com.example.oa_system_backend.module.leave.vo.HolidayVO;

import java.time.LocalDate;
import java.util.List;

public interface HolidayService extends IService<Holiday> {

    List<HolidayVO> getHolidayList(HolidayQueryRequest query);

    HolidayVO getHolidayById(Long id);

    List<HolidayVO> getHolidaysByDateRange(LocalDate startDate, LocalDate endDate);

    Holiday createHoliday(HolidayCreateRequest request);

    Holiday updateHoliday(Long id, HolidayCreateRequest request);

    void deleteHoliday(Long id);

    Holiday getHolidayByDate(LocalDate date);

    List<Holiday> getHolidaysByDate(LocalDate date);

    List<Holiday> getHolidaysByYear(Integer year);

    Long countByDateRange(LocalDate startDate, LocalDate endDate);
}
