package com.example.oa_system_backend.module.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.meeting.entity.MeetingRoom;
import org.apache.ibatis.annotations.Param;

public interface MeetingRoomMapper extends BaseMapper<MeetingRoom> {

    IPage<MeetingRoom> selectPageByCondition(Page<MeetingRoom> page, @Param("status") String status);

    Long countActiveBookings(@Param("roomId") String roomId);
}
