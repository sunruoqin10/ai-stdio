package com.example.oa_system_backend.module.leave.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oa_system_backend.common.exception.BusinessException;
import com.example.oa_system_backend.module.department.entity.Department;
import com.example.oa_system_backend.module.department.mapper.DepartmentMapper;
import com.example.oa_system_backend.module.employee.entity.Employee;
import com.example.oa_system_backend.module.employee.mapper.EmployeeMapper;
import com.example.oa_system_backend.module.leave.dto.*;
import com.example.oa_system_backend.module.leave.entity.LeaveBalance;
import com.example.oa_system_backend.module.leave.entity.LeaveUsageLog;
import com.example.oa_system_backend.module.leave.enums.ChangeType;
import com.example.oa_system_backend.module.leave.enums.LeaveType;
import com.example.oa_system_backend.module.leave.mapper.LeaveBalanceMapper;
import com.example.oa_system_backend.module.leave.mapper.LeaveUsageLogMapper;
import com.example.oa_system_backend.module.leave.service.LeaveBalanceService;
import com.example.oa_system_backend.module.leave.util.AnnualQuotaCalculator;
import com.example.oa_system_backend.module.leave.vo.LeaveBalanceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveBalanceServiceImpl extends ServiceImpl<LeaveBalanceMapper, LeaveBalance> implements LeaveBalanceService {

    private final LeaveBalanceMapper leaveBalanceMapper;
    private final LeaveUsageLogMapper leaveUsageLogMapper;
    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final AnnualQuotaCalculator quotaCalculator;
    private final com.example.oa_system_backend.module.leave.util.LeaveDictLabelUtil dictLabelUtil;

    @Override
    public LeaveBalanceVO getBalance(String employeeId, Integer year) {
        log.info("查询年假余额,员工ID: {}, 年份: {}", employeeId, year);

        if (year == null) {
            year = LocalDateTime.now().getYear();
        }

        LeaveBalance balance = leaveBalanceMapper.selectByEmployeeIdAndYear(employeeId, year);

        if (balance == null) {
            initBalance(employeeId, year);
            balance = leaveBalanceMapper.selectByEmployeeIdAndYear(employeeId, year);
        }

        return convertToVO(balance);
    }

    @Override
    public LeaveBalanceVO getBalanceByEmployeeId(String employeeId) {
        log.info("查询员工年假余额,员工ID: {}", employeeId);
        return getBalance(employeeId, null);
    }

    @Override
    public IPage<LeaveBalanceVO> getBalanceList(BalanceQueryRequest query) {
        log.info("查询年假余额列表,查询条件: {}", query);

        Page<LeaveBalance> page = new Page<>(query.getPage(), query.getPageSize());
        List<LeaveBalance> list = leaveBalanceMapper.selectByCondition(
                query.getEmployeeId(),
                query.getYear()
        );

        IPage<LeaveBalanceVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getCurrent());
        result.setRecords(list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveBalance updateBalance(BalanceUpdateRequest request) {
        log.info("更新年假额度,更新信息: {}", request);

        LeaveBalance balance = leaveBalanceMapper.selectByEmployeeIdAndYear(
                request.getEmployeeId(),
                request.getYear()
        );

        if (balance == null) {
            throw new BusinessException(3007, "年假记录不存在");
        }

        BigDecimal used = balance.getAnnualUsed();
        BigDecimal total = request.getAnnualTotal();

        if (used.compareTo(total) > 0) {
            throw new BusinessException(3008, "年假总额不能小于已使用天数");
        }

        balance.setAnnualTotal(total);
        balance.setAnnualRemaining(total.subtract(used));
        balance.setUpdatedAt(LocalDateTime.now());
        leaveBalanceMapper.updateById(balance);

        log.info("年假额度更新成功");
        return balance;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductBalance(String employeeId, Integer year, BigDecimal duration, String requestId) {
        log.info("扣减年假余额,员工ID: {}, 年份: {}, 时长: {}, 申请ID: {}", employeeId, year, duration, requestId);

        LeaveBalance balance = leaveBalanceMapper.selectByEmployeeIdAndYear(employeeId, year);

        if (balance == null) {
            throw new BusinessException(3007, "年假记录不存在");
        }

        if (balance.getAnnualRemaining().compareTo(duration) < 0) {
            throw new BusinessException(3002, "年假余额不足");
        }

        balance.setAnnualUsed(balance.getAnnualUsed().add(duration));
        balance.setAnnualRemaining(balance.getAnnualRemaining().subtract(duration));
        balance.setUpdatedAt(LocalDateTime.now());
        leaveBalanceMapper.updateById(balance);

        LeaveUsageLog usageLog = new LeaveUsageLog();
        usageLog.setEmployeeId(employeeId);
        usageLog.setRequestId(requestId);
        usageLog.setType(LeaveType.ANNUAL.getCode());
        usageLog.setDuration(duration);
        usageLog.setChangeType(ChangeType.DEDUCT.getCode());
        usageLog.setCreatedAt(LocalDateTime.now());
        leaveUsageLogMapper.insert(usageLog);

        log.info("年假余额扣减成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackBalance(String employeeId, Integer year, BigDecimal duration, String requestId) {
        log.info("回退年假余额,员工ID: {}, 年份: {}, 时长: {}, 申请ID: {}", employeeId, year, duration, requestId);

        LeaveBalance balance = leaveBalanceMapper.selectByEmployeeIdAndYear(employeeId, year);

        if (balance == null) {
            throw new BusinessException(3007, "年假记录不存在");
        }

        balance.setAnnualUsed(balance.getAnnualUsed().subtract(duration));
        balance.setAnnualRemaining(balance.getAnnualRemaining().add(duration));
        balance.setUpdatedAt(LocalDateTime.now());
        leaveBalanceMapper.updateById(balance);

        LeaveUsageLog usageLog = new LeaveUsageLog();
        usageLog.setEmployeeId(employeeId);
        usageLog.setRequestId(requestId);
        usageLog.setType(LeaveType.ANNUAL.getCode());
        usageLog.setDuration(duration);
        usageLog.setChangeType(ChangeType.ROLLBACK.getCode());
        usageLog.setCreatedAt(LocalDateTime.now());
        leaveUsageLogMapper.insert(usageLog);

        log.info("年假余额回退成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveBalance initBalance(String employeeId, Integer year) {
        log.info("初始化年假余额,员工ID: {}, 年份: {}", employeeId, year);

        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new BusinessException(3009, "员工不存在");
        }

        BigDecimal quota = quotaCalculator.calculateAnnualQuota(employee.getJoinDate(), java.time.LocalDate.of(year, 1, 1));

        LeaveBalance balance = leaveBalanceMapper.selectByEmployeeIdAndYear(employeeId, year);
        
        if (balance == null) {
            balance = new LeaveBalance();
            balance.setEmployeeId(employeeId);
            balance.setYear(year);
            balance.setAnnualTotal(quota);
            balance.setAnnualUsed(BigDecimal.ZERO);
            balance.setAnnualRemaining(quota);
            balance.setCreatedAt(LocalDateTime.now());
            balance.setUpdatedAt(LocalDateTime.now());
            leaveBalanceMapper.insert(balance);
            log.info("年假余额初始化成功");
        } else {
            log.info("年假余额已存在,员工ID: {}, 年份: {}", employeeId, year);
        }

        return balance;
    }

    @Override
    public LeaveBalance getBalanceByEmployeeIdAndYear(String employeeId, Integer year) {
        return leaveBalanceMapper.selectByEmployeeIdAndYear(employeeId, year);
    }

    private LeaveBalanceVO convertToVO(LeaveBalance balance) {
        LeaveBalanceVO vo = new LeaveBalanceVO();
        BeanUtils.copyProperties(balance, vo);

        Employee employee = employeeMapper.selectById(balance.getEmployeeId());
        if (employee != null) {
            vo.setEmployeeName(employee.getName());
            vo.setEmployeeAvatar(employee.getAvatar());
            if (employee.getDepartmentId() != null) {
                Department department = departmentMapper.selectById(employee.getDepartmentId());
                if (department != null) {
                    vo.setDepartmentId(department.getId());
                    vo.setDepartmentName(department.getName());
                }
            }
        }

        BigDecimal usagePercentage = BigDecimal.ZERO;
        if (balance.getAnnualTotal().compareTo(BigDecimal.ZERO) > 0) {
            usagePercentage = balance.getAnnualUsed()
                    .divide(balance.getAnnualTotal(), 2, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        vo.setUsagePercentage(usagePercentage);

        BigDecimal remaining = balance.getAnnualRemaining();
        if (remaining.compareTo(new BigDecimal("3")) >= 0) {
            vo.setWarningLevel("none");
        } else if (remaining.compareTo(new BigDecimal("1")) >= 0) {
            vo.setWarningLevel("warning");
        } else {
            vo.setWarningLevel("critical");
        }

        dictLabelUtil.fillDictLabels(vo);

        return vo;
    }
}
