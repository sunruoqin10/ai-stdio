package com.example.oa_system_backend.module.leave.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.leave.dto.*;
import com.example.oa_system_backend.module.leave.entity.LeaveRequest;
import com.example.oa_system_backend.module.leave.service.LeaveRequestService;
import com.example.oa_system_backend.module.leave.vo.LeaveDetailVO;
import com.example.oa_system_backend.module.leave.vo.LeaveRequestVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave/requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @GetMapping
    public ApiResponse<IPage<LeaveRequestVO>> getLeaveRequestList(LeaveQueryRequest request) {
        IPage<LeaveRequestVO> result = leaveRequestService.getLeaveRequestList(request);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<LeaveDetailVO> getLeaveRequestById(@PathVariable String id) {
        LeaveDetailVO detail = leaveRequestService.getLeaveRequestById(id);
        return ApiResponse.success(detail);
    }

    @PostMapping
    public ApiResponse<LeaveRequest> createLeaveRequest(@Valid @RequestBody LeaveCreateRequest request) {
        String id = leaveRequestService.createLeaveRequest(request);
        LeaveRequest leaveRequest = leaveRequestService.getById(id);
        return ApiResponse.success("创建成功", leaveRequest);
    }

    @PutMapping("/{id}")
    public ApiResponse<LeaveRequest> updateLeaveRequest(
            @PathVariable String id,
            @Valid @RequestBody LeaveUpdateRequest request) {
        LeaveRequest leaveRequest = leaveRequestService.updateLeaveRequest(id, request);
        return ApiResponse.success("更新成功", leaveRequest);
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submitLeaveRequest(@PathVariable String id) {
        leaveRequestService.submitLeaveRequest(id);
        return ApiResponse.success("提交成功", null);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancelLeaveRequest(@PathVariable String id) {
        leaveRequestService.cancelLeaveRequest(id);
        return ApiResponse.success("取消成功", null);
    }

    @PostMapping("/{id}/resubmit")
    public ApiResponse<Void> resubmitLeaveRequest(
            @PathVariable String id,
            @Valid @RequestBody LeaveUpdateRequest request) {
        leaveRequestService.resubmitLeaveRequest(id, request);
        return ApiResponse.success("重新提交成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteLeaveRequest(@PathVariable String id) {
        leaveRequestService.deleteLeaveRequest(id);
        return ApiResponse.success("删除成功", null);
    }
}
