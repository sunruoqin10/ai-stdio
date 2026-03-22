package com.example.oa_system_backend.module.meeting.service;

import com.example.oa_system_backend.module.meeting.entity.MeetingNotification;
import com.example.oa_system_backend.module.meeting.vo.NotificationVO;

import java.util.List;

public interface MeetingNotificationService {

    List<NotificationVO> getNotifications(String recipientId);

    void markAsRead(String id);

    void createNotification(MeetingNotification notification);
}
