package com.example.oa_system_backend.module.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.employee.dto.*;
import com.example.oa_system_backend.module.employee.entity.Employee;
import com.example.oa_system_backend.module.employee.service.EmployeeService;
import com.example.oa_system_backend.module.employee.vo.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 员工管理控制器
 */
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 分页查询员工列表
     * GET /api/employees
     */
    @GetMapping
    public ApiResponse<IPage<EmployeeVO>> getEmployeeList(EmployeeQueryRequest request) {
        IPage<EmployeeVO> result = employeeService.getEmployeeList(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取员工详情
     * GET /api/employees/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<EmployeeDetailVO> getEmployeeById(@PathVariable String id) {
        EmployeeDetailVO employee = employeeService.getEmployeeById(id);
        return ApiResponse.success(employee);
    }

    /**
     * 创建员工
     * POST /api/employees
     */
    @PostMapping
    public ApiResponse<Employee> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest request) {
        Employee employee = employeeService.createEmployee(request);
        return ApiResponse.success("创建成功", employee);
    }

    /**
     * 更新员工信息
     * PUT /api/employees/{id}
     */
    @PutMapping("/{id}")
    public ApiResponse<Employee> updateEmployee(
            @PathVariable String id,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        Employee employee = employeeService.updateEmployee(id, request);
        return ApiResponse.success("更新成功", employee);
    }

    /**
     * 更新员工状态
     * PUT /api/employees/{id}/status
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Employee> updateEmployeeStatus(
            @PathVariable String id,
            @Valid @RequestBody EmployeeStatusUpdateRequest request) {
        Employee employee = employeeService.updateEmployeeStatus(id, request);
        return ApiResponse.success("状态更新成功", employee);
    }

    /**
     * 删除员工
     * DELETE /api/employees/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteEmployee(@PathVariable String id) {
        employeeService.deleteEmployee(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 获取统计数据
     * GET /api/employees/statistics
     */
    @GetMapping("/statistics")
    public ApiResponse<EmployeeStatisticsVO> getStatistics() {
        EmployeeStatisticsVO statistics = employeeService.getStatistics();
        return ApiResponse.success(statistics);
    }

    /**
     * 获取操作记录
     * GET /api/employees/{id}/logs
     */
    @GetMapping("/{id}/logs")
    public ApiResponse<IPage<OperationLogVO>> getOperationLogs(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<OperationLogVO> logs = employeeService.getOperationLogs(id, page, pageSize);
        return ApiResponse.success(logs);
    }

    /**
     * 检查邮箱是否存在
     * GET /api/employees/check-email
     */
    @GetMapping("/check-email")
    public ApiResponse<Boolean> checkEmailExists(
            @RequestParam String email,
            @RequestParam(required = false) String excludeId) {
        boolean exists = employeeService.checkEmailExists(email, excludeId);
        return ApiResponse.success(exists);
    }

    /**
     * 检查手机号是否存在
     * GET /api/employees/check-phone
     */
    @GetMapping("/check-phone")
    public ApiResponse<Boolean> checkPhoneExists(
            @RequestParam String phone,
            @RequestParam(required = false) String excludeId) {
        boolean exists = employeeService.checkPhoneExists(phone, excludeId);
        return ApiResponse.success(exists);
    }
}
