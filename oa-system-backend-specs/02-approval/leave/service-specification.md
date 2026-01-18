# 请假管理模块 - Service层规范

> **模块**: leave
> **版本**: v1.0.0
> **更新日期**: 2026-01-18

---

## 🎯 Service层架构

### 服务分层

```
LeaveRequestService (请假申请服务)
    ├── LeaveRequestQueryService (查询服务)
    ├── LeaveRequestValidateService (验证服务)
    └── LeaveRequestWorkflowService (工作流服务)

LeaveApprovalService (审批服务)

LeaveBalanceService (年假余额服务)
    ├── LeaveBalanceQueryService (查询服务)
    └── LeaveBalanceUpdateService (更新服务)

HolidayService (节假日服务)

LeaveStatisticsService (统计服务)
```

---

## 📦 核心Service类

### 1. LeaveRequestService (请假申请服务)

**文件路径**: `com/oa/system/module/leave/service/LeaveRequestService.java`

```java
package com.oa.system.module.leave.service;

import com.oa.system.common.dto.PageResult;
import com.oa.system.module.leave.dto.request.LeaveCreateDTO;
import com.oa.system.module.leave.dto.request.LeaveQueryDTO;
import com.oa.system.module.leave.dto.request.LeaveUpdateDTO;
import com.oa.system.module.leave.vo.LeaveDetailVO;
import com.oa.system.module.leave.vo.LeaveRequestVO;

/**
 * 请假申请服务接口
 *
 * @author OA System
 * @since 2026-01-18
 */
public interface LeaveRequestService {

    PageResult<LeaveRequestVO> getLeaveRequests(LeaveQueryDTO queryDTO);

    LeaveDetailVO getLeaveDetail(String id);

    String createLeaveRequest(LeaveCreateDTO createDTO);

    void updateLeaveRequest(String id, LeaveUpdateDTO updateDTO);

    void deleteLeaveRequest(String id);

    void submitLeaveRequest(String id);

    void cancelLeaveRequest(String id);

    void resubmitLeaveRequest(String id);
}
```

---

### 2. LeaveRequestServiceImpl (实现类)

**文件路径**: `com/oa/system/module/leave/service/impl/LeaveRequestServiceImpl.java`

```java
package com.oa.system.module.leave.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.system.common.dto.PageResult;
import com.oa.system.common.exception.BusinessException;
import com.oa.system.module.department.service.DepartmentService;
import com.oa.system.module.employee.service.EmployeeService;
import com.oa.system.module.leave.converter.LeaveVOConverter;
import com.oa.system.module.leave.dto.request.LeaveCreateDTO;
import com.oa.system.module.leave.dto.request.LeaveQueryDTO;
import com.oa.system.module.leave.dto.request.LeaveUpdateDTO;
import com.oa.system.module.leave.entity.LeaveApproval;
import com.oa.system.module.leave.entity.LeaveRequest;
import com.oa.system.module.leave.enums.LeaveStatus;
import com.oa.system.module.leave.enums.LeaveType;
import com.oa.system.module.leave.exception.LeaveBalanceInsufficientException;
import com.oa.system.module.leave.exception.LeaveTimeConflictException;
import com.oa.system.module.leave.mapper.LeaveRequestMapper;
import com.oa.system.module.leave.service.*;
import com.oa.system.module.leave.util.LeaveDurationCalculator;
import com.oa.system.module.leave.util.LeaveIdGenerator;
import com.oa.system.module.leave.vo.LeaveDetailVO;
import com.oa.system.module.leave.vo.LeaveRequestVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl
        extends ServiceImpl<LeaveRequestMapper, LeaveRequest>
        implements LeaveRequestService {

    private final LeaveRequestMapper leaveRequestMapper;
    private final LeaveApprovalService leaveApprovalService;
    private final LeaveBalanceService leaveBalanceService;
    private final LeaveRequestQueryService queryService;
    private final LeaveRequestValidateService validateService;
    private final LeaveRequestWorkflowService workflowService;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final LeaveVOConverter voConverter;
    private final LeaveDurationCalculator durationCalculator;
    private final LeaveIdGenerator idGenerator;

    @Override
    @Cacheable(value = "leave:requests", key = "#queryDTO.toString()")
    public PageResult<LeaveRequestVO> getLeaveRequests(LeaveQueryDTO queryDTO) {
        log.info("查询请假申请列表,查询条件: {}", queryDTO);

        return queryService.queryLeaveRequests(queryDTO);
    }

    @Override
    @Cacheable(value = "leave:detail", key = "#id")
    public LeaveDetailVO getLeaveDetail(String id) {
        log.info("获取请假申请详情,申请ID: {}", id);

        LeaveRequest request = leaveRequestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(3001, "请假申请不存在");
        }

        LeaveDetailVO detailVO = voConverter.toDetailVO(request);

        List<LeaveApproval> approvals = leaveApprovalService.getApprovalsByRequestId(id);
        detailVO.setApprovals(approvals.stream()
                .map(voConverter::toApprovalVO)
                .collect(Collectors.toList()));

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"leave:requests", "leave:detail"}, allEntries = true)
    public String createLeaveRequest(LeaveCreateDTO createDTO) {
        log.info("创建请假申请,创建信息: {}", createDTO);

        validateService.validateCreateRequest(createDTO);

        LeaveRequest request = new LeaveRequest();
        request.setId(idGenerator.generate());
        request.setType(createDTO.getType());
        request.setStartTime(createDTO.getStartTime());
        request.setEndTime(createDTO.getEndTime());
        request.setAttachments(createDTO.getAttachments());
        request.setReason(createDTO.getReason());
        request.setStatus(LeaveStatus.DRAFT.getCode());
        request.setCurrentApprovalLevel(0);
        request.setVersion(0);

        BigDecimal duration = durationCalculator.calculateDuration(
                createDTO.getStartTime(),
                createDTO.getEndTime()
        );
        request.setDuration(duration);

        leaveRequestMapper.insert(request);

        log.info("请假申请创建成功,申请ID: {}", request.getId());
        return request.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"leave:requests", "leave:detail"}, allEntries = true)
    public void updateLeaveRequest(String id, LeaveUpdateDTO updateDTO) {
        log.info("更新请假申请,申请ID: {}, 更新信息: {}", id, updateDTO);

        LeaveRequest request = leaveRequestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(3001, "请假申请不存在");
        }

        validateService.validateUpdateRequest(request, updateDTO);

        if (updateDTO.getType() != null) {
            request.setType(updateDTO.getType());
        }
        if (updateDTO.getStartTime() != null) {
            request.setStartTime(updateDTO.getStartTime());
        }
        if (updateDTO.getEndTime() != null) {
            request.setEndTime(updateDTO.getEndTime());
        }
        if (updateDTO.getAttachments() != null) {
            request.setAttachments(updateDTO.getAttachments());
        }
        if (updateDTO.getReason() != null) {
            request.setReason(updateDTO.getReason());
        }

        if (updateDTO.getStartTime() != null || updateDTO.getEndTime() != null) {
            BigDecimal duration = durationCalculator.calculateDuration(
                    request.getStartTime(),
                    request.getEndTime()
            );
            request.setDuration(duration);
        }

        request.setVersion(request.getVersion() + 1);
        leaveRequestMapper.updateById(request);

        log.info("请假申请更新成功,申请ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"leave:requests", "leave:detail"}, allEntries = true)
    public void deleteLeaveRequest(String id) {
        log.info("删除请假申请,申请ID: {}", id);

        LeaveRequest request = leaveRequestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(3001, "请假申请不存在");
        }

        validateService.validateDeleteRequest(request);

        request.setIsDeleted(1);
        request.setDeletedAt(LocalDateTime.now());
        leaveRequestMapper.updateById(request);

        log.info("请假申请删除成功,申请ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"leave:requests", "leave:detail"}, allEntries = true)
    public void submitLeaveRequest(String id) {
        log.info("提交请假申请,申请ID: {}", id);

        LeaveRequest request = leaveRequestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(3001, "请假申请不存在");
        }

        validateService.validateSubmitRequest(request);

        if (LeaveType.ANNUAL.getCode().equals(request.getType())) {
            BigDecimal balance = leaveBalanceService.getBalance(
                    request.getApplicantId(),
                    request.getStartTime().getYear()
            ).getAnnualRemaining();
            if (balance.compareTo(request.getDuration()) < 0) {
                throw new LeaveBalanceInsufficientException(
                        "年假余额不足,剩余" + balance + "天,需要" + request.getDuration() + "天"
                );
            }
        }

        workflowService.startWorkflow(request);

        request.setStatus(LeaveStatus.PENDING.getCode());
        request.setCurrentApprovalLevel(1);
        leaveRequestMapper.updateById(request);

        log.info("请假申请提交成功,申请ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"leave:requests", "leave:detail"}, allEntries = true)
    public void cancelLeaveRequest(String id) {
        log.info("撤销请假申请,申请ID: {}", id);

        LeaveRequest request = leaveRequestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(3001, "请假申请不存在");
        }

        validateService.validateCancelRequest(request);

        workflowService.cancelWorkflow(request);

        request.setStatus(LeaveStatus.CANCELLED.getCode());
        leaveRequestMapper.updateById(request);

        log.info("请假申请撤销成功,申请ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"leave:requests", "leave:detail"}, allEntries = true)
    public void resubmitLeaveRequest(String id) {
        log.info("重新提交请假申请,申请ID: {}", id);

        LeaveRequest request = leaveRequestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(3001, "请假申请不存在");
        }

        validateService.validateResubmitRequest(request);

        submitLeaveRequest(id);

        log.info("请假申请重新提交成功,申请ID: {}", id);
    }
}
```

---

### 3. LeaveApprovalService (审批服务)

**文件路径**: `com/oa/system/module/leave/service/LeaveApprovalService.java`

```java
package com.oa.system.module.leave.service;

import com.oa.system.common.dto.PageResult;
import com.oa.system.module.leave.dto.request.ApprovalDTO;
import com.oa.system.module.leave.dto.request.LeaveQueryDTO;
import com.oa.system.module.leave.entity.LeaveApproval;
import com.oa.system.module.leave.vo.LeaveDetailVO;
import com.oa.system.module.leave.vo.LeaveRequestVO;

import java.util.List;

/**
 * 审批服务接口
 *
 * @author OA System
 * @since 2026-01-18
 */
public interface LeaveApprovalService {

    PageResult<LeaveRequestVO> getPendingApprovals(LeaveQueryDTO queryDTO);

    PageResult<LeaveRequestVO> getApprovedRequests(LeaveQueryDTO queryDTO);

    List<LeaveApproval> getApprovalsByRequestId(String requestId);

    LeaveDetailVO approveRequest(String requestId, ApprovalDTO approvalDTO);
}
```

---

### 4. LeaveApprovalServiceImpl (实现类)

**文件路径**: `com/oa/system/module/leave/service/impl/LeaveApprovalServiceImpl.java`

```java
package com.oa.system.module.leave.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.system.common.dto.PageResult;
import com.oa.system.common.exception.BusinessException;
import com.oa.system.module.leave.converter.LeaveVOConverter;
import com.oa.system.module.leave.dto.request.ApprovalDTO;
import com.oa.system.module.leave.dto.request.LeaveQueryDTO;
import com.oa.system.module.leave.entity.LeaveApproval;
import com.oa.system.module.leave.entity.LeaveRequest;
import com.oa.system.module.leave.enums.ApprovalStatus;
import com.oa.system.module.leave.enums.LeaveStatus;
import com.oa.system.module.leave.mapper.LeaveApprovalMapper;
import com.oa.system.module.leave.mapper.LeaveRequestMapper;
import com.oa.system.module.leave.service.LeaveApprovalService;
import com.oa.system.module.leave.service.LeaveBalanceService;
import com.oa.system.module.leave.service.LeaveRequestWorkflowService;
import com.oa.system.module.leave.vo.LeaveDetailVO;
import com.oa.system.module.leave.vo.LeaveRequestVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveApprovalServiceImpl
        extends ServiceImpl<LeaveApprovalMapper, LeaveApproval>
        implements LeaveApprovalService {

    private final LeaveApprovalMapper leaveApprovalMapper;
    private final LeaveRequestMapper leaveRequestMapper;
    private final LeaveBalanceService leaveBalanceService;
    private final LeaveRequestWorkflowService workflowService;
    private final LeaveVOConverter voConverter;

    @Override
    @Cacheable(value = "leave:pending", key = "#queryDTO.toString()")
    public PageResult<LeaveRequestVO> getPendingApprovals(LeaveQueryDTO queryDTO) {
        log.info("查询待审批列表,查询条件: {}", queryDTO);

        LambdaQueryWrapper<LeaveRequest> wrapper = Wrappers.<LeaveRequest>lambdaQuery()
                .in(LeaveRequest::getStatus, LeaveStatus.PENDING.getCode(), LeaveStatus.APPROVING.getCode())
                .orderByDesc(LeaveRequest::getCreatedAt);

        if (queryDTO.getDepartmentId() != null) {
            wrapper.eq(LeaveRequest::getDepartmentId, queryDTO.getDepartmentId());
        }
        if (queryDTO.getType() != null) {
            wrapper.eq(LeaveRequest::getType, queryDTO.getType());
        }
        if (queryDTO.getKeyword() != null) {
            wrapper.like(LeaveRequest::getReason, queryDTO.getKeyword());
        }

        List<LeaveRequest> list = leaveRequestMapper.selectList(wrapper);
        List<LeaveRequestVO> voList = list.stream()
                .map(voConverter::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, (long) voList.size(), queryDTO.getPage(), queryDTO.getPageSize());
    }

    @Override
    @Cacheable(value = "leave:approved", key = "#queryDTO.toString()")
    public PageResult<LeaveRequestVO> getApprovedRequests(LeaveQueryDTO queryDTO) {
        log.info("查询已审批列表,查询条件: {}", queryDTO);

        LambdaQueryWrapper<LeaveRequest> wrapper = Wrappers.<LeaveRequest>lambdaQuery()
                .in(LeaveRequest::getStatus, LeaveStatus.APPROVED.getCode(), LeaveStatus.REJECTED.getCode())
                .orderByDesc(LeaveRequest::getUpdatedAt);

        if (queryDTO.getApplicantId() != null) {
            wrapper.eq(LeaveRequest::getApplicantId, queryDTO.getApplicantId());
        }
        if (queryDTO.getDepartmentId() != null) {
            wrapper.eq(LeaveRequest::getDepartmentId, queryDTO.getDepartmentId());
        }
        if (queryDTO.getType() != null) {
            wrapper.eq(LeaveRequest::getType, queryDTO.getType());
        }

        List<LeaveRequest> list = leaveRequestMapper.selectList(wrapper);
        List<LeaveRequestVO> voList = list.stream()
                .map(voConverter::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, (long) voList.size(), queryDTO.getPage(), queryDTO.getPageSize());
    }

    @Override
    public List<LeaveApproval> getApprovalsByRequestId(String requestId) {
        log.info("查询审批记录,申请ID: {}", requestId);

        return leaveApprovalMapper.selectList(
                Wrappers.<LeaveApproval>lambdaQuery()
                        .eq(LeaveApproval::getRequestId, requestId)
                        .orderByAsc(LeaveApproval::getApprovalLevel)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"leave:requests", "leave:detail", "leave:pending", "leave:approved"}, allEntries = true)
    public LeaveDetailVO approveRequest(String requestId, ApprovalDTO approvalDTO) {
        log.info("审批请假申请,申请ID: {}, 审批信息: {}", requestId, approvalDTO);

        LeaveRequest request = leaveRequestMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException(3001, "请假申请不存在");
        }

        if (!LeaveStatus.PENDING.getCode().equals(request.getStatus())
                && !LeaveStatus.APPROVING.getCode().equals(request.getStatus())) {
            throw new BusinessException(3005, "当前状态不允许审批");
        }

        LeaveApproval approval = leaveApprovalMapper.selectOne(
                Wrappers.<LeaveApproval>lambdaQuery()
                        .eq(LeaveApproval::getRequestId, requestId)
                        .eq(LeaveApproval::getApprovalLevel, request.getCurrentApprovalLevel())
                        .eq(LeaveApproval::getStatus, ApprovalStatus.PENDING.getCode())
        );

        if (approval == null) {
            throw new BusinessException(3006, "未找到待审批的记录");
        }

        approval.setStatus(approvalDTO.getStatus());
        approval.setOpinion(approvalDTO.getOpinion());
        approval.setTimestamp(LocalDateTime.now());
        leaveApprovalMapper.updateById(approval);

        if (ApprovalStatus.APPROVED.getCode().equals(approvalDTO.getStatus())) {
            workflowService.approveWorkflow(request, approval);
        } else {
            workflowService.rejectWorkflow(request, approval);
        }

        LeaveDetailVO detailVO = voConverter.toDetailVO(leaveRequestMapper.selectById(requestId));
        detailVO.setApprovals(getApprovalsByRequestId(requestId).stream()
                .map(voConverter::toApprovalVO)
                .collect(Collectors.toList()));

        log.info("请假申请审批成功,申请ID: {}", requestId);
        return detailVO;
    }
}
```

---

### 5. LeaveBalanceService (年假余额服务)

**文件路径**: `com/oa/system/module/leave/service/LeaveBalanceService.java`

```java
package com.oa.system.module.leave.service;

import com.oa.system.common.dto.PageResult;
import com.oa.system.module.leave.dto.request.BalanceQueryDTO;
import com.oa.system.module.leave.dto.request.BalanceUpdateDTO;
import com.oa.system.module.leave.entity.LeaveBalance;
import com.oa.system.module.leave.vo.LeaveBalanceVO;

import java.math.BigDecimal;

/**
 * 年假余额服务接口
 *
 * @author OA System
 * @since 2026-01-18
 */
public interface LeaveBalanceService {

    LeaveBalanceVO getBalance(String employeeId, Integer year);

    PageResult<LeaveBalanceVO> getBalanceList(BalanceQueryDTO queryDTO);

    void updateBalance(BalanceUpdateDTO updateDTO);

    void deductBalance(String employeeId, Integer year, BigDecimal duration, String requestId);

    void rollbackBalance(String employeeId, Integer year, BigDecimal duration, String requestId);

    void initBalance(String employeeId, Integer year);
}
```

---

### 6. LeaveBalanceServiceImpl (实现类)

**文件路径**: `com/oa/system/module/leave/service/impl/LeaveBalanceServiceImpl.java`

```java
package com.oa.system.module.leave.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.system.common.dto.PageResult;
import com.oa.system.common.exception.BusinessException;
import com.oa.system.module.employee.entity.Employee;
import com.oa.system.module.employee.service.EmployeeService;
import com.oa.system.module.leave.converter.LeaveVOConverter;
import com.oa.system.module.leave.dto.request.BalanceQueryDTO;
import com.oa.system.module.leave.dto.request.BalanceUpdateDTO;
import com.oa.system.module.leave.entity.LeaveBalance;
import com.oa.system.module.leave.entity.LeaveUsageLog;
import com.oa.system.module.leave.enums.LeaveType;
import com.oa.system.module.leave.mapper.LeaveBalanceMapper;
import com.oa.system.module.leave.mapper.LeaveUsageLogMapper;
import com.oa.system.module.leave.service.LeaveBalanceService;
import com.oa.system.module.leave.util.AnnualQuotaCalculator;
import com.oa.system.module.leave.vo.LeaveBalanceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveBalanceServiceImpl
        extends ServiceImpl<LeaveBalanceMapper, LeaveBalance>
        implements LeaveBalanceService {

    private final LeaveBalanceMapper leaveBalanceMapper;
    private final LeaveUsageLogMapper leaveUsageLogMapper;
    private final EmployeeService employeeService;
    private final LeaveVOConverter voConverter;
    private final AnnualQuotaCalculator quotaCalculator;

    @Override
    @Cacheable(value = "leave:balance", key = "#employeeId + ':' + #year")
    public LeaveBalanceVO getBalance(String employeeId, Integer year) {
        log.info("查询年假余额,员工ID: {}, 年份: {}", employeeId, year);

        LeaveBalance balance = leaveBalanceMapper.selectOne(
                Wrappers.<LeaveBalance>lambdaQuery()
                        .eq(LeaveBalance::getEmployeeId, employeeId)
                        .eq(LeaveBalance::getYear, year)
        );

        if (balance == null) {
            initBalance(employeeId, year);
            balance = leaveBalanceMapper.selectOne(
                    Wrappers.<LeaveBalance>lambdaQuery()
                            .eq(LeaveBalance::getEmployeeId, employeeId)
                            .eq(LeaveBalance::getYear, year)
            );
        }

        return voConverter.toBalanceVOWithDetails(balance);
    }

    @Override
    public PageResult<LeaveBalanceVO> getBalanceList(BalanceQueryDTO queryDTO) {
        log.info("查询年假余额列表,查询条件: {}", queryDTO);

        LambdaQueryWrapper<LeaveBalance> wrapper = Wrappers.<LeaveBalance>lambdaQuery();

        if (queryDTO.getEmployeeId() != null) {
            wrapper.eq(LeaveBalance::getEmployeeId, queryDTO.getEmployeeId());
        }
        if (queryDTO.getYear() != null) {
            wrapper.eq(LeaveBalance::getYear, queryDTO.getYear());
        }

        List<LeaveBalance> list = leaveBalanceMapper.selectList(wrapper);
        List<LeaveBalanceVO> voList = list.stream()
                .map(voConverter::toBalanceVOWithDetails)
                .collect(Collectors.toList());

        return new PageResult<>(voList, (long) voList.size(), queryDTO.getPage(), queryDTO.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "leave:balance", allEntries = true)
    public void updateBalance(BalanceUpdateDTO updateDTO) {
        log.info("更新年假额度,更新信息: {}", updateDTO);

        LeaveBalance balance = leaveBalanceMapper.selectOne(
                Wrappers.<LeaveBalance>lambdaQuery()
                        .eq(LeaveBalance::getEmployeeId, updateDTO.getEmployeeId())
                        .eq(LeaveBalance::getYear, updateDTO.getYear())
        );

        if (balance == null) {
            throw new BusinessException(3007, "年假记录不存在");
        }

        BigDecimal used = balance.getAnnualUsed();
        BigDecimal total = updateDTO.getAnnualTotal();

        if (used.compareTo(total) > 0) {
            throw new BusinessException(3008, "年假总额不能小于已使用天数");
        }

        balance.setAnnualTotal(total);
        balance.setAnnualRemaining(total.subtract(used));
        leaveBalanceMapper.updateById(balance);

        log.info("年假额度更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "leave:balance", allEntries = true)
    public void deductBalance(String employeeId, Integer year, BigDecimal duration, String requestId) {
        log.info("扣减年假余额,员工ID: {}, 年份: {}, 时长: {}, 申请ID: {}", employeeId, year, duration, requestId);

        LeaveBalance balance = leaveBalanceMapper.selectOne(
                Wrappers.<LeaveBalance>lambdaQuery()
                        .eq(LeaveBalance::getEmployeeId, employeeId)
                        .eq(LeaveBalance::getYear, year)
        );

        if (balance == null) {
            throw new BusinessException(3007, "年假记录不存在");
        }

        if (balance.getAnnualRemaining().compareTo(duration) < 0) {
            throw new BusinessException(3002, "年假余额不足");
        }

        balance.setAnnualUsed(balance.getAnnualUsed().add(duration));
        balance.setAnnualRemaining(balance.getAnnualRemaining().subtract(duration));
        leaveBalanceMapper.updateById(balance);

        LeaveUsageLog log = new LeaveUsageLog();
        log.setEmployeeId(employeeId);
        log.setRequestId(requestId);
        log.setType(LeaveType.ANNUAL.getCode());
        log.setDuration(duration);
        log.setChangeType("deduct");
        leaveUsageLogMapper.insert(log);

        log.info("年假余额扣减成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "leave:balance", allEntries = true)
    public void rollbackBalance(String employeeId, Integer year, BigDecimal duration, String requestId) {
        log.info("回退年假余额,员工ID: {}, 年份: {}, 时长: {}, 申请ID: {}", employeeId, year, duration, requestId);

        LeaveBalance balance = leaveBalanceMapper.selectOne(
                Wrappers.<LeaveBalance>lambdaQuery()
                        .eq(LeaveBalance::getEmployeeId, employeeId)
                        .eq(LeaveBalance::getYear, year)
        );

        if (balance == null) {
            throw new BusinessException(3007, "年假记录不存在");
        }

        balance.setAnnualUsed(balance.getAnnualUsed().subtract(duration));
        balance.setAnnualRemaining(balance.getAnnualRemaining().add(duration));
        leaveBalanceMapper.updateById(balance);

        LeaveUsageLog log = new LeaveUsageLog();
        log.setEmployeeId(employeeId);
        log.setRequestId(requestId);
        log.setType(LeaveType.ANNUAL.getCode());
        log.setDuration(duration);
        log.setChangeType("rollback");
        leaveUsageLogMapper.insert(log);

        log.info("年假余额回退成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "leave:balance", allEntries = true)
    public void initBalance(String employeeId, Integer year) {
        log.info("初始化年假余额,员工ID: {}, 年份: {}", employeeId, year);

        Employee employee = employeeService.getById(employeeId);
        if (employee == null) {
            throw new BusinessException(3009, "员工不存在");
        }

        BigDecimal quota = quotaCalculator.calculateQuota(employee.getWorkYears());

        LeaveBalance balance = new LeaveBalance();
        balance.setEmployeeId(employeeId);
        balance.setYear(year);
        balance.setAnnualTotal(quota);
        balance.setAnnualUsed(BigDecimal.ZERO);
        balance.setAnnualRemaining(quota);
        leaveBalanceMapper.insert(balance);

        log.info("年假余额初始化成功");
    }
}
```

---

## 🔧 辅助Service

### LeaveRequestQueryService

**文件路径**: `com/oa/system/module/leave/service/LeaveRequestQueryService.java`

```java
package com.oa.system.module.leave.service;

import com.oa.system.common.dto.PageResult;
import com.oa.system.module.leave.dto.request.LeaveQueryDTO;
import com.oa.system.module.leave.vo.LeaveRequestVO;

/**
 * 请假申请查询服务
 *
 * @author OA System
 * @since 2026-01-18
 */
public interface LeaveRequestQueryService {

    PageResult<LeaveRequestVO> queryLeaveRequests(LeaveQueryDTO queryDTO);
}
```

### LeaveRequestValidateService

**文件路径**: `com/oa/system/module/leave/service/LeaveRequestValidateService.java`

```java
package com.oa.system.module.leave.service;

import com.oa.system.module.leave.dto.request.LeaveCreateDTO;
import com.oa.system.module.leave.dto.request.LeaveUpdateDTO;
import com.oa.system.module.leave.entity.LeaveRequest;

/**
 * 请假申请验证服务
 *
 * @author OA System
 * @since 2026-01-18
 */
public interface LeaveRequestValidateService {

    void validateCreateRequest(LeaveCreateDTO createDTO);

    void validateUpdateRequest(LeaveRequest request, LeaveUpdateDTO updateDTO);

    void validateDeleteRequest(LeaveRequest request);

    void validateSubmitRequest(LeaveRequest request);

    void validateCancelRequest(LeaveRequest request);

    void validateResubmitRequest(LeaveRequest request);
}
```

### LeaveRequestWorkflowService

**文件路径**: `com/oa/system/module/leave/service/LeaveRequestWorkflowService.java`

```java
package com.oa.system.module.leave.service;

import com.oa.system.module.leave.entity.LeaveApproval;
import com.oa.system.module.leave.entity.LeaveRequest;

/**
 * 请假申请工作流服务
 *
 * @author OA System
 * @since 2026-01-18
 */
public interface LeaveRequestWorkflowService {

    void startWorkflow(LeaveRequest request);

    void approveWorkflow(LeaveRequest request, LeaveApproval approval);

    void rejectWorkflow(LeaveRequest request, LeaveApproval approval);

    void cancelWorkflow(LeaveRequest request);
}
```

---

## 📊 缓存策略

### 缓存配置

| 缓存名称 | 缓存内容 | TTL | 失效策略 |
|---------|---------|-----|---------|
| leave:requests | 请假申请列表 | 5分钟 | 增删改时清除 |
| leave:detail | 请假申请详情 | 10分钟 | 增删改时清除 |
| leave:pending | 待审批列表 | 3分钟 | 审批时清除 |
| leave:approved | 已审批列表 | 5分钟 | 审批时清除 |
| leave:balance | 年假余额 | 10分钟 | 更新时清除 |

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-18
