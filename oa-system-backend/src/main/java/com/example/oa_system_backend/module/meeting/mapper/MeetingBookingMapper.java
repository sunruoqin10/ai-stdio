package com.example.oa_system_backend.module.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.meeting.dto.BookingQueryRequest;
import com.example.oa_system_backend.module.meeting.entity.MeetingBooking;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingBookingMapper extends BaseMapper<MeetingBooking> {

    IPage<MeetingBooking> selectPageByCondition(Page<MeetingBooking> page, @Param("query") BookingQueryRequest query);

    Long countTimeConflict(@Param("roomId") String roomId,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime,
                           @Param("excludeId") String excludeId);

    List<MeetingBooking> selectByRoomAndDateRange(@Param("roomId") String roomId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    List<MeetingBooking> selectApprovedByDateRange(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    Long countByStatus(@Param("status") String status);

    List<MeetingBooking> selectRoomUsageStats(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    List<MeetingBooking> selectDepartmentStats(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    List<MeetingBooking> selectTimeSlotStats(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    List<MeetingBooking> selectMonthlyStats(@Param("year") Integer year, @Param("month") Integer month);

    Long countActiveBookings(@Param("roomId") String roomId);
}
