package com.example.oa_system_backend.module.meeting.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.common.vo.ApiResponse;
import com.example.oa_system_backend.module.meeting.dto.BookingForm;
import com.example.oa_system_backend.module.meeting.dto.BookingQueryRequest;
import com.example.oa_system_backend.module.meeting.entity.MeetingBooking;
import com.example.oa_system_backend.module.meeting.service.MeetingBookingService;
import com.example.oa_system_backend.module.meeting.vo.BookingDetailVO;
import com.example.oa_system_backend.module.meeting.vo.BookingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meeting/bookings")
@RequiredArgsConstructor
public class MeetingBookingController {

    private final MeetingBookingService bookingService;

    @GetMapping
    public ApiResponse<IPage<BookingVO>> getBookingList(BookingQueryRequest query) {
        IPage<BookingVO> result = bookingService.getBookingList(query);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingDetailVO> getBookingDetail(@PathVariable String id) {
        BookingDetailVO detail = bookingService.getBookingDetail(id);
        return ApiResponse.success(detail);
    }

    @PostMapping
    public ApiResponse<MeetingBooking> createBooking(@RequestBody BookingForm form) {
        MeetingBooking booking = bookingService.createBooking(form);
        return ApiResponse.success(booking);
    }

    @PutMapping("/{id}")
    public ApiResponse<MeetingBooking> updateBooking(@PathVariable String id, @RequestBody BookingForm form) {
        MeetingBooking booking = bookingService.updateBooking(id, form);
        return ApiResponse.success(booking);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancelBooking(@PathVariable String id) {
        bookingService.cancelBooking(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}/permanent")
    public ApiResponse<Void> deleteBooking(@PathVariable String id) {
        bookingService.deleteBooking(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/check-in")
    public ApiResponse<Void> checkIn(@PathVariable String id) {
        bookingService.checkIn(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/check-out")
    public ApiResponse<Void> checkOut(@PathVariable String id) {
        bookingService.checkOut(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/rating")
    public ApiResponse<Void> submitRating(
            @PathVariable String id,
            @RequestParam Integer rating,
            @RequestParam(required = false) String feedback) {
        bookingService.submitRating(id, rating, feedback);
        return ApiResponse.success(null);
    }
}
