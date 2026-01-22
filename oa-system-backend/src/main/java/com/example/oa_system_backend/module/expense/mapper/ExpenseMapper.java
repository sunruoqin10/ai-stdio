package com.example.oa_system_backend.module.expense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.expense.entity.Expense;
import com.example.oa_system_backend.module.expense.dto.ExpenseQueryRequest;
import com.example.oa_system_backend.module.expense.vo.ExpenseVO;
import com.example.oa_system_backend.module.expense.vo.ExpenseStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ExpenseMapper extends BaseMapper<Expense> {

    IPage<ExpenseVO> selectPageByCondition(
        Page<ExpenseVO> page,
        @Param("query") ExpenseQueryRequest query
    );

    ExpenseVO selectDetailById(@Param("id") String id);

    IPage<ExpenseVO> selectPendingDeptApproval(
        Page<ExpenseVO> page,
        @Param("approverId") String approverId
    );

    IPage<ExpenseVO> selectPendingFinanceApproval(
        Page<ExpenseVO> page,
        @Param("approverId") String approverId
    );

    BigDecimal selectMonthlyTotalAmount(
        @Param("applicantId") String applicantId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    List<ExpenseStatisticsVO> selectDepartmentStats(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("departmentId") String departmentId
    );

    List<ExpenseStatisticsVO> selectTypeStats(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    List<ExpenseStatisticsVO> selectMonthlyStats(
        @Param("year") Integer year
    );

    /**
     * 根据日期前缀查询报销单ID列表
     * @param datePrefix 日期前缀，格式为 EXPyyyyMMdd
     * @return 报销单ID列表
     */
    List<String> selectIdsByDatePrefix(@Param("datePrefix") String datePrefix);
}
