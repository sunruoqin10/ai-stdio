package com.example.oa_system_backend.module.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oa_system_backend.module.meeting.entity.MeetingNotification;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MeetingNotificationMapper extends BaseMapper<MeetingNotification> {

    List<MeetingNotification> selectByRecipientId(@Param("recipientId") String recipientId);

    void markAsRead(@Param("id") String id);
}
