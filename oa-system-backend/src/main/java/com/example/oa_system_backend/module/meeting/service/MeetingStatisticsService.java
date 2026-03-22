package com.example.oa_system_backend.module.meeting.service;

import com.example.oa_system_backend.module.meeting.vo.*;

import java.util.List;

public interface MeetingStatisticsService {

    List<RoomUsageStatsVO> getRoomUsageStats(String startDate, String endDate);

    List<DepartmentUsageStatsVO> getDepartmentUsageStats(String startDate, String endDate);

    List<TimeSlotStatsVO> getTimeSlotStats(String startDate, String endDate);

    List<MonthlyStatsVO> getMonthlyStats(Integer year);
}
