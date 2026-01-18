package com.example.oa_system_backend.module.leave.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.leave.dto.BalanceQueryRequest;
import com.example.oa_system_backend.module.leave.dto.BalanceUpdateRequest;
import com.example.oa_system_backend.module.leave.entity.LeaveBalance;
import com.example.oa_system_backend.module.leave.service.LeaveBalanceService;
import com.example.oa_system_backend.module.leave.vo.LeaveBalanceVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave/balance")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    @GetMapping
    public ApiResponse<IPage<LeaveBalanceVO>> getBalanceList(BalanceQueryRequest request) {
        IPage<LeaveBalanceVO> result = leaveBalanceService.getBalanceList(request);
        return ApiResponse.success(result);
    }

    @GetMapping("/{employeeId}")
    public ApiResponse<LeaveBalanceVO> getBalanceByEmployeeId(@PathVariable String employeeId) {
        LeaveBalanceVO balance = leaveBalanceService.getBalanceByEmployeeId(employeeId);
        return ApiResponse.success(balance);
    }

    @PostMapping("/{employeeId}")
    public ApiResponse<LeaveBalance> updateBalance(
            @PathVariable String employeeId,
            @Valid @RequestBody BalanceUpdateRequest request) {
        request.setEmployeeId(employeeId);
        LeaveBalance balance = leaveBalanceService.updateBalance(request);
        return ApiResponse.success("更新成功", balance);
    }

    @PostMapping("/init/{employeeId}")
    public ApiResponse<LeaveBalance> initBalance(@PathVariable String employeeId) {
        LeaveBalance balance = leaveBalanceService.initBalance(employeeId, null);
        return ApiResponse.success("初始化成功", balance);
    }
}
