package com.example.oa_system_backend.module.meeting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oa_system_backend.common.exception.BusinessException;
import com.example.oa_system_backend.module.meeting.dto.RoomForm;
import com.example.oa_system_backend.module.meeting.entity.MeetingBooking;
import com.example.oa_system_backend.module.meeting.entity.MeetingRoom;
import com.example.oa_system_backend.module.meeting.enums.BookingStatus;
import com.example.oa_system_backend.module.meeting.enums.MeetingLevel;
import com.example.oa_system_backend.module.meeting.enums.MeetingRoomStatus;
import com.example.oa_system_backend.module.meeting.mapper.MeetingBookingMapper;
import com.example.oa_system_backend.module.meeting.mapper.MeetingRoomMapper;
import com.example.oa_system_backend.module.meeting.service.MeetingRoomService;
import com.example.oa_system_backend.module.meeting.util.MeetingRoomIdGenerator;
import com.example.oa_system_backend.module.meeting.vo.AvailabilityVO;
import com.example.oa_system_backend.module.meeting.vo.BookingVO;
import com.example.oa_system_backend.module.meeting.vo.RoomDetailVO;
import com.example.oa_system_backend.module.meeting.vo.RoomVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingRoomServiceImpl extends ServiceImpl<MeetingRoomMapper, MeetingRoom>
        implements MeetingRoomService {

    private final MeetingRoomMapper meetingRoomMapper;
    private final MeetingBookingMapper meetingBookingMapper;
    private final MeetingRoomIdGenerator meetingRoomIdGenerator;
    private final ObjectMapper objectMapper;

    @Override
    public IPage<RoomVO> getRoomList(String status, int page, int size) {
        Page<MeetingRoom> pageObj = new Page<>(page, size);
        IPage<MeetingRoom> result = meetingRoomMapper.selectPageByCondition(pageObj, status);
        return result.convert(this::convertToVO);
    }

    @Override
    public RoomDetailVO getRoomDetail(String id) {
        MeetingRoom room = getById(id);
        if (room == null) {
            throw new BusinessException(5001, "会议室不存在");
        }

        RoomDetailVO vo = new RoomDetailVO();
        vo.setId(room.getId());
        vo.setName(room.getName());
        vo.setLocation(room.getLocation());
        vo.setCapacity(room.getCapacity());
        vo.setFloor(room.getFloor());
        vo.setArea(room.getArea());
        vo.setEquipment(parseJsonArray(room.getFacilities()));
        vo.setDescription(room.getDescription());
        vo.setImages(room.getImages());
        vo.setStatus(room.getStatus());
        vo.setStatusName(MeetingRoomStatus.fromCode(room.getStatus()).getName());
        vo.setCreatedAt(room.getCreatedAt());
        vo.setUpdatedAt(room.getUpdatedAt());

        List<MeetingBooking> upcomingBookings = meetingBookingMapper.selectByRoomAndDateRange(
                id,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7)
        );
        vo.setUpcomingBookings(upcomingBookings.stream().limit(5).map(this::convertToSimpleVO).collect(Collectors.toList()));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MeetingRoom createRoom(RoomForm form) {
        MeetingRoom room = new MeetingRoom();
        room.setId(meetingRoomIdGenerator.generate());
        room.setName(form.getName());
        room.setLocation(form.getLocation());
        room.setCapacity(form.getCapacity());
        room.setFloor(form.getFloor());
        room.setArea(form.getArea());
        room.setFacilities(toJsonString(form.getEquipment()));
        room.setDescription(form.getDescription());
        room.setImages(form.getImages());
        room.setStatus(form.getStatus() != null ? form.getStatus() : MeetingRoomStatus.AVAILABLE.getCode());
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());

        save(room);
        log.info("创建会议室成功: {}", room.getId());
        return room;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MeetingRoom updateRoom(String id, RoomForm form) {
        MeetingRoom room = getById(id);
        if (room == null) {
            throw new BusinessException(5001, "会议室不存在");
        }

        if (form.getName() != null) {
            room.setName(form.getName());
        }
        if (form.getLocation() != null) {
            room.setLocation(form.getLocation());
        }
        if (form.getCapacity() != null) {
            room.setCapacity(form.getCapacity());
        }
        if (form.getFloor() != null) {
            room.setFloor(form.getFloor());
        }
        if (form.getArea() != null) {
            room.setArea(form.getArea());
        }
        if (form.getEquipment() != null) {
            room.setFacilities(toJsonString(form.getEquipment()));
        }
        if (form.getDescription() != null) {
            room.setDescription(form.getDescription());
        }
        if (form.getImages() != null) {
            room.setImages(form.getImages());
        }
        if (form.getStatus() != null) {
            room.setStatus(form.getStatus());
        }
        room.setUpdatedAt(LocalDateTime.now());

        updateById(room);
        log.info("更新会议室成功: {}", id);
        return room;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(String id) {
        MeetingRoom room = getById(id);
        if (room == null) {
            throw new BusinessException(5001, "会议室不存在");
        }

        Long activeBookings = meetingRoomMapper.countActiveBookings(id);
        if (activeBookings > 0) {
            throw new BusinessException(5002, "该会议室有待处理的预定，无法删除");
        }

        room.setStatus(MeetingRoomStatus.DISABLED.getCode());
        room.setUpdatedAt(LocalDateTime.now());
        updateById(room);

        log.info("删除会议室成功: {}", id);
    }

    @Override
    public List<AvailabilityVO> getRoomAvailability(String roomId, String date) {
        LocalDate targetDate = LocalDate.parse(date);
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        List<MeetingBooking> bookings = meetingBookingMapper.selectByRoomAndDateRange(roomId, startOfDay, endOfDay);

        List<AvailabilityVO> result = new ArrayList<>();
        for (int hour = 8; hour < 20; hour++) {
            LocalDateTime slotStart = targetDate.atTime(hour, 0);
            LocalDateTime slotEnd = targetDate.atTime(hour + 1, 0);

            AvailabilityVO vo = new AvailabilityVO();
            vo.setDate(date);
            vo.setStartTime(String.format("%02d:00", hour));
            vo.setEndTime(String.format("%02d:00", hour + 1));

            boolean occupied = bookings.stream().anyMatch(b -> {
                LocalDateTime bStart = b.getStartTime();
                LocalDateTime bEnd = b.getEndTime();
                return slotStart.isBefore(bEnd) && slotEnd.isAfter(bStart);
            });

            vo.setAvailable(!occupied);
            result.add(vo);
        }

        return result;
    }

    private RoomVO convertToVO(MeetingRoom room) {
        RoomVO vo = new RoomVO();
        vo.setId(room.getId());
        vo.setName(room.getName());
        vo.setLocation(room.getLocation());
        vo.setCapacity(room.getCapacity());
        vo.setFloor(room.getFloor());
        vo.setArea(room.getArea());
        vo.setEquipment(parseJsonArray(room.getFacilities()));
        vo.setDescription(room.getDescription());
        vo.setImages(room.getImages());
        vo.setStatus(room.getStatus());
        vo.setStatusName(MeetingRoomStatus.fromCode(room.getStatus()).getName());
        vo.setCreatedAt(room.getCreatedAt());
        vo.setUpdatedAt(room.getUpdatedAt());

        Long bookingCount = meetingRoomMapper.countActiveBookings(room.getId());
        vo.setBookingCount7Days(bookingCount.intValue());

        return vo;
    }

    private BookingVO convertToSimpleVO(MeetingBooking booking) {
        BookingVO vo = new BookingVO();
        vo.setId(booking.getId());
        vo.setTitle(booking.getTitle());
        vo.setRoomId(booking.getRoomId());
        vo.setRoomName(booking.getRoomName());
        vo.setRoomLocation(booking.getRoomLocation());
        vo.setStartTime(booking.getStartTime());
        vo.setEndTime(booking.getEndTime());
        vo.setDuration((int) java.time.Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes());
        vo.setBookerId(booking.getBookerId());
        vo.setBookerName(booking.getBookerName());
        vo.setBookerPosition(booking.getBookerPosition());
        vo.setDepartmentName(booking.getDepartmentName());
        vo.setParticipantCount(booking.getParticipantCount());
        vo.setAgenda(booking.getAgenda());
        vo.setEquipment(parseJsonArray(booking.getEquipment()));
        vo.setLevel(booking.getLevel());
        vo.setLevelName(MeetingLevel.fromCode(booking.getLevel()) != null ? MeetingLevel.fromCode(booking.getLevel()).getName() : null);
        vo.setReminder(booking.getReminder());
        vo.setStatus(booking.getStatus());
        vo.setStatusName(BookingStatus.fromCode(booking.getStatus()) != null ? BookingStatus.fromCode(booking.getStatus()).getName() : null);
        vo.setCreatedAt(booking.getCreatedAt());
        vo.setUpdatedAt(booking.getUpdatedAt());
        return vo;
    }

    private List<String> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("解析JSON数组失败: {}", json, e);
            return new ArrayList<>();
        }
    }

    private String toJsonString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("序列化JSON数组失败", e);
            return "[]";
        }
    }
}
