package com.example.oa_system_backend.module.leave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oa_system_backend.module.leave.entity.LeaveBalance;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface LeaveBalanceMapper extends BaseMapper<LeaveBalance> {

    @Select("SELECT * FROM approval_leave_balance WHERE employee_id = #{employeeId} AND year = #{year}")
    LeaveBalance selectByEmployeeIdAndYear(@Param("employeeId") String employeeId,
                                         @Param("year") Integer year);

    List<LeaveBalance> selectByCondition(@Param("employeeId") String employeeId,
                                       @Param("year") Integer year);

    @Select("SELECT COUNT(*) as total, SUM(annual_total) as total_quota, SUM(annual_used) as total_used, SUM(annual_remaining) as total_remaining FROM approval_leave_balance WHERE year = #{year}")
    Map<String, Object> selectSummaryByYear(@Param("year") Integer year);
}
