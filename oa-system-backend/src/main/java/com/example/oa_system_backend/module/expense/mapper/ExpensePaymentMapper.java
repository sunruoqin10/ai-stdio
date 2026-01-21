package com.example.oa_system_backend.module.expense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oa_system_backend.module.expense.entity.ExpensePayment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExpensePaymentMapper extends BaseMapper<ExpensePayment> {
}
