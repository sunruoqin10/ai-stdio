package com.example.oa_system_backend.module.employee.task;

import com.example.oa_system_backend.module.employee.entity.Employee;
import com.example.oa_system_backend.module.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 工龄自动更新任务
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WorkYearUpdateTask {

    private final EmployeeService employeeService;

    /**
     * 每月1号凌晨1点更新工龄
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void updateAllWorkYears() {
        log.info("开始更新所有员工工龄");

        // 查询所有在职员工
        List<Employee> employees = employeeService.getAllActiveEmployees();

        int updateCount = 0;
        for (Employee employee : employees) {
            try {
                Integer newWorkYears = employeeService.calculateWorkYears(employee.getJoinDate());

                if (!newWorkYears.equals(employee.getWorkYears())) {
                    employeeService.updateWorkYears(employee.getId(), newWorkYears);
                    updateCount++;
                    log.info("更新员工 {} {} 的工龄从 {} 年更新为 {} 年",
                            employee.getId(), employee.getName(),
                            employee.getWorkYears(), newWorkYears);
                }
            } catch (Exception e) {
                log.error("更新员工 {} 工龄失败: {}", employee.getId(), e.getMessage());
            }
        }

        log.info("工龄更新完成，共更新 {} 条记录", updateCount);
    }
}
