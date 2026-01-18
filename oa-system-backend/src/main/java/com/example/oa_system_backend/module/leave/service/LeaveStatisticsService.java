package com.example.oa_system_backend.module.leave.service;

import com.example.oa_system_backend.module.leave.dto.LeaveQueryRequest;
import com.example.oa_system_backend.module.leave.vo.LeaveStatisticsVO;

import java.time.LocalDate;

public interface LeaveStatisticsService {

    LeaveStatisticsVO getStatistics(LeaveQueryRequest query);

    LeaveStatisticsVO getEmployeeStatistics(String employeeId, LocalDate startDate, LocalDate endDate);

    LeaveStatisticsVO getDepartmentStatistics(String departmentId, LocalDate startDate, LocalDate endDate);

    LeaveStatisticsVO getGlobalStatistics(LocalDate startDate, LocalDate endDate);
}
