package com.example.oa_system_backend.module.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.common.exception.BusinessException;
import com.example.oa_system_backend.module.employee.dto.*;
import com.example.oa_system_backend.module.employee.entity.Employee;
import com.example.oa_system_backend.module.employee.entity.EmployeeOperationLog;
import com.example.oa_system_backend.module.employee.mapper.EmployeeMapper;
import com.example.oa_system_backend.module.employee.mapper.EmployeeOperationLogMapper;
import com.example.oa_system_backend.module.employee.service.EmployeeService;
import com.example.oa_system_backend.module.employee.util.DictLabelUtil;
import com.example.oa_system_backend.module.employee.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 员工服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final EmployeeOperationLogMapper operationLogMapper;
    private final DictLabelUtil dictLabelUtil;

    @Override
    public IPage<EmployeeVO> getEmployeeList(EmployeeQueryRequest request) {
        Page<EmployeeVO> page = new Page<>(request.getPage(), request.getPageSize());

        IPage<EmployeeVO> result = employeeMapper.selectPageWithDetails(
                page,
                request.getKeyword(),
                request.getStatus(),
                request.getDepartmentIds(),
                request.getPosition(),
                request.getGender(),
                request.getJoinDateStart(),
                request.getJoinDateEnd()
        );

        // 填充字典标签
        dictLabelUtil.fillDictLabelsForList(result.getRecords());

        return result;
    }

    @Override
    public EmployeeDetailVO getEmployeeById(String id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        EmployeeDetailVO vo = new EmployeeDetailVO();
        BeanUtils.copyProperties(employee, vo);

        // 设置员工编号（别名）
        vo.setEmployeeNo(employee.getId());

        // 获取部门名称和上级姓名
        if (employee.getDepartmentId() != null) {
            QueryWrapper<Employee> wrapper = new QueryWrapper<>();
            wrapper.eq("id", employee.getId());
            wrapper.last("LIMIT 1");
            Employee employeeWithDetails = employeeMapper.selectOne(wrapper);
            if (employeeWithDetails != null) {
                vo.setDepartmentName(employeeWithDetails.getDepartmentName());
                vo.setManagerName(employeeWithDetails.getManagerName());
            }
        }

        // 填充字典标签
        dictLabelUtil.fillDictLabels(vo);

        return vo;
    }

    @Override
    @Transactional
    public Employee createEmployee(EmployeeCreateRequest request) {
        // 1. 验证邮箱唯一性
        if (employeeMapper.countByEmail(request.getEmail(), null) > 0) {
            throw new BusinessException("该邮箱已被使用");
        }

        // 2. 验证手机号唯一性
        if (employeeMapper.countByPhone(request.getPhone(), null) > 0) {
            throw new BusinessException("该手机号已被使用");
        }

        // 3. 验证部门存在性
        if (employeeMapper.countByDepartmentId(request.getDepartmentId()) == 0) {
            throw new BusinessException("指定的部门不存在");
        }

        // 4. 验证直属上级（如果不是第一个员工，则必须指定直属上级）
        long employeeCount = employeeMapper.selectCount(null);
        if (employeeCount > 0) {
            // 不是第一个员工，必须指定直属上级
            if (request.getManagerId() == null || request.getManagerId().trim().isEmpty()) {
                throw new BusinessException("请指定直属上级");
            }
            // 验证指定的直属上级是否存在
            if (employeeMapper.countByManagerId(request.getManagerId()) == 0) {
                throw new BusinessException("指定的直属上级不存在或已离职");
            }
        } else if (request.getManagerId() != null && !request.getManagerId().trim().isEmpty()) {
            // 是第一个员工，但如果指定了上级，也要验证其存在性
            if (employeeMapper.countByManagerId(request.getManagerId()) == 0) {
                throw new BusinessException("指定的直属上级不存在或已离职");
            }
        }

        // 5. 生成员工编号
        String employeeId = generateEmployeeId(request.getJoinDate());

        // 6. 计算工龄
        Integer workYears = calculateWorkYears(request.getJoinDate());

        // 7. 计算试用期结束日期(如果未指定)
        LocalDate probationEndDate = request.getProbationEndDate();
        if (probationEndDate == null) {
            probationEndDate = calculateProbationEndDate(request.getJoinDate());
        }

        // 8. 构建Employee实体
        Employee employee = new Employee();
        BeanUtils.copyProperties(request, employee);
        employee.setId(employeeId);
        employee.setWorkYears(workYears);
        employee.setProbationEndDate(probationEndDate);
        employee.setStatus("active");
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());

        // 9. 保存到数据库
        employeeMapper.insert(employee);

        // 10. 记录操作日志
        saveOperationLog(employeeId, "CREATE", "创建员工: " + employee.getName());

        return employee;
    }

    @Override
    @Transactional
    public Employee updateEmployee(String id, EmployeeUpdateRequest request) {
        // 1. 检查员工是否存在
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 2. 验证邮箱唯一性(如果更新了邮箱)
        if (request.getEmail() != null && !request.getEmail().equals(employee.getEmail())) {
            if (employeeMapper.countByEmail(request.getEmail(), id) > 0) {
                throw new BusinessException("该邮箱已被使用");
            }
        }

        // 3. 验证手机号唯一性(如果更新了手机号)
        if (request.getPhone() != null && !request.getPhone().equals(employee.getPhone())) {
            if (employeeMapper.countByPhone(request.getPhone(), id) > 0) {
                throw new BusinessException("该手机号已被使用");
            }
        }

        // 4. 验证部门存在性(如果更新了部门)
        if (request.getDepartmentId() != null && !request.getDepartmentId().equals(employee.getDepartmentId())) {
            if (employeeMapper.countByDepartmentId(request.getDepartmentId()) == 0) {
                throw new BusinessException("指定的部门不存在");
            }
        }

        // 5. 验证上级存在性(如果更新了上级)
        if (request.getManagerId() != null && !request.getManagerId().equals(employee.getManagerId())) {
            if (employeeMapper.countByManagerId(request.getManagerId()) == 0) {
                throw new BusinessException("指定的直属上级不存在或已离职");
            }
        }

        // 6. 更新字段
        if (request.getName() != null) {
            employee.setName(request.getName());
        }
        if (request.getEnglishName() != null) {
            employee.setEnglishName(request.getEnglishName());
        }
        if (request.getGender() != null) {
            employee.setGender(request.getGender());
        }
        if (request.getBirthDate() != null) {
            employee.setBirthDate(request.getBirthDate());
        }
        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            employee.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) {
            employee.setAvatar(request.getAvatar());
        }
        if (request.getDepartmentId() != null) {
            employee.setDepartmentId(request.getDepartmentId());
        }
        if (request.getPosition() != null) {
            employee.setPosition(request.getPosition());
        }
        if (request.getLevel() != null) {
            employee.setLevel(request.getLevel());
        }
        if (request.getManagerId() != null) {
            employee.setManagerId(request.getManagerId());
        }
        if (request.getJoinDate() != null) {
            employee.setJoinDate(request.getJoinDate());
            employee.setWorkYears(calculateWorkYears(request.getJoinDate()));
        }
        if (request.getProbationEndDate() != null) {
            employee.setProbationEndDate(request.getProbationEndDate());
        }
        if (request.getOfficeLocation() != null) {
            employee.setOfficeLocation(request.getOfficeLocation());
        }
        if (request.getEmergencyContact() != null) {
            employee.setEmergencyContact(request.getEmergencyContact());
        }
        if (request.getEmergencyPhone() != null) {
            employee.setEmergencyPhone(request.getEmergencyPhone());
        }

        employee.setUpdatedAt(LocalDateTime.now());

        // 7. 保存更新
        employeeMapper.updateById(employee);

        // 8. 记录操作日志
        saveOperationLog(id, "UPDATE", "更新员工信息: " + employee.getName());

        return employee;
    }

    @Override
    @Transactional
    public Employee updateEmployeeStatus(String id, EmployeeStatusUpdateRequest request) {
        // 1. 检查员工是否存在
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 2. 更新状态
        employee.setStatus(request.getStatus());
        employee.setUpdatedAt(LocalDateTime.now());

        // 3. 保存更新
        employeeMapper.updateById(employee);

        // 4. 记录操作日志
        String details = "更新员工状态为: " + request.getStatus();
        if (request.getReason() != null) {
            details += ", 原因: " + request.getReason();
        }
        saveOperationLog(id, "STATUS_UPDATE", details);

        return employee;
    }

    @Override
    @Transactional
    public void deleteEmployee(String id) {
        // 1. 检查员工是否存在
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 2. 逻辑删除
        employeeMapper.deleteById(id);

        // 3. 记录操作日志
        saveOperationLog(id, "DELETE", "删除员工: " + employee.getName());
    }

    @Override
    public EmployeeStatisticsVO getStatistics() {
        EmployeeStatisticsVO statistics = new EmployeeStatisticsVO();

        // 统计总数
        QueryWrapper<Employee> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("is_deleted", 0);
        statistics.setTotal(employeeMapper.selectCount(totalWrapper).intValue());

        // 统计在职人数
        QueryWrapper<Employee> activeWrapper = new QueryWrapper<>();
        activeWrapper.eq("status", "active");
        activeWrapper.eq("is_deleted", 0);
        statistics.setActive(employeeMapper.selectCount(activeWrapper).intValue());

        // 统计离职人数
        QueryWrapper<Employee> resignedWrapper = new QueryWrapper<>();
        resignedWrapper.eq("status", "resigned");
        resignedWrapper.eq("is_deleted", 0);
        statistics.setResigned(employeeMapper.selectCount(resignedWrapper).intValue());

        // 统计停薪留职人数
        QueryWrapper<Employee> suspendedWrapper = new QueryWrapper<>();
        suspendedWrapper.eq("status", "suspended");
        suspendedWrapper.eq("is_deleted", 0);
        statistics.setSuspended(employeeMapper.selectCount(suspendedWrapper).intValue());

        // 统计试用期人数（动态计算：试用期结束日期在今天之后，且状态为在职）
        LocalDate today = LocalDate.now();
        QueryWrapper<Employee> probationWrapper = new QueryWrapper<>();
        probationWrapper.ge("probation_end_date", today);
        probationWrapper.eq("status", "active");
        probationWrapper.eq("is_deleted", 0);
        statistics.setProbation(employeeMapper.selectCount(probationWrapper).intValue());

        // 统计本月新入职
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        QueryWrapper<Employee> newThisMonthWrapper = new QueryWrapper<>();
        newThisMonthWrapper.ge("join_date", startOfMonth);
        newThisMonthWrapper.le("join_date", now);
        newThisMonthWrapper.eq("is_deleted", 0);
        statistics.setNewThisMonth(employeeMapper.selectCount(newThisMonthWrapper).intValue());

        return statistics;
    }

    @Override
    public IPage<OperationLogVO> getOperationLogs(String employeeId, Integer page, Integer pageSize) {
        Page<EmployeeOperationLog> pageInfo = new Page<>(page, pageSize);

        QueryWrapper<EmployeeOperationLog> wrapper = new QueryWrapper<>();
        wrapper.eq("employee_id", employeeId);
        // 按ID降序排序（ID通常是自增的，可以作为时间排序的替代）
        wrapper.orderByDesc("id");

        IPage<EmployeeOperationLog> logPage = operationLogMapper.selectPage(pageInfo, wrapper);

        // 转换为VO
        Page<OperationLogVO> voPage = new Page<>(page, pageSize);
        voPage.setTotal(logPage.getTotal());
        voPage.setRecords(logPage.getRecords().stream()
                .map(this::convertToOperationLogVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public boolean checkEmailExists(String email, String excludeId) {
        return employeeMapper.countByEmail(email, excludeId) > 0;
    }

    @Override
    public boolean checkPhoneExists(String phone, String excludeId) {
        return employeeMapper.countByPhone(phone, excludeId) > 0;
    }

    @Override
    @Transactional
    public String generateEmployeeId(LocalDate joinDate) {
        // 格式化日期为 YYYYMMDD
        String dateStr = joinDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 查询当天入职人数
        Integer count = employeeMapper.countByJoinDate(dateStr);

        // 生成3位序号
        String sequence = String.format("%03d", count + 1);

        // 返回员工编号: EMP + YYYYMMDD + 序号
        return "EMP" + dateStr + sequence;
    }

    @Override
    public Integer calculateWorkYears(LocalDate joinDate) {
        LocalDate now = LocalDate.now();

        // 计算年份差
        int years = now.getYear() - joinDate.getYear();

        // 如果还没到入职月份,减1年
        if (now.getMonthValue() < joinDate.getMonthValue()) {
            years--;
        }
        // 如果是入职月份但还没到入职日,减1年
        else if (now.getMonthValue() == joinDate.getMonthValue() &&
                now.getDayOfMonth() < joinDate.getDayOfMonth()) {
            years--;
        }

        return Math.max(0, years);
    }

    @Override
    public LocalDate calculateProbationEndDate(LocalDate joinDate) {
        // 默认试用期3个月
        return joinDate.plusMonths(3);
    }

    @Override
    public List<Employee> getAllActiveEmployees() {
        return employeeMapper.selectAllActive();
    }

    @Override
    public List<Employee> getTodayBirthdayEmployees() {
        return employeeMapper.selectTodayBirthday();
    }

    @Override
    public List<Employee> getProbationExpiringEmployees(Integer days) {
        return employeeMapper.selectProbationExpiring(days);
    }

    @Override
    @Transactional
    public void updateWorkYears(String employeeId, Integer workYears) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee != null) {
            employee.setWorkYears(workYears);
            employee.setUpdatedAt(LocalDateTime.now());
            employeeMapper.updateById(employee);
        }
    }

    /**
     * 记录操作日志
     */
    private void saveOperationLog(String employeeId, String operation, String details) {
        EmployeeOperationLog log = new EmployeeOperationLog();
        log.setEmployeeId(employeeId);
        log.setOperation(operation);
        log.setOperator("system"); // 设置操作人
        log.setOperatorId("system"); // TODO: 从SecurityContext获取当前用户ID
        log.setOperatorName("系统管理员"); // TODO: 从SecurityContext获取当前用户名
        log.setOperationTime(LocalDateTime.now());
        log.setDetails(details);
        log.setCreatedAt(LocalDateTime.now());

        operationLogMapper.insert(log);
    }

    /**
     * 转换为操作日志VO
     */
    private OperationLogVO convertToOperationLogVO(EmployeeOperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(log.getId());
        vo.setEmployeeId(log.getEmployeeId());
        vo.setOperation(log.getOperation());
        vo.setOperatorId(log.getOperatorId());
        // 使用operator字段作为operatorName（因为数据库中存储的是operator字段）
        vo.setOperatorName(log.getOperator() != null ? log.getOperator() : "系统");
        // operationTime不在数据库中，用当前时间代替或可以为null
        vo.setOperationTime(log.getOperationTime());
        vo.setDetails(log.getDetails());
        vo.setIpAddress(log.getIpAddress());
        return vo;
    }
}
