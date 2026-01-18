package com.example.oa_system_backend.module.leave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oa_system_backend.module.leave.entity.LeaveUsageLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface LeaveUsageLogMapper extends BaseMapper<LeaveUsageLog> {

    @Select("SELECT * FROM approval_leave_usage_log WHERE employee_id = #{employeeId} AND year = #{year} ORDER BY created_at DESC")
    List<LeaveUsageLog> selectByEmployeeIdAndYear(@Param("employeeId") String employeeId,
                                                  @Param("year") Integer year);

    @Select("SELECT * FROM approval_leave_usage_log WHERE request_id = #{requestId} ORDER BY created_at DESC")
    List<LeaveUsageLog> selectByRequestId(@Param("requestId") String requestId);

    List<Map<String, Object>> selectSummaryByEmployeeIdAndYear(@Param("employeeId") String employeeId,
                                                            @Param("year") Integer year);
}
