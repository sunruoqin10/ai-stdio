package com.example.oa_system_backend.module.leave.controller;

import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.leave.service.LeaveStatisticsService;
import com.example.oa_system_backend.module.leave.vo.LeaveStatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/leave/statistics")
@RequiredArgsConstructor
public class LeaveStatisticsController {

    private final LeaveStatisticsService leaveStatisticsService;

    @GetMapping("/employee/{employeeId}")
    public ApiResponse<LeaveStatisticsVO> getEmployeeStatistics(
            @PathVariable String employeeId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        LeaveStatisticsVO statistics = leaveStatisticsService.getEmployeeStatistics(employeeId, startDate, endDate);
        return ApiResponse.success(statistics);
    }

    @GetMapping("/department/{departmentId}")
    public ApiResponse<LeaveStatisticsVO> getDepartmentStatistics(
            @PathVariable String departmentId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        LeaveStatisticsVO statistics = leaveStatisticsService.getDepartmentStatistics(departmentId, startDate, endDate);
        return ApiResponse.success(statistics);
    }

    @GetMapping("/global")
    public ApiResponse<LeaveStatisticsVO> getGlobalStatistics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        LeaveStatisticsVO statistics = leaveStatisticsService.getGlobalStatistics(startDate, endDate);
        return ApiResponse.success(statistics);
    }
}
