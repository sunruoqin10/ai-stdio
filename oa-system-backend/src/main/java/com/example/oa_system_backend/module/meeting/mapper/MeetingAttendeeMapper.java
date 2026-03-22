package com.example.oa_system_backend.module.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oa_system_backend.module.meeting.entity.MeetingAttendee;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MeetingAttendeeMapper extends BaseMapper<MeetingAttendee> {

    List<MeetingAttendee> selectByBookingId(@Param("bookingId") String bookingId);

    void deleteByBookingId(@Param("bookingId") String bookingId);
}
