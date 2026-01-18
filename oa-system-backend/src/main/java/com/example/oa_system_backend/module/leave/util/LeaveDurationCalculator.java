package com.example.oa_system_backend.module.leave.util;

import com.example.oa_system_backend.module.leave.entity.Holiday;
import com.example.oa_system_backend.module.leave.enums.HolidayType;
import com.example.oa_system_backend.module.leave.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveDurationCalculator {

    private final HolidayService holidayService;

    public BigDecimal calculateDuration(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        BigDecimal totalDays = BigDecimal.ZERO;

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            if (isWorkday(currentDate)) {
                totalDays = totalDays.add(BigDecimal.ONE);
            }
            currentDate = currentDate.plusDays(1);
        }

        return totalDays;
    }

    public BigDecimal calculateDurationByHours(LocalDate startDate, LocalDate endDate, BigDecimal totalHours) {
        BigDecimal workdays = calculateDuration(startDate, endDate);
        if (workdays.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalHours.divide(workdays, 2, RoundingMode.HALF_UP);
    }

    private boolean isWorkday(LocalDate date) {
        if (isWeekend(date)) {
            return isWorkdayOnWeekend(date);
        }
        return !isHoliday(date);
    }

    private boolean isWeekend(LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        return dayOfWeek == 6 || dayOfWeek == 7;
    }

    private boolean isHoliday(LocalDate date) {
        List<Holiday> holidays = holidayService.getHolidaysByDate(date);
        return holidays.stream()
                .anyMatch(h -> HolidayType.NATIONAL.getCode().equals(h.getType()) ||
                               HolidayType.COMPANY.getCode().equals(h.getType()));
    }

    private boolean isWorkdayOnWeekend(LocalDate date) {
        List<Holiday> holidays = holidayService.getHolidaysByDate(date);
        return holidays.stream()
                .anyMatch(h -> h.getIsWorkday() != null && h.getIsWorkday() == 1);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
    }

    public long calculateCalendarDays(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
