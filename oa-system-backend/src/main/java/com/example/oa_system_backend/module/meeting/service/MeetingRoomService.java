package com.example.oa_system_backend.module.meeting.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.module.meeting.dto.RoomForm;
import com.example.oa_system_backend.module.meeting.entity.MeetingRoom;
import com.example.oa_system_backend.module.meeting.vo.AvailabilityVO;
import com.example.oa_system_backend.module.meeting.vo.RoomDetailVO;
import com.example.oa_system_backend.module.meeting.vo.RoomVO;

import java.util.List;

public interface MeetingRoomService {

    IPage<RoomVO> getRoomList(String status, int page, int size);

    RoomDetailVO getRoomDetail(String id);

    MeetingRoom createRoom(RoomForm form);

    MeetingRoom updateRoom(String id, RoomForm form);

    void deleteRoom(String id);

    List<AvailabilityVO> getRoomAvailability(String roomId, String date);
}
