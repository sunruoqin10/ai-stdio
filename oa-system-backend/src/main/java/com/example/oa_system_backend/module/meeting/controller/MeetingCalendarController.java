package com.example.oa_system_backend.module.meeting.controller;

import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.meeting.entity.MeetingBooking;
import com.example.oa_system_backend.module.meeting.service.MeetingBookingService;
import com.example.oa_system_backend.module.meeting.vo.CalendarEventVO;
import com.example.oa_system_backend.module.meeting.vo.CalendarResourceVO;
import com.example.oa_system_backend.module.meeting.vo.RoomVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meeting/calendar")
@RequiredArgsConstructor
public class MeetingCalendarController {

    private final MeetingBookingService bookingService;

    @GetMapping("/events")
    public ApiResponse<List<CalendarEventVO>> getCalendarEvents(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) List<String> roomIds) {
        List<MeetingBooking> bookings = bookingService.getApprovedByDateRange(startDate, endDate);
        List<CalendarEventVO> events = bookings.stream().map(this::convertToEvent).collect(Collectors.toList());
        return ApiResponse.success(events);
    }

    @GetMapping("/resources")
    public ApiResponse<List<CalendarResourceVO>> getCalendarResources() {
        return ApiResponse.success(null);
    }

    private CalendarEventVO convertToEvent(MeetingBooking booking) {
        CalendarEventVO vo = new CalendarEventVO();
        vo.setId(booking.getId());
        vo.setTitle(booking.getTitle());
        vo.setStart(booking.getStartTime());
        vo.setEnd(booking.getEndTime());
        vo.setResourceId(booking.getRoomId());
        vo.setStatus(booking.getStatus());
        vo.setOrganizer(booking.getBookerName());
        vo.setDepartment(booking.getDepartmentName());
        vo.setLevel(booking.getLevel());
        vo.setAttendeeCount(booking.getParticipantCount());
        return vo;
    }
}
