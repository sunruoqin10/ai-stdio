package com.example.oa_system_backend.module.leave.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.leave.dto.HolidayCreateRequest;
import com.example.oa_system_backend.module.leave.dto.HolidayQueryRequest;
import com.example.oa_system_backend.module.leave.entity.Holiday;
import com.example.oa_system_backend.module.leave.service.HolidayService;
import com.example.oa_system_backend.module.leave.vo.HolidayVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/leave/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping
    public ApiResponse<List<HolidayVO>> getHolidayList(HolidayQueryRequest request) {
        List<HolidayVO> result = holidayService.getHolidayList(request);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<HolidayVO> getHolidayById(@PathVariable Long id) {
        HolidayVO holiday = holidayService.getHolidayById(id);
        return ApiResponse.success(holiday);
    }

    @GetMapping("/date-range")
    public ApiResponse<List<HolidayVO>> getHolidaysByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<HolidayVO> holidays = holidayService.getHolidaysByDateRange(startDate, endDate);
        return ApiResponse.success(holidays);
    }

    @PostMapping
    public ApiResponse<Holiday> createHoliday(@Valid @RequestBody HolidayCreateRequest request) {
        Holiday holiday = holidayService.createHoliday(request);
        return ApiResponse.success("创建成功", holiday);
    }

    @PutMapping("/{id}")
    public ApiResponse<Holiday> updateHoliday(
            @PathVariable Long id,
            @Valid @RequestBody HolidayCreateRequest request) {
        Holiday holiday = holidayService.updateHoliday(id, request);
        return ApiResponse.success("更新成功", holiday);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteHoliday(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return ApiResponse.success("删除成功", null);
    }
}
