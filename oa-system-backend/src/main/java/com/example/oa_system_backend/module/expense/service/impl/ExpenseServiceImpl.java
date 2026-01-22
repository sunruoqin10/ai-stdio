package com.example.oa_system_backend.module.expense.service.impl;

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
import com.example.oa_system_backend.module.expense.dto.*;
import com.example.oa_system_backend.module.expense.entity.*;
import com.example.oa_system_backend.module.expense.enums.*;
import com.example.oa_system_backend.module.expense.mapper.*;
import com.example.oa_system_backend.module.expense.service.ExpenseService;
import com.example.oa_system_backend.module.expense.util.ExpenseIdGenerator;
import com.example.oa_system_backend.module.expense.util.InvoiceValidator;
import com.example.oa_system_backend.module.expense.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl extends ServiceImpl<ExpenseMapper, Expense>
        implements ExpenseService {

    private final ExpenseMapper expenseMapper;
    private final ExpenseItemMapper expenseItemMapper;
    private final ExpenseInvoiceMapper expenseInvoiceMapper;
    private final ExpensePaymentMapper expensePaymentMapper;
    private final ExpenseIdGenerator expenseIdGenerator;
    private final InvoiceValidator invoiceValidator;
    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Expense createExpense(ExpenseCreateRequest request) {
        for (ExpenseCreateRequest.ExpenseInvoiceRequest invoiceReq : request.getInvoices()) {
            String invoiceNumber = invoiceReq.getInvoiceNumber();
            if (invoiceNumber == null || invoiceNumber.isEmpty()) {
                throw new BusinessException(4001, "发票号不能为空");
            }
            if (!invoiceValidator.validateUnique(invoiceNumber)) {
                throw new BusinessException(4001, "发票号 " + invoiceNumber + " 已被使用");
            }
        }

        BigDecimal invoiceTotal = request.getInvoices().stream()
                .map(ExpenseCreateRequest.ExpenseInvoiceRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal itemsTotal = request.getItems().stream()
                .map(ExpenseCreateRequest.ExpenseItemRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (invoiceTotal.compareTo(itemsTotal) < 0) {
            throw new BusinessException(4002, "发票总金额不能小于明细总金额");
        }

        ExpenseType expenseType = ExpenseType.fromCode(request.getType());
        if (itemsTotal.compareTo(expenseType.getApprovalThreshold()) > 0) {
            checkLargeAmountApproval(SecurityUtils.getCurrentUserId(), itemsTotal);
        }

        String expenseId = expenseIdGenerator.generate();
        Expense expense = new Expense();
        expense.setId(expenseId);
        expense.setApplicantId(SecurityUtils.getCurrentUserId());
        // 暂时设置为默认部门ID，后续可以从用户信息中获取
        expense.setDepartmentId("DEPT001");
        expense.setType(request.getType());
        expense.setAmount(itemsTotal);
        expense.setReason(request.getReason());
        expense.setApplyDate(LocalDate.now());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setStatus(ExpenseStatus.DRAFT.getCode());
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());

        save(expense);

        int sortOrder = 1;
        for (ExpenseCreateRequest.ExpenseItemRequest itemReq : request.getItems()) {
            ExpenseItem item = new ExpenseItem();
            item.setExpenseId(expenseId);
            item.setDescription(itemReq.getDescription());
            item.setAmount(itemReq.getAmount());
            item.setExpenseDate(itemReq.getExpenseDate());
            item.setCategory(itemReq.getCategory());
            item.setSortOrder(sortOrder++);
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            expenseItemMapper.insert(item);
        }

        for (ExpenseCreateRequest.ExpenseInvoiceRequest invoiceReq : request.getInvoices()) {
            ExpenseInvoice invoice = new ExpenseInvoice();
            invoice.setExpenseId(expenseId);
            invoice.setInvoiceType(invoiceReq.getInvoiceType());
            invoice.setInvoiceNumber(invoiceReq.getInvoiceNumber());
            invoice.setAmount(invoiceReq.getAmount());
            invoice.setInvoiceDate(invoiceReq.getInvoiceDate());
            invoice.setImageUrl(invoiceReq.getImageUrl());
            invoice.setVerified(false);
            invoice.setCreatedAt(LocalDateTime.now());
            invoice.setUpdatedAt(LocalDateTime.now());
            expenseInvoiceMapper.insert(invoice);
        }

        log.info("创建报销单成功: {}", expenseId);
        return expense;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitExpense(String id) {
        Expense expense = getById(id);
        if (expense == null) {
            throw new BusinessException(4003, "报销单不存在");
        }

        ExpenseStatus status = ExpenseStatus.fromCode(expense.getStatus());
        if (!status.canSubmit()) {
            throw new BusinessException(4004, "当前状态不允许提交");
        }

        Long invoiceCount = expenseInvoiceMapper.selectCount(
                new QueryWrapper<ExpenseInvoice>()
                        .eq("expense_id", id)
        );
        if (invoiceCount == 0) {
            throw new BusinessException(4005, "请至少上传一张发票");
        }

        String deptLeaderId = getDepartmentLeader(expense.getDepartmentId());
        expense.setStatus(ExpenseStatus.DEPT_PENDING.getCode());
        expense.setDeptApproverId(deptLeaderId);
        expense.setDeptApproverName(getEmployeeName(deptLeaderId));
        expense.setDeptApprovalStatus("pending");
        expense.setUpdatedAt(LocalDateTime.now());

        updateById(expense);

        log.info("提交报销单成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deptApprove(String id, ApprovalRequest request) {
        Expense expense = getById(id);
        if (expense == null) {
            throw new BusinessException(4003, "报销单不存在");
        }

        if (!ExpenseStatus.DEPT_PENDING.getCode().equals(expense.getStatus())) {
            throw new BusinessException(4006, "当前状态不允许部门审批");
        }

        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!currentUserId.equals(expense.getDeptApproverId())) {
            throw new BusinessException(4007, "您不是该报销单的部门审批人");
        }

        LocalDateTime now = LocalDateTime.now();

        if ("approved".equals(request.getStatus())) {
            String financeApproverId = getFinanceApprover();
            expense.setStatus(ExpenseStatus.FINANCE_PENDING.getCode());
            expense.setDeptApproverId(currentUserId);
            expense.setDeptApproverName(getEmployeeName(currentUserId));
            expense.setDeptApprovalStatus("approved");
            expense.setDeptApprovalOpinion(request.getOpinion());
            expense.setDeptApprovalTime(now);
            expense.setFinanceApproverId(financeApproverId);
            expense.setFinanceApproverName(getEmployeeName(financeApproverId));
            expense.setFinanceApprovalStatus("pending");
        } else {
            expense.setStatus(ExpenseStatus.REJECTED.getCode());
            expense.setDeptApproverId(currentUserId);
            expense.setDeptApproverName(getEmployeeName(currentUserId));
            expense.setDeptApprovalStatus("rejected");
            expense.setDeptApprovalOpinion(request.getOpinion());
            expense.setDeptApprovalTime(now);
        }
        expense.setUpdatedAt(now);
        updateById(expense);

        log.info("部门审批完成: {}, 结果: {}", id, request.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void financeApprove(String id, ApprovalRequest request) {
        Expense expense = getById(id);
        if (expense == null) {
            throw new BusinessException(4003, "报销单不存在");
        }

        if (!ExpenseStatus.FINANCE_PENDING.getCode().equals(expense.getStatus())) {
            throw new BusinessException(4008, "当前状态不允许财务审批");
        }

        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!currentUserId.equals(expense.getFinanceApproverId())) {
            throw new BusinessException(4009, "您不是该报销单的财务审批人");
        }

        LocalDateTime now = LocalDateTime.now();

        if ("approved".equals(request.getStatus())) {
            createPaymentRecord(expense);
            expense.setStatus(ExpenseStatus.PAID.getCode());
            expense.setFinanceApproverId(currentUserId);
            expense.setFinanceApproverName(getEmployeeName(currentUserId));
            expense.setFinanceApprovalStatus("approved");
            expense.setFinanceApprovalOpinion(request.getOpinion());
            expense.setFinanceApprovalTime(now);
        } else {
            expense.setStatus(ExpenseStatus.REJECTED.getCode());
            expense.setFinanceApproverId(currentUserId);
            expense.setFinanceApproverName(getEmployeeName(currentUserId));
            expense.setFinanceApprovalStatus("rejected");
            expense.setFinanceApprovalOpinion(request.getOpinion());
            expense.setFinanceApprovalTime(now);
        }
        expense.setUpdatedAt(now);
        updateById(expense);

        log.info("财务审批完成: {}, 结果: {}", id, request.getStatus());
    }

    private void createPaymentRecord(Expense expense) {
        String bankAccount = getEmployeeBankAccount(expense.getApplicantId());
        ExpensePayment payment = new ExpensePayment();
        payment.setExpenseId(expense.getId());
        payment.setAmount(expense.getAmount());
        payment.setPaymentMethod(PaymentMethod.BANK_TRANSFER.getCode());
        payment.setPaymentDate(LocalDate.now());
        payment.setBankAccount(bankAccount);
        payment.setStatus("pending");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        expensePaymentMapper.insert(payment);
    }

    private void checkLargeAmountApproval(String applicantId, BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
        }
        else if (amount.compareTo(new BigDecimal("5000")) > 0) {
        }

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        BigDecimal monthlyTotal = getMonthlyTotalAmount(applicantId, monthStart, LocalDate.now());
        monthlyTotal = monthlyTotal.add(amount);

        if (monthlyTotal.compareTo(new BigDecimal("50000")) > 0) {
        }
    }

    private BigDecimal getMonthlyTotalAmount(String applicantId, LocalDate start, LocalDate end) {
        return expenseMapper.selectMonthlyTotalAmount(applicantId, start, end);
    }

    private String getDepartmentLeader(String departmentId) {
        Department department = departmentMapper.selectById(departmentId);
        if (department == null) {
            throw new BusinessException(4010, "部门不存在");
        }
        return department.getLeaderId();
    }

    private String getFinanceApprover() {
        return "FINANCE_ID";
    }

    private String getEmployeeName(String employeeId) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            return "Unknown";
        }
        return employee.getName();
    }

    private String getEmployeeBankAccount(String employeeId) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            return "Unknown";
        }
        // 暂时返回默认银行账号，后续可以从Employee类中添加bankAccount字段
        return "6222021234567890123";
    }

    @Override
    public IPage<ExpenseVO> getExpenseList(ExpenseQueryRequest query) {
        // 设置当前用户为申请人，确保只返回当前用户的报销单
        query.setApplicantId(SecurityUtils.getCurrentUserId());
        Page<ExpenseVO> page = new Page<>(query.getPage(), query.getPageSize());
        return expenseMapper.selectPageByCondition(page, query);
    }

    @Override
    public ExpenseDetailVO getExpenseDetail(String id) {
        ExpenseVO expenseVO = expenseMapper.selectDetailById(id);
        if (expenseVO == null) {
            throw new BusinessException(4003, "报销单不存在");
        }

        ExpenseDetailVO detailVO = new ExpenseDetailVO();
        detailVO.setId(expenseVO.getId());
        detailVO.setApplicantId(expenseVO.getApplicantId());
        detailVO.setApplicantName(expenseVO.getApplicantName());
        detailVO.setDepartmentId(expenseVO.getDepartmentId());
        detailVO.setDepartmentName(expenseVO.getDepartmentName());
        detailVO.setType(expenseVO.getType());
        detailVO.setTypeName(expenseVO.getTypeName());
        detailVO.setAmount(expenseVO.getAmount());
        detailVO.setReason(expenseVO.getReason());
        detailVO.setApplyDate(expenseVO.getApplyDate());
        detailVO.setExpenseDate(expenseVO.getExpenseDate());
        detailVO.setStatus(expenseVO.getStatus());
        detailVO.setStatusName(expenseVO.getStatusName());
        detailVO.setDeptApproverName(expenseVO.getDeptApproverName());
        detailVO.setDeptApprovalStatus(expenseVO.getDeptApprovalStatus());
        detailVO.setDeptApprovalTime(expenseVO.getDeptApprovalTime());
        detailVO.setFinanceApproverName(expenseVO.getFinanceApproverName());
        detailVO.setFinanceApprovalStatus(expenseVO.getFinanceApprovalStatus());
        detailVO.setFinanceApprovalTime(expenseVO.getFinanceApprovalTime());
        detailVO.setPaymentDate(expenseVO.getPaymentDate());
        detailVO.setCreatedAt(expenseVO.getCreatedAt());
        detailVO.setUpdatedAt(expenseVO.getUpdatedAt());
        detailVO.setItemCount(expenseVO.getItemCount());
        detailVO.setInvoiceCount(expenseVO.getInvoiceCount());

        List<ExpenseItem> items = expenseItemMapper.selectList(
                new QueryWrapper<ExpenseItem>()
                        .eq("expense_id", id)
                        .orderByAsc("sort_order")
        );
        detailVO.setItems(items.stream().map(item -> {
            ExpenseDetailVO.ExpenseItemVO itemVO = new ExpenseDetailVO.ExpenseItemVO();
            itemVO.setId(item.getId());
            itemVO.setDescription(item.getDescription());
            itemVO.setAmount(item.getAmount());
            itemVO.setExpenseDate(item.getExpenseDate());
            itemVO.setCategory(item.getCategory());
            return itemVO;
        }).collect(Collectors.toList()));

        List<ExpenseInvoice> invoices = expenseInvoiceMapper.selectList(
                new QueryWrapper<ExpenseInvoice>()
                        .eq("expense_id", id)
                        .orderByDesc("invoice_date")
        );
        detailVO.setInvoices(invoices.stream().map(invoice -> {
            ExpenseDetailVO.ExpenseInvoiceVO invoiceVO = new ExpenseDetailVO.ExpenseInvoiceVO();
            invoiceVO.setId(invoice.getId());
            invoiceVO.setInvoiceType(invoice.getInvoiceType());
            invoiceVO.setInvoiceTypeName(InvoiceType.fromCode(invoice.getInvoiceType()).getName());
            invoiceVO.setInvoiceNumber(invoice.getInvoiceNumber());
            invoiceVO.setAmount(invoice.getAmount());
            invoiceVO.setInvoiceDate(invoice.getInvoiceDate());
            invoiceVO.setImageUrl(invoice.getImageUrl());
            invoiceVO.setVerified(invoice.getVerified());
            return invoiceVO;
        }).collect(Collectors.toList()));

        ExpensePayment payment = expensePaymentMapper.selectOne(
                new QueryWrapper<ExpensePayment>()
                        .eq("expense_id", id)
        );
        if (payment != null) {
            ExpenseDetailVO.ExpensePaymentVO paymentVO = new ExpenseDetailVO.ExpensePaymentVO();
            paymentVO.setId(payment.getId());
            paymentVO.setAmount(payment.getAmount());
            paymentVO.setPaymentMethod(payment.getPaymentMethod());
            paymentVO.setPaymentMethodName(PaymentMethod.fromCode(payment.getPaymentMethod()).getName());
            paymentVO.setPaymentDate(payment.getPaymentDate());
            paymentVO.setBankAccount(payment.getBankAccount());
            paymentVO.setProof(payment.getProof());
            paymentVO.setStatus(payment.getStatus());
            paymentVO.setStatusName("pending".equals(payment.getStatus()) ? "待处理" : "已完成");
            paymentVO.setRemark(payment.getRemark());
            detailVO.setPayment(paymentVO);
        }

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Expense updateExpense(String id, ExpenseUpdateRequest request) {
        Expense expense = getById(id);
        if (expense == null) {
            throw new BusinessException(4003, "报销单不存在");
        }

        ExpenseStatus status = ExpenseStatus.fromCode(expense.getStatus());
        if (!status.canEdit()) {
            throw new BusinessException(4010, "当前状态不允许编辑");
        }

        if (request.getVersion() != null && !request.getVersion().equals(expense.getVersion())) {
            throw new BusinessException(4011, "数据已被修改，请刷新后重试");
        }

        if (request.getReason() != null) {
            expense.setReason(request.getReason());
        }

        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(request.getExpenseDate());
        }

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            expenseItemMapper.delete(
                    new QueryWrapper<ExpenseItem>()
                            .eq("expense_id", id)
            );

            int sortOrder = 1;
            for (ExpenseCreateRequest.ExpenseItemRequest itemReq : request.getItems()) {
                ExpenseItem item = new ExpenseItem();
                item.setExpenseId(id);
                item.setDescription(itemReq.getDescription());
                item.setAmount(itemReq.getAmount());
                item.setExpenseDate(itemReq.getExpenseDate());
                item.setCategory(itemReq.getCategory());
                item.setSortOrder(sortOrder++);
                item.setCreatedAt(LocalDateTime.now());
                item.setUpdatedAt(LocalDateTime.now());
                expenseItemMapper.insert(item);
            }
        }

        if (request.getInvoices() != null && !request.getInvoices().isEmpty()) {
            expenseInvoiceMapper.delete(
                    new QueryWrapper<ExpenseInvoice>()
                            .eq("expense_id", id)
            );

            for (ExpenseCreateRequest.ExpenseInvoiceRequest invoiceReq : request.getInvoices()) {
                ExpenseInvoice invoice = new ExpenseInvoice();
                invoice.setExpenseId(id);
                invoice.setInvoiceType(invoiceReq.getInvoiceType());
                invoice.setInvoiceNumber(invoiceReq.getInvoiceNumber());
                invoice.setAmount(invoiceReq.getAmount());
                invoice.setInvoiceDate(invoiceReq.getInvoiceDate());
                invoice.setImageUrl(invoiceReq.getImageUrl());
                invoice.setCreatedAt(LocalDateTime.now());
                invoice.setUpdatedAt(LocalDateTime.now());
                expenseInvoiceMapper.insert(invoice);
            }
        }

        expense.setUpdatedAt(LocalDateTime.now());
        updateById(expense);

        log.info("更新报销单成功: {}", id);
        return expense;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExpense(String id) {
        Expense expense = getById(id);
        if (expense == null) {
            throw new BusinessException(4003, "报销单不存在");
        }

        ExpenseStatus status = ExpenseStatus.fromCode(expense.getStatus());
        if (!status.canDelete()) {
            throw new BusinessException(4012, "当前状态不允许删除");
        }

        expense.setDeletedAt(LocalDateTime.now());
        updateById(expense);

        log.info("删除报销单成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelExpense(String id) {
        Expense expense = getById(id);
        if (expense == null) {
            throw new BusinessException(4003, "报销单不存在");
        }

        ExpenseStatus status = ExpenseStatus.fromCode(expense.getStatus());
        if (!status.canCancel()) {
            throw new BusinessException(4013, "当前状态不允许撤销");
        }

        expense.setStatus(ExpenseStatus.REJECTED.getCode());
        expense.setUpdatedAt(LocalDateTime.now());
        updateById(expense);

        log.info("撤销报销单成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPayment(String id) {
        Expense expense = getById(id);
        if (expense == null) {
            throw new BusinessException(4003, "报销单不存在");
        }

        if (!ExpenseStatus.PAID.getCode().equals(expense.getStatus())) {
            throw new BusinessException(4014, "当前状态不允许创建打款记录");
        }

        String bankAccount = getEmployeeBankAccount(expense.getApplicantId());
        ExpensePayment payment = new ExpensePayment();
        payment.setExpenseId(expense.getId());
        payment.setAmount(expense.getAmount());
        payment.setPaymentMethod(PaymentMethod.BANK_TRANSFER.getCode());
        payment.setPaymentDate(LocalDate.now());
        payment.setBankAccount(bankAccount);
        payment.setStatus("pending");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        expensePaymentMapper.insert(payment);

        log.info("创建打款记录成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadPaymentProof(String expenseId, String proofUrl) {
        Expense expense = getById(expenseId);
        if (expense == null) {
            throw new BusinessException(4003, "报销单不存在");
        }

        if (!ExpenseStatus.PAID.getCode().equals(expense.getStatus())) {
            throw new BusinessException(4015, "当前状态不允许上传打款凭证");
        }

        expense.setPaymentProof(proofUrl);
        expense.setUpdatedAt(LocalDateTime.now());
        updateById(expense);

        log.info("上传打款凭证成功: {}", expenseId);
    }

    @Override
    public IPage<ExpenseVO> getPendingDeptApproval(ExpenseQueryRequest query) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Page<ExpenseVO> page = new Page<>(query.getPage(), query.getPageSize());
        return expenseMapper.selectPendingDeptApproval(page, currentUserId);
    }

    @Override
    public IPage<ExpenseVO> getPendingFinanceApproval(ExpenseQueryRequest query) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Page<ExpenseVO> page = new Page<>(query.getPage(), query.getPageSize());
        return expenseMapper.selectPendingFinanceApproval(page, currentUserId);
    }

    @Override
    public List<ExpenseStatisticsVO> getDepartmentStats(LocalDate startDate, LocalDate endDate, String departmentId) {
        return expenseMapper.selectDepartmentStats(startDate, endDate, departmentId);
    }

    @Override
    public List<ExpenseStatisticsVO> getTypeStats(LocalDate startDate, LocalDate endDate) {
        return expenseMapper.selectTypeStats(startDate, endDate);
    }

    @Override
    public List<ExpenseStatisticsVO> getMonthlyStats(Integer year) {
        return expenseMapper.selectMonthlyStats(year);
    }

    @Override
    public boolean validateInvoiceUnique(String invoiceNumber) {
        return invoiceValidator.validateUnique(invoiceNumber);
    }

    @Override
    public Object recognizeInvoice(String imageUrl) {
        log.info("发票OCR识别,图片URL: {}", imageUrl);
        return null;
    }
}
