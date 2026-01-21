package com.example.oa_system_backend.module.expense.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.module.expense.dto.*;
import com.example.oa_system_backend.module.expense.entity.Expense;
import com.example.oa_system_backend.module.expense.vo.ExpenseDetailVO;
import com.example.oa_system_backend.module.expense.vo.ExpenseStatisticsVO;
import com.example.oa_system_backend.module.expense.vo.ExpenseVO;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {

    IPage<ExpenseVO> getExpenseList(ExpenseQueryRequest query);

    ExpenseDetailVO getExpenseDetail(String id);

    Expense createExpense(ExpenseCreateRequest request);

    Expense updateExpense(String id, ExpenseUpdateRequest request);

    void deleteExpense(String id);

    void submitExpense(String id);

    void deptApprove(String id, ApprovalRequest request);

    void financeApprove(String id, ApprovalRequest request);

    void cancelExpense(String id);

    void createPayment(String id);

    void uploadPaymentProof(String expenseId, String proofUrl);

    IPage<ExpenseVO> getPendingDeptApproval(ExpenseQueryRequest query);

    IPage<ExpenseVO> getPendingFinanceApproval(ExpenseQueryRequest query);

    List<ExpenseStatisticsVO> getDepartmentStats(LocalDate startDate, LocalDate endDate, String departmentId);

    List<ExpenseStatisticsVO> getTypeStats(LocalDate startDate, LocalDate endDate);

    List<ExpenseStatisticsVO> getMonthlyStats(Integer year);

    boolean validateInvoiceUnique(String invoiceNumber);

    Object recognizeInvoice(String imageUrl);
}
