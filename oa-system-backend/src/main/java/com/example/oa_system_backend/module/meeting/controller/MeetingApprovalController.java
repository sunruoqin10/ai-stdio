package com.example.oa_system_backend.module.meeting.controller;

import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.meeting.dto.ApprovalForm;
import com.example.oa_system_backend.module.meeting.entity.MeetingBooking;
import com.example.oa_system_backend.module.meeting.service.MeetingBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meeting/approvals")
@RequiredArgsConstructor
public class MeetingApprovalController {

    private final MeetingBookingService bookingService;

    @GetMapping("/pending")
    public ApiResponse<List<MeetingBooking>> getPendingApprovals() {
        List<MeetingBooking> pending = bookingService.getPendingApprovals();
        return ApiResponse.success(pending);
    }

    @PostMapping("/{id}")
    public ApiResponse<Void> approve(@PathVariable String id, @RequestBody ApprovalForm form) {
        bookingService.approveBooking(id, form.getStatus(), form.getOpinion());
        return ApiResponse.success(null);
    }
}
