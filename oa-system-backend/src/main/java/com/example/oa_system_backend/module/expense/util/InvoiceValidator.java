package com.example.oa_system_backend.module.expense.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.oa_system_backend.module.expense.entity.ExpenseInvoice;
import com.example.oa_system_backend.module.expense.mapper.ExpenseInvoiceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class InvoiceValidator {

    private final ExpenseInvoiceMapper expenseInvoiceMapper;

    private static final Pattern INVOICE_NUMBER_PATTERN = Pattern.compile("^\\d{8}$|^\\d{20}$");

    public boolean validateFormat(String invoiceNumber) {
        if (invoiceNumber == null || invoiceNumber.isEmpty()) {
            return false;
        }
        return INVOICE_NUMBER_PATTERN.matcher(invoiceNumber).matches();
    }

    public boolean validateUnique(String invoiceNumber) {
        if (!validateFormat(invoiceNumber)) {
            return false;
        }

        Long count = expenseInvoiceMapper.selectCount(
                new QueryWrapper<ExpenseInvoice>()
                        .eq("invoice_number", invoiceNumber)
        );

        return count == 0;
    }
}
