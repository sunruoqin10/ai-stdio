package com.example.oa_system_backend.module.meeting.controller;

import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.meeting.service.MeetingStatisticsService;
import com.example.oa_system_backend.module.meeting.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meeting/stats")
@RequiredArgsConstructor
public class MeetingStatisticsController {

    private final MeetingStatisticsService statisticsService;

    @GetMapping("/room-usage")
    public ApiResponse<List<RoomUsageStatsVO>> getRoomUsageStats(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<RoomUsageStatsVO> stats = statisticsService.getRoomUsageStats(startDate, endDate);
        return ApiResponse.success(stats);
    }

    @GetMapping("/department-usage")
    public ApiResponse<List<DepartmentUsageStatsVO>> getDepartmentUsageStats(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<DepartmentUsageStatsVO> stats = statisticsService.getDepartmentUsageStats(startDate, endDate);
        return ApiResponse.success(stats);
    }

    @GetMapping("/time-slot")
    public ApiResponse<List<TimeSlotStatsVO>> getTimeSlotStats(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<TimeSlotStatsVO> stats = statisticsService.getTimeSlotStats(startDate, endDate);
        return ApiResponse.success(stats);
    }

    @GetMapping("/monthly")
    public ApiResponse<List<MonthlyStatsVO>> getMonthlyStats(@RequestParam Integer year) {
        List<MonthlyStatsVO> stats = statisticsService.getMonthlyStats(year);
        return ApiResponse.success(stats);
    }
}
