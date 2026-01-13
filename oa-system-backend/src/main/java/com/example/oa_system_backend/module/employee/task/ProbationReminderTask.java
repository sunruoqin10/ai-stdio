package com.example.oa_system_backend.module.employee.task;

import com.example.oa_system_backend.module.employee.entity.Employee;
import com.example.oa_system_backend.module.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 转正提醒任务
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ProbationReminderTask {

    private final EmployeeService employeeService;

    /**
     * 每天早上9点检查转正提醒
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkProbationReminders() {
        log.info("开始检查转正提醒");

        // 查询7天内试用期到期的员工
        List<Employee> expiringEmployees = employeeService.getProbationExpiringEmployees(7);

        if (!expiringEmployees.isEmpty()) {
            for (Employee employee : expiringEmployees) {
                // 发送转正提醒
                sendProbationReminder(employee);
            }

            log.info("有 {} 位员工试用期即将到期", expiringEmployees.size());
        } else {
            log.info("没有即将到期试用期员工");
        }
    }

    /**
     * 发送转正提醒
     */
    private void sendProbationReminder(Employee employee) {
        // TODO: 实现发送提醒逻辑（邮件/系统消息等）
        log.info("发送转正提醒给: {} ({})，试用期结束日期: {}",
                employee.getName(), employee.getEmail(), employee.getProbationEndDate());
    }
}
