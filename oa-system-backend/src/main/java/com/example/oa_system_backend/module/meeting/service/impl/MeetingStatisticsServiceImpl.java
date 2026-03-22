package com.example.oa_system_backend.module.meeting.service.impl;

import com.example.oa_system_backend.module.meeting.entity.MeetingBooking;
import com.example.oa_system_backend.module.meeting.mapper.MeetingBookingMapper;
import com.example.oa_system_backend.module.meeting.service.MeetingStatisticsService;
import com.example.oa_system_backend.module.meeting.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingStatisticsServiceImpl implements MeetingStatisticsService {

    private final MeetingBookingMapper bookingMapper;

    @Override
    public List<RoomUsageStatsVO> getRoomUsageStats(String startDate, String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");

        List<MeetingBooking> bookings = bookingMapper.selectRoomUsageStats(start, end);

        Map<String, List<MeetingBooking>> byRoom = bookings.stream()
                .collect(Collectors.groupingBy(MeetingBooking::getRoomId));

        List<RoomUsageStatsVO> result = new ArrayList<>();
        for (Map.Entry<String, List<MeetingBooking>> entry : byRoom.entrySet()) {
            RoomUsageStatsVO vo = new RoomUsageStatsVO();
            vo.setRoomId(entry.getKey());
            vo.setRoomName(entry.getValue().get(0).getRoomName());
            vo.setBookingCount(entry.getValue().size());
            int totalMinutes = entry.getValue().stream()
                    .mapToInt(b -> (int) java.time.Duration.between(b.getStartTime(), b.getEndTime()).toMinutes())
                    .sum();
            vo.setTotalHours(totalMinutes / 60);
            result.add(vo);
        }

        return result;
    }

    @Override
    public List<DepartmentUsageStatsVO> getDepartmentUsageStats(String startDate, String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");

        List<MeetingBooking> bookings = bookingMapper.selectDepartmentStats(start, end);

        Map<String, List<MeetingBooking>> byDept = bookings.stream()
                .filter(b -> b.getDepartmentId() != null)
                .collect(Collectors.groupingBy(MeetingBooking::getDepartmentId));

        List<DepartmentUsageStatsVO> result = new ArrayList<>();
        for (Map.Entry<String, List<MeetingBooking>> entry : byDept.entrySet()) {
            DepartmentUsageStatsVO vo = new DepartmentUsageStatsVO();
            vo.setDepartmentId(entry.getKey());
            vo.setDepartmentName(entry.getValue().get(0).getDepartmentName());
            vo.setBookingCount(entry.getValue().size());
            int totalMinutes = entry.getValue().stream()
                    .mapToInt(b -> (int) java.time.Duration.between(b.getStartTime(), b.getEndTime()).toMinutes())
                    .sum();
            vo.setTotalHours(totalMinutes / 60);
            result.add(vo);
        }

        return result;
    }

    @Override
    public List<TimeSlotStatsVO> getTimeSlotStats(String startDate, String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");

        List<MeetingBooking> bookings = bookingMapper.selectTimeSlotStats(start, end);

        Map<String, List<MeetingBooking>> bySlot = new HashMap<>();
        bySlot.put("morning", new ArrayList<>());
        bySlot.put("afternoon", new ArrayList<>());
        bySlot.put("evening", new ArrayList<>());

        for (MeetingBooking booking : bookings) {
            int hour = booking.getStartTime().getHour();
            if (hour >= 8 && hour < 12) {
                bySlot.get("morning").add(booking);
            } else if (hour >= 12 && hour < 18) {
                bySlot.get("afternoon").add(booking);
            } else {
                bySlot.get("evening").add(booking);
            }
        }

        List<TimeSlotStatsVO> result = new ArrayList<>();
        for (Map.Entry<String, List<MeetingBooking>> entry : bySlot.entrySet()) {
            TimeSlotStatsVO vo = new TimeSlotStatsVO();
            vo.setTimeSlot(entry.getKey());
            vo.setMorningCount(entry.getKey().equals("morning") ? entry.getValue().size() : 0);
            vo.setAfternoonCount(entry.getKey().equals("afternoon") ? entry.getValue().size() : 0);
            vo.setEveningCount(entry.getKey().equals("evening") ? entry.getValue().size() : 0);
            result.add(vo);
        }

        return result;
    }

    @Override
    public List<MonthlyStatsVO> getMonthlyStats(Integer year) {
        List<MonthlyStatsVO> result = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            List<MeetingBooking> bookings = bookingMapper.selectMonthlyStats(year, month);

            MonthlyStatsVO vo = new MonthlyStatsVO();
            vo.setYear(year);
            vo.setMonth(month);
            vo.setBookingCount(bookings.size());
            int totalMinutes = bookings.stream()
                    .mapToInt(b -> (int) java.time.Duration.between(b.getStartTime(), b.getEndTime()).toMinutes())
                    .sum();
            vo.setTotalHours(totalMinutes / 60);
            vo.setGrowthRate(0.0);
            result.add(vo);
        }

        return result;
    }
}
