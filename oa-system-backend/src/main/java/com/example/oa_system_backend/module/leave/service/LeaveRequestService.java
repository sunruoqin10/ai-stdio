package com.example.oa_system_backend.module.leave.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.oa_system_backend.module.leave.dto.*;
import com.example.oa_system_backend.module.leave.entity.LeaveRequest;
import com.example.oa_system_backend.module.leave.vo.*;

import java.util.List;

public interface LeaveRequestService extends IService<LeaveRequest> {

    IPage<LeaveRequestVO> getLeaveRequestList(LeaveQueryRequest query);

    LeaveDetailVO getLeaveRequestById(String id);

    String createLeaveRequest(LeaveCreateRequest request);

    LeaveRequest updateLeaveRequest(String id, LeaveUpdateRequest request);

    void deleteLeaveRequest(String id);

    void submitLeaveRequest(String id);

    void cancelLeaveRequest(String id);

    void resubmitLeaveRequest(String id, LeaveUpdateRequest request);
}
