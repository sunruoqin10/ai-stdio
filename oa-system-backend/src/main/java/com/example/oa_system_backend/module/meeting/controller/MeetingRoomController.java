package com.example.oa_system_backend.module.meeting.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.meeting.dto.RoomForm;
import com.example.oa_system_backend.module.meeting.entity.MeetingRoom;
import com.example.oa_system_backend.module.meeting.service.MeetingRoomService;
import com.example.oa_system_backend.module.meeting.vo.AvailabilityVO;
import com.example.oa_system_backend.module.meeting.vo.RoomDetailVO;
import com.example.oa_system_backend.module.meeting.vo.RoomVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meeting/rooms")
@RequiredArgsConstructor
public class MeetingRoomController {

    private final MeetingRoomService roomService;

    @GetMapping
    public ApiResponse<IPage<RoomVO>> getRoomList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<RoomVO> result = roomService.getRoomList(status, page, size);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<RoomDetailVO> getRoomDetail(@PathVariable String id) {
        RoomDetailVO detail = roomService.getRoomDetail(id);
        return ApiResponse.success(detail);
    }

    @PostMapping
    public ApiResponse<MeetingRoom> createRoom(@RequestBody RoomForm form) {
        MeetingRoom room = roomService.createRoom(form);
        return ApiResponse.success(room);
    }

    @PutMapping("/{id}")
    public ApiResponse<MeetingRoom> updateRoom(@PathVariable String id, @RequestBody RoomForm form) {
        MeetingRoom room = roomService.updateRoom(id, form);
        return ApiResponse.success(room);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRoom(@PathVariable String id) {
        roomService.deleteRoom(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/availability")
    public ApiResponse<List<AvailabilityVO>> getRoomAvailability(
            @PathVariable String id,
            @RequestParam String date) {
        List<AvailabilityVO> availability = roomService.getRoomAvailability(id, date);
        return ApiResponse.success(availability);
    }
}
