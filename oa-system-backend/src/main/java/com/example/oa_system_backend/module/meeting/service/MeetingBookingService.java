package com.example.oa_system_backend.module.meeting.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.oa_system_backend.module.meeting.dto.BookingForm;
import com.example.oa_system_backend.module.meeting.dto.BookingQueryRequest;
import com.example.oa_system_backend.module.meeting.dto.ConflictCheckRequest;
import com.example.oa_system_backend.module.meeting.entity.MeetingBooking;
import com.example.oa_system_backend.module.meeting.vo.BookingDetailVO;
import com.example.oa_system_backend.module.meeting.vo.BookingVO;
import com.example.oa_system_backend.module.meeting.vo.ConflictVO;

import java.util.List;

public interface MeetingBookingService {

    IPage<BookingVO> getBookingList(BookingQueryRequest query);

    BookingDetailVO getBookingDetail(String id);

    MeetingBooking createBooking(BookingForm form);

    MeetingBooking updateBooking(String id, BookingForm form);

    void cancelBooking(String id);

    void deleteBooking(String id);

    ConflictVO checkConflicts(ConflictCheckRequest request);

    void checkIn(String id);

    void checkOut(String id);

    void submitRating(String id, Integer rating, String feedback);

    void approveBooking(String id, String status, String opinion);

    List<MeetingBooking> getPendingApprovals();

    List<MeetingBooking> getApprovedByDateRange(String startDate, String endDate);
}
