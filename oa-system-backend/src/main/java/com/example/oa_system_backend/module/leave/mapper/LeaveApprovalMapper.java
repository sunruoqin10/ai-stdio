package com.example.oa_system_backend.module.leave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oa_system_backend.module.leave.entity.LeaveApproval;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface LeaveApprovalMapper extends BaseMapper<LeaveApproval> {

    @Select("SELECT * FROM approval_leave_approval WHERE request_id = #{requestId} ORDER BY approval_level ASC")
    List<LeaveApproval> selectByRequestId(@Param("requestId") String requestId);

    @Select("SELECT * FROM approval_leave_approval WHERE approver_id = #{approverId} AND status = 'pending' ORDER BY timestamp ASC")
    List<LeaveApproval> selectPendingByApproverId(@Param("approverId") String approverId);

    Long countByRequestIdAndLevel(@Param("requestId") String requestId,
                                 @Param("approvalLevel") Integer approvalLevel);

    Long countApprovedByRequestId(@Param("requestId") String requestId);

    Long countRejectedByRequestId(@Param("requestId") String requestId);
}
