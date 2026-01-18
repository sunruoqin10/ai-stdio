package com.example.oa_system_backend.module.leave.service.impl;

import com.example.oa_system_backend.module.leave.dto.LeaveQueryRequest;
import com.example.oa_system_backend.module.leave.mapper.LeaveRequestMapper;
import com.example.oa_system_backend.module.leave.service.LeaveStatisticsService;
import com.example.oa_system_backend.module.leave.vo.LeaveStatisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveStatisticsServiceImpl implements LeaveStatisticsService {

    private final LeaveRequestMapper leaveRequestMapper;

    @Override
    public LeaveStatisticsVO getStatistics(LeaveQueryRequest query) {
        log.info("获取请假统计,查询条件: {}", query);

        LeaveStatisticsVO statistics = new LeaveStatisticsVO();

        Integer currentYear = LocalDate.now().getYear();

        List<Map<String, Object>> byTypeList = leaveRequestMapper.selectStatisticsByType(
                query.getApplicantId() != null ? query.getApplicantId() : "ALL",
                query.getYear() != null ? query.getYear() : currentYear
        );
        Map<String, Integer> byType = new HashMap<>();
        byTypeList.forEach(item -> {
            String type = (String) item.get("type");
            Long count = ((Number) item.get("count")).longValue();
            byType.put(type, count.intValue());
        });
        statistics.setByType(byType);

        List<Map<String, Object>> byStatusList = leaveRequestMapper.selectStatisticsByStatus(
                query.getApplicantId() != null ? query.getApplicantId() : "ALL",
                query.getYear() != null ? query.getYear() : currentYear
        );
        Map<String, Integer> byStatus = new HashMap<>();
        byStatusList.forEach(item -> {
            String status = (String) item.get("status");
            Long count = ((Number) item.get("count")).longValue();
            byStatus.put(status, count.intValue());
        });
        statistics.setByStatus(byStatus);

        List<Map<String, Object>> byMonthList = leaveRequestMapper.selectStatisticsByMonth(
                query.getApplicantId() != null ? query.getApplicantId() : "ALL",
                query.getYear() != null ? query.getYear() : currentYear
        );

        List<LeaveStatisticsVO.MonthlyData> monthlyData = byMonthList.stream()
                .map(item -> {
                    LeaveStatisticsVO.MonthlyData data = new LeaveStatisticsVO.MonthlyData();
                    data.setMonth((String) item.get("month"));
                    data.setCount(((Number) item.get("count")).intValue());
                    data.setDuration(new java.math.BigDecimal(item.get("duration").toString()));
                    return data;
                })
                .collect(java.util.stream.Collectors.toList());

        statistics.setMonthlyData(monthlyData);

        Integer totalRequests = byStatus.values().stream().mapToInt(Integer::intValue).sum();
        statistics.setTotalRequests(totalRequests);

        java.math.BigDecimal totalDuration = byMonthList.stream()
                .map(item -> new java.math.BigDecimal(item.get("duration").toString()))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        statistics.setTotalDuration(totalDuration);

        return statistics;
    }

    @Override
    public LeaveStatisticsVO getDepartmentStatistics(String departmentId, LocalDate startDate, LocalDate endDate) {
        LeaveQueryRequest query = new LeaveQueryRequest();
        query.setDepartmentId(departmentId);
        if (startDate != null && endDate != null) {
            query.setStartDate(startDate);
            query.setEndDate(endDate);
            query.setYear(startDate.getYear());
        }
        return getStatistics(query);
    }

    @Override
    public LeaveStatisticsVO getEmployeeStatistics(String employeeId, LocalDate startDate, LocalDate endDate) {
        LeaveQueryRequest query = new LeaveQueryRequest();
        query.setApplicantId(employeeId);
        if (startDate != null && endDate != null) {
            query.setStartDate(startDate);
            query.setEndDate(endDate);
            query.setYear(startDate.getYear());
        }
        return getStatistics(query);
    }

    @Override
    public LeaveStatisticsVO getGlobalStatistics(LocalDate startDate, LocalDate endDate) {
        LeaveQueryRequest query = new LeaveQueryRequest();
        if (startDate != null && endDate != null) {
            query.setStartDate(startDate);
            query.setEndDate(endDate);
            query.setYear(startDate.getYear());
        }
        return getStatistics(query);
    }
}
