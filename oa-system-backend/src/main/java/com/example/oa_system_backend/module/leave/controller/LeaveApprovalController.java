package com.example.oa_system_backend.module.leave.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.leave.dto.ApprovalRequest;
import com.example.oa_system_backend.module.leave.dto.LeaveQueryRequest;
import com.example.oa_system_backend.module.leave.service.LeaveApprovalService;
import com.example.oa_system_backend.module.leave.vo.LeaveDetailVO;
import com.example.oa_system_backend.module.leave.vo.LeaveRequestVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave/approvals")
@RequiredArgsConstructor
public class LeaveApprovalController {

    private final LeaveApprovalService leaveApprovalService;

    @GetMapping("/pending")
    public ApiResponse<IPage<LeaveRequestVO>> getPendingApprovals(LeaveQueryRequest request) {
        IPage<LeaveRequestVO> result = leaveApprovalService.getPendingApprovals(request);
        return ApiResponse.success(result);
    }

    @GetMapping("/approved")
    public ApiResponse<IPage<LeaveRequestVO>> getApprovedApprovals(LeaveQueryRequest request) {
        IPage<LeaveRequestVO> result = leaveApprovalService.getApprovedApprovals(request);
        return ApiResponse.success(result);
    }

    @PostMapping("/{requestId}/approve")
    public ApiResponse<LeaveDetailVO> approveRequest(
            @PathVariable String requestId,
            @Valid @RequestBody ApprovalRequest approvalRequest) {
        LeaveDetailVO result = leaveApprovalService.approveRequest(requestId, approvalRequest);
        return ApiResponse.success("审批完成", result);
    }

    @GetMapping("/{requestId}/history")
    public ApiResponse<?> getApprovalHistory(@PathVariable String requestId) {
        var history = leaveApprovalService.getApprovalHistory(requestId);
        return ApiResponse.success(history);
    }
}
