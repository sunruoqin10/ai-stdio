package com.example.oa_system_backend.module.employee.task;

import com.example.oa_system_backend.module.employee.entity.Employee;
import com.example.oa_system_backend.module.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 生日提醒任务
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BirthdayReminderTask {

    private final EmployeeService employeeService;

    /**
     * 每天早上9点检查生日提醒
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkBirthdayReminders() {
        log.info("开始检查生日提醒");

        // 查询今天生日的在职员工
        List<Employee> birthdayEmployees = employeeService.getTodayBirthdayEmployees();

        if (!birthdayEmployees.isEmpty()) {
            for (Employee employee : birthdayEmployees) {
                // 发送生日祝福通知
                sendBirthdayWish(employee);
            }

            log.info("今日有 {} 位员工过生日", birthdayEmployees.size());
        } else {
            log.info("今日没有员工过生日");
        }
    }

    /**
     * 发送生日祝福
     */
    private void sendBirthdayWish(Employee employee) {
        // TODO: 实现发送通知逻辑（邮件/系统消息等）
        log.info("发送生日祝福给: {} ({})", employee.getName(), employee.getEmail());
    }
}
