package com.example.oa_system_backend.module.leave.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oa_system_backend.common.exception.BusinessException;
import com.example.oa_system_backend.common.utils.SecurityUtils;
import com.example.oa_system_backend.module.department.entity.Department;
import com.example.oa_system_backend.module.department.mapper.DepartmentMapper;
import com.example.oa_system_backend.module.employee.entity.Employee;
import com.example.oa_system_backend.module.employee.mapper.EmployeeMapper;
import com.example.oa_system_backend.module.leave.dto.*;
import com.example.oa_system_backend.module.leave.entity.LeaveApproval;
import com.example.oa_system_backend.module.leave.entity.LeaveRequest;
import com.example.oa_system_backend.module.leave.enums.ApprovalStatus;
import com.example.oa_system_backend.module.leave.enums.LeaveStatus;
import com.example.oa_system_backend.module.leave.enums.LeaveType;
import com.example.oa_system_backend.module.leave.mapper.LeaveRequestMapper;
import com.example.oa_system_backend.module.leave.service.LeaveApprovalService;
import com.example.oa_system_backend.module.leave.service.LeaveBalanceService;
import com.example.oa_system_backend.module.leave.service.LeaveRequestService;
import com.example.oa_system_backend.module.leave.util.LeaveDurationCalculator;
import com.example.oa_system_backend.module.leave.util.LeaveIdGenerator;
import com.example.oa_system_backend.module.leave.vo.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl extends ServiceImpl<LeaveRequestMapper, LeaveRequest> implements LeaveRequestService {

    private final LeaveRequestMapper leaveRequestMapper;
    private final LeaveApprovalService leaveApprovalService;
    private final LeaveBalanceService leaveBalanceService;
    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final LeaveDurationCalculator durationCalculator;
    private final LeaveIdGenerator idGenerator;
    private final com.example.oa_system_backend.module.leave.util.LeaveDictLabelUtil dictLabelUtil;
    private final ObjectMapper objectMapper;

    public IPage<LeaveRequestVO> getLeaveRequests(LeaveQueryRequest query) {
        log.info("查询请假申请列表,查询条件: {}", query);

        Page<LeaveRequest> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<LeaveRequestVO> result = leaveRequestMapper.selectPageByCondition(page, query);

        dictLabelUtil.fillDictLabelsForList(result.getRecords());
        result.getRecords().forEach(vo -> {
            if (vo.getCurrentApprovalLevel() != null && vo.getDuration() != null) {
                vo.setTotalApprovalLevels(calculateTotalApprovalLevels(vo.getDuration()));
            }
        });

        return result;
    }

    @Override
    public IPage<LeaveRequestVO> getLeaveRequestList(LeaveQueryRequest query) {
        return getLeaveRequests(query);
    }

    @Override
    public LeaveDetailVO getLeaveRequestById(String id) {
        return getLeaveDetail(id);
    }

    public LeaveDetailVO getLeaveDetail(String id) {
        log.info("获取请假申请详情,申请ID: {}", id);

        LeaveRequest request = leaveRequestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(3001, "请假申请不存在");
        }

        LeaveDetailVO detailVO = new LeaveDetailVO();
        BeanUtils.copyProperties(request, detailVO);
        detailVO.setTypeName(LeaveType.fromCode(request.getType()).getName());
        detailVO.setStatusName(LeaveStatus.fromCode(request.getStatus()).getName());
        detailVO.setTotalApprovalLevels(calculateTotalApprovalLevels(request.getDuration()));
        
        // 转换附件列表JSON字符串为List<String>
        if (request.getAttachments() != null) {
            try {
                List<String> attachments = objectMapper.readValue(request.getAttachments(), new TypeReference<List<String>>() {});
                detailVO.setAttachments(attachments);
            } catch (Exception e) {
                log.error("解析附件列表失败", e);
                // 如果解析失败，尝试作为单个URL处理
                List<String> singleAttachment = Collections.singletonList(request.getAttachments());
                detailVO.setAttachments(singleAttachment);
            }
        }

        Employee applicant = employeeMapper.selectById(request.getApplicantId());
        if (applicant != null) {
            detailVO.setApplicantName(applicant.getName());
            detailVO.setApplicantPosition(applicant.getPosition());
            detailVO.setApplicantPhone(applicant.getPhone());
            detailVO.setApplicantEmail(applicant.getEmail());
            detailVO.setApplicantAvatar(applicant.getAvatar());
            if (applicant.getDepartmentId() != null) {
                Department department = departmentMapper.selectById(applicant.getDepartmentId());
                if (department != null) {
                    detailVO.setDepartmentName(department.getName());
                }
            }
            if (applicant.getManagerId() != null) {
                Employee manager = employeeMapper.selectById(applicant.getManagerId());
                if (manager != null) {
                    detailVO.setManagerName(manager.getName());
                }
            }
        }

        List<LeaveApproval> approvals = leaveApprovalService.getApprovalHistory(id);
        List<ApprovalRecordVO> approvalVOs = approvals.stream().map(approval -> {
            ApprovalRecordVO vo = new ApprovalRecordVO();
            BeanUtils.copyProperties(approval, vo);
            vo.setStatusName(ApprovalStatus.fromCode(approval.getStatus()).getName());
            vo.setIsCurrent(request.getCurrentApprovalLevel() != null
                    && request.getCurrentApprovalLevel().equals(approval.getApprovalLevel()));
            return vo;
        }).collect(Collectors.toList());

        detailVO.setApprovals(approvalVOs);

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createLeaveRequest(LeaveCreateRequest request) {
        log.info("创建请假申请,创建信息: {}", request);

        validateCreateRequest(request);

        LeaveRequest leaveRequest = new LeaveRequest();
        BeanUtils.copyProperties(request, leaveRequest);
        leaveRequest.setId(idGenerator.generate());
        leaveRequest.setStatus(LeaveStatus.DRAFT.getCode());
        leaveRequest.setCurrentApprovalLevel(0);
        leaveRequest.setCreatedAt(LocalDateTime.now());
        leaveRequest.setUpdatedAt(LocalDateTime.now());

        String currentUserId = getCurrentUserId();
        leaveRequest.setApplicantId(currentUserId);

        Employee employee = employeeMapper.selectById(currentUserId);
        if (employee != null && employee.getDepartmentId() != null) {
            leaveRequest.setDepartmentId(employee.getDepartmentId());
        }

        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            try {
                leaveRequest.setAttachments(objectMapper.writeValueAsString(request.getAttachments()));
            } catch (Exception e) {
                log.error("转换附件列表失败", e);
                leaveRequest.setAttachments("[]");
            }
        } else {
            leaveRequest.setAttachments("[]");
        }

        BigDecimal duration = durationCalculator.calculateDuration(
                request.getStartTime().toLocalDate(),
                request.getEndTime().toLocalDate()
        );
        leaveRequest.setDuration(duration);

        leaveRequestMapper.insert(leaveRequest);

        log.info("请假申请创建成功,申请ID: {}", leaveRequest.getId());
        return leaveRequest.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveRequest updateLeaveRequest(String id, LeaveUpdateRequest request) {
        log.info("更新请假申请,申请ID: {}, 更新信息: {}", id, request);

        LeaveRequest leaveRequest = leaveRequestMapper.selectById(id);
        if (leaveRequest == null) {
            throw new BusinessException(3001, "请假申请不存在");
        }

        validateUpdateRequest(leaveRequest, request);

        if (request.getType() != null) {
            leaveRequest.setType(request.getType());
        }
        if (request.getStartTime() != null) {
            leaveRequest.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            leaveRequest.setEndTime(request.getEndTime());
        }
        if (request.getAttachments() != null) {
            try {
                leaveRequest.setAttachments(objectMapper.writeValueAsString(request.getAttachments()));
            } catch (Exception e) {
                log.error("转换附件列表失败", e);
            }
        } else {
            // 清除附件
            leaveRequest.setAttachments(null);
        }
        if (request.getReason() != null) {
            leaveRequest.setReason(request.getReason());
        }

        if (request.getStartTime() != null || request.getEndTime() != null) {
            BigDecimal duration = durationCalculator.calculateDuration(
                    leaveRequest.getStartTime().toLocalDate(),
                    leaveRequest.getEndTime().toLocalDate()
            );
            leaveRequest.setDuration(duration);
        }

        leaveRequest.setUpdatedAt(LocalDateTime.now());
        leaveRequestMapper.updateById(leaveRequest);

        log.info("请假申请更新成功,申请ID: {}", id);
        return leaveRequest;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLeaveRequest(String id) {
        log.info("删除请假申请,申请ID: {}", id);

        LeaveRequest leaveRequest = leaveRequestMapper.selectById(id);
        if (leaveRequest == null) {
            throw new BusinessException(3001, "请假申请不存在");
        }

        validateDeleteRequest(leaveRequest);

        leaveRequestMapper.deleteById(id);

        log.info("请假申请删除成功,申请ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitLeaveRequest(String id) {
        log.info("提交请假申请,申请ID: {}", id);

        LeaveRequest request = leaveRequestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(3001, "请假申请不存在");
        }

        validateSubmitRequest(request);

        if (LeaveType.ANNUAL.getCode().equals(request.getType())) {
            Integer year = request.getStartTime().getYear();
            var balance = leaveBalanceService.getBalanceByEmployeeIdAndYear(request.getApplicantId(), year);
            if (balance == null) {
                leaveBalanceService.initBalance(request.getApplicantId(), year);
                balance = leaveBalanceService.getBalanceByEmployeeIdAndYear(request.getApplicantId(), year);
            }
            if (balance.getAnnualRemaining().compareTo(request.getDuration()) < 0) {
                throw new BusinessException(3002, "年假余额不足,剩余" + balance.getAnnualRemaining() + "天,需要" + request.getDuration() + "天");
            }
        }

        startWorkflow(request);

        request.setStatus(LeaveStatus.PENDING.getCode());
        request.setCurrentApprovalLevel(1);
        request.setUpdatedAt(LocalDateTime.now());
        leaveRequestMapper.updateById(request);

        log.info("请假申请提交成功,申请ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelLeaveRequest(String id) {
        log.info("撤销请假申请,申请ID: {}", id);

        LeaveRequest request = leaveRequestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(3001, "请假申请不存在");
        }

        validateCancelRequest(request);

        cancelWorkflow(request);

        request.setStatus(LeaveStatus.CANCELLED.getCode());
        request.setUpdatedAt(LocalDateTime.now());
        leaveRequestMapper.updateById(request);

        log.info("请假申请撤销成功,申请ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resubmitLeaveRequest(String id, LeaveUpdateRequest request) {
        log.info("重新提交请假申请,申请ID: {}, 更新信息: {}", id, request);

        LeaveRequest leaveRequest = leaveRequestMapper.selectById(id);
        if (leaveRequest == null) {
            throw new BusinessException(3001, "请假申请不存在");
        }

        validateResubmitRequest(leaveRequest);

        updateLeaveRequest(id, request);

        submitLeaveRequest(id);

        log.info("请假申请重新提交成功,申请ID: {}", id);
    }

    private void validateCreateRequest(LeaveCreateRequest request) {
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BusinessException(3017, "结束时间必须晚于开始时间");
        }
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(3018, "开始时间不能早于当前时间");
        }

        Employee employee = employeeMapper.selectById(getCurrentUserId());
        if (employee == null) {
            throw new BusinessException(3008, "申请人不存在");
        }
    }

    private void validateUpdateRequest(LeaveRequest request, LeaveUpdateRequest updateRequest) {
        if (!LeaveStatus.DRAFT.getCode().equals(request.getStatus())) {
            throw new BusinessException(3005, "只能编辑草稿状态的申请");
        }

        if (updateRequest.getStartTime() != null && updateRequest.getEndTime() != null) {
            if (updateRequest.getStartTime().isAfter(updateRequest.getEndTime())) {
                throw new BusinessException(3017, "结束时间必须晚于开始时间");
            }
        }

        Long conflictCount = leaveRequestMapper.countTimeConflict(
                request.getApplicantId(),
                updateRequest.getStartTime() != null ? updateRequest.getStartTime() : request.getStartTime(),
                updateRequest.getEndTime() != null ? updateRequest.getEndTime() : request.getEndTime(),
                request.getId()
        );
        if (conflictCount > 0) {
            throw new BusinessException(3003, "请假时间冲突");
        }
    }

    private void validateDeleteRequest(LeaveRequest request) {
        if (!LeaveStatus.DRAFT.getCode().equals(request.getStatus())) {
            throw new BusinessException(3005, "只能删除草稿状态的申请");
        }
    }

    private void validateSubmitRequest(LeaveRequest request) {
        if (!LeaveStatus.DRAFT.getCode().equals(request.getStatus())
                && !LeaveStatus.REJECTED.getCode().equals(request.getStatus())) {
            throw new BusinessException(3005, "只能提交草稿或已拒绝状态的申请");
        }

        Long conflictCount = leaveRequestMapper.countTimeConflict(
                request.getApplicantId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getId()
        );
        if (conflictCount > 0) {
            throw new BusinessException(3003, "请假时间冲突");
        }
    }

    private void validateCancelRequest(LeaveRequest request) {
        if (!LeaveStatus.PENDING.getCode().equals(request.getStatus())) {
            throw new BusinessException(3005, "只能撤销待审批状态的申请");
        }
    }

    private void validateResubmitRequest(LeaveRequest request) {
        if (!LeaveStatus.REJECTED.getCode().equals(request.getStatus())) {
            throw new BusinessException(3005, "只能重新提交已拒绝状态的申请");
        }
    }

    private void startWorkflow(LeaveRequest request) {
        Integer totalLevels = calculateTotalApprovalLevels(request.getDuration());
        Employee applicant = employeeMapper.selectById(request.getApplicantId());
        if (applicant == null) {
            throw new BusinessException(3008, "申请人不存在");
        }

        for (int level = 1; level <= totalLevels; level++) {
            LeaveApproval approval = new LeaveApproval();
            approval.setRequestId(request.getId());
            approval.setApprovalLevel(level);
            approval.setStatus(ApprovalStatus.PENDING.getCode());
            approval.setTimestamp(LocalDateTime.now());

            Employee approver = null;
            if (level == 1) {
                approver = employeeMapper.selectById(applicant.getManagerId());
            } else if (level == 2) {
                if (applicant.getDepartmentId() != null) {
                    Department department = departmentMapper.selectById(applicant.getDepartmentId());
                    if (department != null && department.getLeaderId() != null) {
                        approver = employeeMapper.selectById(department.getLeaderId());
                    }
                }
            } else if (level == 3) {
                QueryWrapper<Employee> wrapper = new QueryWrapper<>();
                wrapper.like("position", "经理");
                wrapper.orderByDesc("level");
                wrapper.last("LIMIT 1");
                approver = employeeMapper.selectOne(wrapper);
            }

            if (approver != null) {
                approval.setApproverId(approver.getId());
                approval.setApproverName(approver.getName());
            } else {
                log.warn("审批级别{}未找到审批人，申请ID: {}", level, request.getId());
            }

            leaveApprovalService.save(approval);
        }
    }

    private void cancelWorkflow(LeaveRequest request) {
        QueryWrapper<LeaveApproval> wrapper = new QueryWrapper<>();
        wrapper.eq("request_id", request.getId());
        leaveApprovalService.remove(wrapper);
    }

    private Integer calculateTotalApprovalLevels(BigDecimal duration) {
        if (duration == null) {
            return 1;
        }
        if (duration.compareTo(new BigDecimal("3")) <= 0) {
            return 1;
        } else if (duration.compareTo(new BigDecimal("7")) <= 0) {
            return 2;
        } else {
            return 3;
        }
    }

    private String getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }
}
