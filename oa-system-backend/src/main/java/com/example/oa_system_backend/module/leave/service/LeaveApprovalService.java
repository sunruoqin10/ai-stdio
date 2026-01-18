package com.example.oa_system_backend.module.leave.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.oa_system_backend.module.leave.dto.ApprovalRequest;
import com.example.oa_system_backend.module.leave.dto.LeaveQueryRequest;
import com.example.oa_system_backend.module.leave.entity.LeaveApproval;
import com.example.oa_system_backend.module.leave.vo.*;

import java.util.List;

public interface LeaveApprovalService extends IService<LeaveApproval> {

    IPage<LeaveRequestVO> getPendingApprovals(LeaveQueryRequest query);

    IPage<LeaveRequestVO> getApprovedApprovals(LeaveQueryRequest query);

    List<LeaveApproval> getApprovalHistory(String requestId);

    LeaveDetailVO approveRequest(String requestId, ApprovalRequest approvalRequest);
}
