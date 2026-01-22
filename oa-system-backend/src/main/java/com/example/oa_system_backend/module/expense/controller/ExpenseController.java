package com.example.oa_system_backend.module.expense.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.expense.dto.*;
import com.example.oa_system_backend.module.expense.entity.Expense;
import com.example.oa_system_backend.module.expense.service.ExpenseService;
import com.example.oa_system_backend.module.expense.vo.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/expense")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    @PreAuthorize("hasAuthority('expense:view')")
    public ApiResponse<IPage<ExpenseVO>> getExpenseList(ExpenseQueryRequest query) {
        log.info("查询报销单列表, 参数: {}", query);
        IPage<ExpenseVO> result = expenseService.getExpenseList(query);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('expense:view')")
    public ApiResponse<ExpenseDetailVO> getExpenseDetail(@PathVariable String id) {
        log.info("查询报销单详情, id: {}", id);
        ExpenseDetailVO detail = expenseService.getExpenseDetail(id);
        log.info("返回的报销单详情, version: {}", detail.getVersion());
        return ApiResponse.success(detail);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('expense:create')")
    public ApiResponse<Expense> createExpense(@Valid @RequestBody ExpenseCreateRequest request) {
        log.info("创建报销单, 类型: {}", request.getType());
        Expense expense = expenseService.createExpense(request);
        return ApiResponse.success(expense);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('expense:edit')")
    public ApiResponse<Expense> updateExpense(
            @PathVariable String id,
            @Valid @RequestBody ExpenseUpdateRequest request) {
        log.info("更新报销单, id: {}, version: {}", id, request.getVersion());
        log.info("完整的请求数据: {}", request);
        Expense expense = expenseService.updateExpense(id, request);
        return ApiResponse.success(expense);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('expense:delete')")
    public ApiResponse<Void> deleteExpense(@PathVariable String id) {
        log.info("删除报销单, id: {}", id);
        expenseService.deleteExpense(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('expense:submit')")
    public ApiResponse<Void> submitExpense(@PathVariable String id) {
        log.info("提交报销单, id: {}", id);
        expenseService.submitExpense(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/dept-approval")
    @PreAuthorize("hasAuthority('expense:dept_approve')")
    public ApiResponse<Void> deptApprove(
            @PathVariable String id,
            @Valid @RequestBody ApprovalRequest request) {
        log.info("部门审批, id: {}, 状态: {}", id, request.getStatus());
        expenseService.deptApprove(id, request);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/finance-approval")
    @PreAuthorize("hasAuthority('expense:finance_approve')")
    public ApiResponse<Void> financeApprove(
            @PathVariable String id,
            @Valid @RequestBody ApprovalRequest request) {
        log.info("财务审批, id: {}, 状态: {}", id, request.getStatus());
        expenseService.financeApprove(id, request);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('expense:cancel')")
    public ApiResponse<Void> cancelExpense(@PathVariable String id) {
        log.info("撤销报销单, id: {}", id);
        expenseService.cancelExpense(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/payment")
    @PreAuthorize("hasAuthority('expense:payment')")
    public ApiResponse<Void> createPayment(@PathVariable String id) {
        log.info("创建打款记录, id: {}", id);
        expenseService.createPayment(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/payment-proof")
    @PreAuthorize("hasAuthority('expense:payment')")
    public ApiResponse<Void> uploadPaymentProof(
            @PathVariable String id,
            @RequestParam String proofUrl) {
        log.info("上传打款凭证, id: {}", id);
        expenseService.uploadPaymentProof(id, proofUrl);
        return ApiResponse.success(null);
    }

    @GetMapping("/pending/dept")
    @PreAuthorize("hasAuthority('expense:dept_approve')")
    public ApiResponse<IPage<ExpenseVO>> getPendingDeptApproval(ExpenseQueryRequest query) {
        IPage<ExpenseVO> result = expenseService.getPendingDeptApproval(query);
        return ApiResponse.success(result);
    }

    @GetMapping("/pending/finance")
    @PreAuthorize("hasAuthority('expense:finance_approve')")
    public ApiResponse<IPage<ExpenseVO>> getPendingFinanceApproval(ExpenseQueryRequest query) {
        IPage<ExpenseVO> result = expenseService.getPendingFinanceApproval(query);
        return ApiResponse.success(result);
    }

    @GetMapping("/invoices/validate")
    public ApiResponse<Boolean> validateInvoice(@RequestParam String number) {
        log.info("验证发票唯一性, 号码: {}", number);
        boolean valid = expenseService.validateInvoiceUnique(number);
        return ApiResponse.success(valid);
    }

    @PostMapping("/invoices/ocr")
    public ApiResponse<Object> recognizeInvoice(@RequestBody InvoiceOCRRequest request) {
        log.info("OCR识别发票, URL: {}", request.getImageUrl());
        Object result = expenseService.recognizeInvoice(request.getImageUrl());
        return ApiResponse.success(result);
    }

    @GetMapping("/stats/department")
    @PreAuthorize("hasAuthority('expense:stats')")
    public ApiResponse<List<ExpenseStatisticsVO>> getDepartmentStats(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) String departmentId) {
        log.info("查询部门统计, 日期范围: {} - {}", startDate, endDate);
        List<ExpenseStatisticsVO> result = expenseService.getDepartmentStats(startDate, endDate, departmentId);
        return ApiResponse.success(result);
    }

    @GetMapping("/stats/type")
    @PreAuthorize("hasAuthority('expense:stats')")
    public ApiResponse<List<ExpenseStatisticsVO>> getTypeStats(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("查询类型统计, 日期范围: {} - {}", startDate, endDate);
        List<ExpenseStatisticsVO> result = expenseService.getTypeStats(startDate, endDate);
        return ApiResponse.success(result);
    }

    @GetMapping("/stats/monthly")
    @PreAuthorize("hasAuthority('expense:stats')")
    public ApiResponse<List<ExpenseStatisticsVO>> getMonthlyStats(
            @RequestParam Integer year) {
        log.info("查询月度统计, 年份: {}", year);
        List<ExpenseStatisticsVO> result = expenseService.getMonthlyStats(year);
        return ApiResponse.success(result);
    }
}
