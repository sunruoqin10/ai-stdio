package com.example.oa_system_backend.module.meeting.controller;

import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.meeting.service.MeetingNotificationService;
import com.example.oa_system_backend.module.meeting.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meeting/notifications")
@RequiredArgsConstructor
public class MeetingNotificationController {

    private final MeetingNotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationVO>> getNotifications(@RequestParam String recipientId) {
        List<NotificationVO> notifications = notificationService.getNotifications(recipientId);
        return ApiResponse.success(notifications);
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ApiResponse.success(null);
    }
}
