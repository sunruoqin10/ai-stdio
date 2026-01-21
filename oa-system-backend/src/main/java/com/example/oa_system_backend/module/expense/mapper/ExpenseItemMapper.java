package com.example.oa_system_backend.module.expense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oa_system_backend.module.expense.entity.ExpenseItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExpenseItemMapper extends BaseMapper<ExpenseItem> {
}
