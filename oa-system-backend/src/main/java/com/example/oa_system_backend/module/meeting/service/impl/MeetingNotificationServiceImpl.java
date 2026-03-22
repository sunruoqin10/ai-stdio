package com.example.oa_system_backend.module.meeting.service.impl;

import com.example.oa_system_backend.module.meeting.entity.MeetingNotification;
import com.example.oa_system_backend.module.meeting.mapper.MeetingNotificationMapper;
import com.example.oa_system_backend.module.meeting.service.MeetingNotificationService;
import com.example.oa_system_backend.module.meeting.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingNotificationServiceImpl implements MeetingNotificationService {

    private final MeetingNotificationMapper notificationMapper;

    @Override
    public List<NotificationVO> getNotifications(String recipientId) {
        List<MeetingNotification> notifications = notificationMapper.selectByRecipientId(recipientId);
        return notifications.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(String id) {
        notificationMapper.markAsRead(id);
        log.info("标记通知已读: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createNotification(MeetingNotification notification) {
        notificationMapper.insert(notification);
        log.info("创建会议通知: {}", notification.getId());
    }

    private NotificationVO convertToVO(MeetingNotification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setType(notification.getType());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setBookingId(notification.getBookingId());
        vo.setIsRead(notification.getIsRead() != null && notification.getIsRead() == 1);
        vo.setCreatedAt(notification.getCreatedAt());
        return vo;
    }
}
