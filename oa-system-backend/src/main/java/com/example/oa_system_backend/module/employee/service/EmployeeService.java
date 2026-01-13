package com.example.oa_system_backend.module.employee.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.module.employee.dto.*;
import com.example.oa_system_backend.module.employee.entity.Employee;
import com.example.oa_system_backend.module.employee.vo.*;

import java.util.List;

/**
 * 员工服务接口
 */
public interface EmployeeService {

    /**
     * 分页查询员工列表
     */
    IPage<EmployeeVO> getEmployeeList(EmployeeQueryRequest request);

    /**
     * 根据ID获取员工详情
     */
    EmployeeDetailVO getEmployeeById(String id);

    /**
     * 创建员工
     */
    Employee createEmployee(EmployeeCreateRequest request);

    /**
     * 更新员工信息
     */
    Employee updateEmployee(String id, EmployeeUpdateRequest request);

    /**
     * 更新员工状态(办理离职等)
     */
    Employee updateEmployeeStatus(String id, EmployeeStatusUpdateRequest request);

    /**
     * 删除员工(逻辑删除)
     */
    void deleteEmployee(String id);

    /**
     * 获取员工统计数据
     */
    EmployeeStatisticsVO getStatistics();

    /**
     * 获取员工操作记录
     */
    IPage<OperationLogVO> getOperationLogs(String employeeId, Integer page, Integer pageSize);

    /**
     * 检查邮箱是否存在
     */
    boolean checkEmailExists(String email, String excludeId);

    /**
     * 检查手机号是否存在
     */
    boolean checkPhoneExists(String phone, String excludeId);

    /**
     * 生成员工编号
     */
    String generateEmployeeId(java.time.LocalDate joinDate);

    /**
     * 计算工龄
     */
    Integer calculateWorkYears(java.time.LocalDate joinDate);

    /**
     * 计算试用期结束日期
     */
    java.time.LocalDate calculateProbationEndDate(java.time.LocalDate joinDate);

    /**
     * 获取所有在职员工
     */
    List<Employee> getAllActiveEmployees();

    /**
     * 获取今天生日的员工
     */
    List<Employee> getTodayBirthdayEmployees();

    /**
     * 获取试用期即将到期的员工
     */
    List<Employee> getProbationExpiringEmployees(Integer days);

    /**
     * 更新员工工龄（供定时任务使用）
     */
    void updateWorkYears(String employeeId, Integer workYears);
}
