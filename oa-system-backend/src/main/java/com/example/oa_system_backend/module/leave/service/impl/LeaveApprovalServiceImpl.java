package com.example.oa_system_backend.module.leave.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oa_system_backend.common.exception.BusinessException;
import com.example.oa_system_backend.common.utils.SecurityUtils;
import com.example.oa_system_backend.module.leave.dto.ApprovalRequest;
import com.example.oa_system_backend.module.leave.dto.LeaveQueryRequest;
import com.example.oa_system_backend.module.leave.entity.LeaveApproval;
import com.example.oa_system_backend.module.leave.entity.LeaveRequest;
import com.example.oa_system_backend.module.leave.enums.ApprovalStatus;
import com.example.oa_system_backend.module.leave.enums.LeaveStatus;
import com.example.oa_system_backend.module.leave.enums.LeaveType;
import com.example.oa_system_backend.module.leave.mapper.LeaveApprovalMapper;
import com.example.oa_system_backend.module.leave.mapper.LeaveRequestMapper;
import com.example.oa_system_backend.module.leave.service.LeaveApprovalService;
import com.example.oa_system_backend.module.leave.service.LeaveBalanceService;
import com.example.oa_system_backend.module.leave.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveApprovalServiceImpl extends ServiceImpl<LeaveApprovalMapper, LeaveApproval> implements LeaveApprovalService {

    private final LeaveApprovalMapper leaveApprovalMapper;
    private final LeaveRequestMapper leaveRequestMapper;
    private final LeaveBalanceService leaveBalanceService;

    @Override
    public IPage<LeaveRequestVO> getPendingApprovals(LeaveQueryRequest query) {
        log.info("查询待审批列表,查询条件: {}", query);

        query.setStatusList(java.util.Arrays.asList(LeaveStatus.PENDING.getCode(), LeaveStatus.APPROVING.getCode()));
        query.setSortBy("created_at");
        query.setSortOrder("DESC");

        Page<LeaveRequest> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<LeaveRequestVO> result = leaveRequestMapper.selectPageByCondition(page, query);

        result.getRecords().forEach(vo -> {
            vo.setTypeName(LeaveType.fromCode(vo.getType()).getName());
            vo.setStatusName(LeaveStatus.fromCode(vo.getStatus()).getName());
            if (vo.getCurrentApprovalLevel() != null && vo.getDuration() != null) {
                vo.setTotalApprovalLevels(calculateTotalApprovalLevels(vo.getDuration()));
            }
        });

        return result;
    }

    @Override
    public IPage<LeaveRequestVO> getApprovedApprovals(LeaveQueryRequest query) {
        log.info("查询已审批列表,查询条件: {}", query);

        QueryWrapper<LeaveRequest> wrapper = new QueryWrapper<>();
        wrapper.in("status", LeaveStatus.APPROVED.getCode(), LeaveStatus.REJECTED.getCode());
        wrapper.orderByDesc("updated_at");

        if (query.getApplicantId() != null && !query.getApplicantId().isEmpty()) {
            wrapper.eq("applicant_id", query.getApplicantId());
        }
        if (query.getDepartmentId() != null && !query.getDepartmentId().isEmpty()) {
            wrapper.eq("department_id", query.getDepartmentId());
        }
        if (query.getType() != null && !query.getType().isEmpty()) {
            wrapper.eq("type", query.getType());
        }

        Page<LeaveRequest> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<LeaveRequest> result = leaveRequestMapper.selectPage(page, wrapper);

        IPage<LeaveRequestVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getCurrent());
        voPage.setRecords(result.getRecords().stream().map(entity -> {
            LeaveRequestVO vo = new LeaveRequestVO();
            BeanUtils.copyProperties(entity, vo);
            vo.setTypeName(LeaveType.fromCode(entity.getType()).getName());
            vo.setStatusName(LeaveStatus.fromCode(entity.getStatus()).getName());
            if (entity.getCurrentApprovalLevel() != null && entity.getDuration() != null) {
                vo.setTotalApprovalLevels(calculateTotalApprovalLevels(entity.getDuration()));
            }
            return vo;
        }).collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public List<LeaveApproval> getApprovalHistory(String requestId) {
        log.info("查询审批历史,申请ID: {}", requestId);

        return leaveApprovalMapper.selectByRequestId(requestId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveDetailVO approveRequest(String requestId, ApprovalRequest approvalRequest) {
        log.info("审批请假申请,申请ID: {}, 审批信息: {}", requestId, approvalRequest);

        LeaveRequest request = leaveRequestMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException(3001, "请假申请不存在");
        }

        if (!LeaveStatus.PENDING.getCode().equals(request.getStatus())
                && !LeaveStatus.APPROVING.getCode().equals(request.getStatus())) {
            throw new BusinessException(3005, "当前状态不允许审批");
        }

        validateApprovePermission(request, getCurrentUserId());
        validateApprovalOpinion(approvalRequest);

        LeaveApproval approval = leaveApprovalMapper.selectOne(
                new QueryWrapper<LeaveApproval>()
                        .eq("request_id", requestId)
                        .eq("approval_level", request.getCurrentApprovalLevel())
                        .eq("status", ApprovalStatus.PENDING.getCode())
        );

        if (approval == null) {
            throw new BusinessException(3006, "未找到待审批的记录");
        }

        approval.setStatus(approvalRequest.getStatus());
        approval.setOpinion(approvalRequest.getOpinion());
        approval.setTimestamp(LocalDateTime.now());
        leaveApprovalMapper.updateById(approval);

        if (ApprovalStatus.APPROVED.getCode().equals(approvalRequest.getStatus())) {
            approveWorkflow(request, approval);
        } else {
            rejectWorkflow(request, approval);
        }

        return getLeaveDetailVO(requestId);
    }

    private void validateApprovePermission(LeaveRequest request, String approverId) {
        if (request.getApplicantId().equals(approverId)) {
            throw new BusinessException(3004, "不能审批自己的申请");
        }
    }

    private void validateApprovalOpinion(ApprovalRequest approvalRequest) {
        if (ApprovalStatus.REJECTED.getCode().equals(approvalRequest.getStatus())) {
            if (approvalRequest.getOpinion() == null || approvalRequest.getOpinion().trim().isEmpty()) {
                throw new BusinessException(3007, "拒绝时必须填写审批意见");
            }
        }
    }

    private void approveWorkflow(LeaveRequest request, LeaveApproval approval) {
        Integer totalLevels = calculateTotalApprovalLevels(request.getDuration());
        Integer currentLevel = request.getCurrentApprovalLevel();

        if (currentLevel < totalLevels) {
            request.setStatus(LeaveStatus.APPROVING.getCode());
            request.setCurrentApprovalLevel(currentLevel + 1);
        } else {
            request.setStatus(LeaveStatus.APPROVED.getCode());

            if (LeaveType.ANNUAL.getCode().equals(request.getType())) {
                leaveBalanceService.deductBalance(
                        request.getApplicantId(),
                        request.getStartTime().getYear(),
                        request.getDuration(),
                        request.getId()
                );
            }
        }

        request.setUpdatedAt(LocalDateTime.now());
        leaveRequestMapper.updateById(request);
    }

    private void rejectWorkflow(LeaveRequest request, LeaveApproval approval) {
        request.setStatus(LeaveStatus.REJECTED.getCode());
        request.setUpdatedAt(LocalDateTime.now());
        leaveRequestMapper.updateById(request);
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

    private LeaveDetailVO getLeaveDetailVO(String requestId) {
        LeaveRequest request = leaveRequestMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException(3001, "请假申请不存在");
        }

        LeaveDetailVO detailVO = new LeaveDetailVO();
        BeanUtils.copyProperties(request, detailVO);
        detailVO.setTypeName(LeaveType.fromCode(request.getType()).getName());
        detailVO.setStatusName(LeaveStatus.fromCode(request.getStatus()).getName());
        detailVO.setTotalApprovalLevels(calculateTotalApprovalLevels(request.getDuration()));

        List<LeaveApproval> approvals = getApprovalHistory(requestId);
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
}
