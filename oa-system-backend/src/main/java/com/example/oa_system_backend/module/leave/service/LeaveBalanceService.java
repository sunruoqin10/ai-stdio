package com.example.oa_system_backend.module.leave.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.oa_system_backend.module.leave.dto.*;
import com.example.oa_system_backend.module.leave.entity.LeaveBalance;
import com.example.oa_system_backend.module.leave.vo.LeaveBalanceVO;

import java.math.BigDecimal;

public interface LeaveBalanceService extends IService<LeaveBalance> {

    LeaveBalanceVO getBalance(String employeeId, Integer year);

    LeaveBalanceVO getBalanceByEmployeeId(String employeeId);

    IPage<LeaveBalanceVO> getBalanceList(BalanceQueryRequest query);

    LeaveBalance updateBalance(BalanceUpdateRequest request);

    void deductBalance(String employeeId, Integer year, BigDecimal duration, String requestId);

    void rollbackBalance(String employeeId, Integer year, BigDecimal duration, String requestId);

    LeaveBalance initBalance(String employeeId, Integer year);

    LeaveBalance getBalanceByEmployeeIdAndYear(String employeeId, Integer year);
}
