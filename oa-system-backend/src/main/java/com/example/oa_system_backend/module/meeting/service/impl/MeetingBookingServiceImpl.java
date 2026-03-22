package com.example.oa_system_backend.module.meeting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oa_system_backend.common.exception.BusinessException;
import com.example.oa_system_backend.common.utils.SecurityUtils;
import com.example.oa_system_backend.module.department.entity.Department;
import com.example.oa_system_backend.module.department.mapper.DepartmentMapper;
import com.example.oa_system_backend.module.employee.entity.Employee;
import com.example.oa_system_backend.module.employee.mapper.EmployeeMapper;
import com.example.oa_system_backend.module.meeting.dto.BookingForm;
import com.example.oa_system_backend.module.meeting.dto.BookingQueryRequest;
import com.example.oa_system_backend.module.meeting.dto.ConflictCheckRequest;
import com.example.oa_system_backend.module.meeting.entity.MeetingBooking;
import com.example.oa_system_backend.module.meeting.entity.MeetingRoom;
import com.example.oa_system_backend.module.meeting.enums.BookingStatus;
import com.example.oa_system_backend.module.meeting.enums.MeetingLevel;
import com.example.oa_system_backend.module.meeting.enums.MeetingRoomStatus;
import com.example.oa_system_backend.module.meeting.mapper.MeetingBookingMapper;
import com.example.oa_system_backend.module.meeting.mapper.MeetingAttendeeMapper;
import com.example.oa_system_backend.module.meeting.mapper.MeetingRoomMapper;
import com.example.oa_system_backend.module.meeting.service.MeetingBookingService;
import com.example.oa_system_backend.module.meeting.util.MeetingIdGenerator;
import com.example.oa_system_backend.module.meeting.vo.AttendeeVO;
import com.example.oa_system_backend.module.meeting.vo.BookingDetailVO;
import com.example.oa_system_backend.module.meeting.vo.BookingVO;
import com.example.oa_system_backend.module.meeting.vo.ConflictVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingBookingServiceImpl extends ServiceImpl<MeetingBookingMapper, MeetingBooking>
        implements MeetingBookingService {

    private final MeetingBookingMapper bookingMapper;
    private final MeetingAttendeeMapper attendeeMapper;
    private final MeetingRoomMapper roomMapper;
    private final MeetingIdGenerator meetingIdGenerator;
    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final ObjectMapper objectMapper;

    @Override
    public IPage<BookingVO> getBookingList(BookingQueryRequest query) {
        Page<MeetingBooking> pageObj = new Page<>(query.getPage(), query.getPageSize());
        IPage<MeetingBooking> result = bookingMapper.selectPageByCondition(pageObj, query);
        return result.convert(this::convertToVO);
    }

    @Override
    public BookingDetailVO getBookingDetail(String id) {
        MeetingBooking booking = getById(id);
        if (booking == null) {
            throw new BusinessException(5101, "预定不存在");
        }

        MeetingRoom room = roomMapper.selectById(booking.getRoomId());
        Employee booker = employeeMapper.selectById(booking.getBookerId());

        BookingDetailVO vo = new BookingDetailVO();
        vo.setId(booking.getId());
        vo.setTitle(booking.getTitle());
        vo.setRoomId(booking.getRoomId());
        vo.setRoomName(booking.getRoomName());
        vo.setRoomLocation(room != null ? room.getLocation() : null);
        vo.setRoomCapacity(room != null ? room.getCapacity() : null);
        vo.setStartTime(booking.getStartTime());
        vo.setEndTime(booking.getEndTime());
        vo.setDuration(calculateDuration(booking.getStartTime(), booking.getEndTime()));
        vo.setBookerId(booking.getBookerId());
        vo.setBookerName(booking.getBookerName());
        vo.setBookerPosition(booker != null ? booker.getPosition() : null);
        vo.setBookerPhone(booker != null ? booker.getPhone() : null);
        vo.setBookerEmail(booker != null ? booker.getEmail() : null);
        vo.setDepartmentId(booking.getDepartmentId());
        vo.setDepartmentName(booking.getDepartmentName());
        vo.setParticipantCount(booking.getParticipantCount());
        vo.setAttendees(parseAttendees(booking.getParticipantIds()));
        vo.setAgenda(booking.getAgenda());
        vo.setEquipment(parseJsonArray(booking.getEquipment()));
        vo.setLevel(booking.getLevel());
        vo.setLevelName(MeetingLevel.fromCode(booking.getLevel()).getName());
        vo.setReminder(booking.getReminder());
        vo.setRecurringRule(booking.getRecurringRule());
        vo.setStatus(booking.getStatus());
        vo.setStatusName(BookingStatus.fromCode(booking.getStatus()).getName());
        vo.setActualStartTime(booking.getActualStartTime());
        vo.setActualEndTime(booking.getActualEndTime());
        vo.setCheckInUser(booking.getCheckInUser());
        vo.setCheckOutUser(booking.getCheckOutUser());
        vo.setRating(booking.getRating());
        vo.setFeedback(booking.getFeedback());
        vo.setApproverId(booking.getApproverId());
        vo.setApproverName(booking.getApproverName());
        vo.setApprovalOpinion(booking.getApprovalOpinion());
        vo.setApprovalTime(booking.getApprovalTime());
        vo.setRejectionReason(booking.getRejectionReason());
        vo.setVersion(booking.getVersion());
        vo.setCreatedAt(booking.getCreatedAt());
        vo.setUpdatedAt(booking.getUpdatedAt());

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MeetingBooking createBooking(BookingForm form) {
        MeetingRoom room = roomMapper.selectById(form.getRoomId());
        if (room == null) {
            throw new BusinessException(5102, "会议室不存在");
        }

        if (!MeetingRoomStatus.AVAILABLE.getCode().equals(room.getStatus())) {
            throw new BusinessException(5103, "会议室当前不可用");
        }

        if (form.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(5104, "不能预定过去的时间");
        }

        if (form.getEndTime().isBefore(form.getStartTime()) || 
            form.getEndTime().isEqual(form.getStartTime())) {
            throw new BusinessException(5105, "结束时间必须晚于开始时间");
        }

        int duration = calculateDuration(form.getStartTime(), form.getEndTime());
        if (duration < 15) {
            throw new BusinessException(5106, "会议时长不能少于15分钟");
        }
        if (duration > 480) {
            throw new BusinessException(5107, "会议时长不能超过8小时");
        }

        Long conflictCount = bookingMapper.countTimeConflict(
                form.getRoomId(),
                form.getStartTime(),
                form.getEndTime(),
                null
        );
        if (conflictCount > 0) {
            throw new BusinessException(5108, "该时间段已被预定");
        }

        MeetingBooking booking = new MeetingBooking();
        booking.setId(meetingIdGenerator.generate());
        booking.setTitle(form.getTitle());
        booking.setRoomId(form.getRoomId());
        booking.setRoomName(room.getName());
        booking.setRoomLocation(room.getLocation());
        booking.setStartTime(form.getStartTime());
        booking.setEndTime(form.getEndTime());
        
        String currentUserId = SecurityUtils.getCurrentUserId();
        booking.setBookerId(currentUserId);
        
        Employee employee = employeeMapper.selectById(currentUserId);
        if (employee != null) {
            booking.setBookerName(employee.getName());
            booking.setDepartmentId(employee.getDepartmentId());
            
            Department department = departmentMapper.selectById(employee.getDepartmentId());
            if (department != null) {
                booking.setDepartmentName(department.getName());
            }
        } else {
            booking.setBookerName(SecurityUtils.getCurrentUserName());
        }
        booking.setParticipantCount(form.getParticipantCount());
        booking.setParticipantIds(toJsonString(form.getParticipantIds()));
        booking.setAgenda(form.getAgenda());
        booking.setEquipment(toJsonString(form.getEquipment()));
        booking.setLevel(form.getLevel() != null ? form.getLevel() : MeetingLevel.NORMAL.getCode());
        booking.setReminder(form.getReminder());
        booking.setRecurringRule(form.getRecurringRule());
        booking.setStatus(BookingStatus.PENDING.getCode());
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        save(booking);
        log.info("创建会议预定成功: {}", booking.getId());
        return booking;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MeetingBooking updateBooking(String id, BookingForm form) {
        MeetingBooking booking = getById(id);
        if (booking == null) {
            throw new BusinessException(5101, "预定不存在");
        }

        if (!BookingStatus.PENDING.getCode().equals(booking.getStatus())) {
            throw new BusinessException(5109, "只有待审批的预定才能修改");
        }

        if (form.getTitle() != null) {
            booking.setTitle(form.getTitle());
        }
        if (form.getStartTime() != null && form.getEndTime() != null) {
            booking.setStartTime(form.getStartTime());
            booking.setEndTime(form.getEndTime());
        }
        if (form.getAgenda() != null) {
            booking.setAgenda(form.getAgenda());
        }
        if (form.getParticipantCount() != null) {
            booking.setParticipantCount(form.getParticipantCount());
        }
        if (form.getEquipment() != null) {
            booking.setEquipment(toJsonString(form.getEquipment()));
        }
        if (form.getLevel() != null) {
            booking.setLevel(form.getLevel());
        }
        if (form.getReminder() != null) {
            booking.setReminder(form.getReminder());
        }

        booking.setUpdatedAt(LocalDateTime.now());
        updateById(booking);
        log.info("更新会议预定成功: {}", id);
        return booking;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(String id) {
        MeetingBooking booking = getById(id);
        if (booking == null) {
            throw new BusinessException(5101, "预定不存在");
        }

        String status = booking.getStatus();
        if (!BookingStatus.PENDING.getCode().equals(status) && 
            !BookingStatus.APPROVED.getCode().equals(status)) {
            throw new BusinessException(5110, "只有待审批或已通过的预定才能取消");
        }

        booking.setStatus(BookingStatus.CANCELLED.getCode());
        booking.setUpdatedAt(LocalDateTime.now());
        updateById(booking);
        log.info("取消会议预定成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBooking(String id) {
        MeetingBooking booking = getById(id);
        if (booking == null) {
            throw new BusinessException(5101, "预定不存在");
        }

        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!booking.getBookerId().equals(currentUserId) && !"ADMIN".equals(currentUserId)) {
            throw new BusinessException(5103, "只有预定者或管理员可以删除");
        }

        attendeeMapper.deleteByBookingId(id);
        removeById(id);
        log.info("删除会议预定成功: {}", id);
    }

    @Override
    public ConflictVO checkConflicts(ConflictCheckRequest request) {
        Long count = bookingMapper.countTimeConflict(
                request.getRoomId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getExcludeBookingId()
        );

        ConflictVO vo = new ConflictVO();
        vo.setHasConflict(count > 0);
        vo.setConflicts(new ArrayList<>());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(String id) {
        MeetingBooking booking = getById(id);
        if (booking == null) {
            throw new BusinessException(5101, "预定不存在");
        }

        if (!BookingStatus.APPROVED.getCode().equals(booking.getStatus())) {
            throw new BusinessException(5111, "只有已通过的预定才能签到");
        }

        if (booking.getActualStartTime() != null) {
            throw new BusinessException(5112, "已经签到过了");
        }

        booking.setActualStartTime(LocalDateTime.now());
        booking.setCheckInUser(booking.getBookerId());
        booking.setStatus(BookingStatus.CHECKED_IN.getCode());
        booking.setUpdatedAt(LocalDateTime.now());
        updateById(booking);
        log.info("会议签到成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOut(String id) {
        MeetingBooking booking = getById(id);
        if (booking == null) {
            throw new BusinessException(5101, "预定不存在");
        }

        if (booking.getActualStartTime() == null) {
            throw new BusinessException(5113, "请先签到");
        }

        if (booking.getActualEndTime() != null) {
            throw new BusinessException(5114, "已经签退过了");
        }

        booking.setActualEndTime(LocalDateTime.now());
        booking.setCheckOutUser(booking.getBookerId());
        booking.setStatus(BookingStatus.CHECKED_OUT.getCode());
        booking.setUpdatedAt(LocalDateTime.now());
        updateById(booking);
        log.info("会议签退成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRating(String id, Integer rating, String feedback) {
        MeetingBooking booking = getById(id);
        if (booking == null) {
            throw new BusinessException(5101, "预定不存在");
        }

        if (booking.getActualEndTime() == null) {
            throw new BusinessException(5115, "会议结束后才能评价");
        }

        booking.setRating(rating);
        booking.setFeedback(feedback);
        booking.setStatus(BookingStatus.COMPLETED.getCode());
        booking.setUpdatedAt(LocalDateTime.now());
        updateById(booking);
        log.info("会议评价提交成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveBooking(String id, String status, String opinion) {
        MeetingBooking booking = getById(id);
        if (booking == null) {
            throw new BusinessException(5101, "预定不存在");
        }

        if (!BookingStatus.PENDING.getCode().equals(booking.getStatus())) {
            throw new BusinessException(5116, "只有待审批的预定才能审批");
        }

        LocalDateTime now = LocalDateTime.now();
        if ("approved".equals(status)) {
            booking.setStatus(BookingStatus.APPROVED.getCode());
        } else {
            booking.setStatus(BookingStatus.REJECTED.getCode());
            booking.setRejectionReason(opinion);
        }
        booking.setApprovalOpinion(opinion);
        booking.setApprovalTime(now);
        booking.setUpdatedAt(now);

        updateById(booking);
        log.info("审批会议预定完成: {}, 结果: {}", id, status);
    }

    @Override
    public List<MeetingBooking> getPendingApprovals() {
        QueryWrapper<MeetingBooking> wrapper = new QueryWrapper<>();
        wrapper.eq("status", BookingStatus.PENDING.getCode());
        wrapper.orderByDesc("created_at");
        return list(wrapper);
    }

    @Override
    public List<MeetingBooking> getApprovedByDateRange(String startDate, String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        return bookingMapper.selectApprovedByDateRange(start, end);
    }

    private int calculateDuration(LocalDateTime start, LocalDateTime end) {
        return (int) java.time.Duration.between(start, end).toMinutes();
    }

    private BookingVO convertToVO(MeetingBooking booking) {
        BookingVO vo = new BookingVO();
        vo.setId(booking.getId());
        vo.setTitle(booking.getTitle());
        vo.setRoomId(booking.getRoomId());
        vo.setRoomName(booking.getRoomName());
        vo.setRoomLocation(booking.getRoomLocation());
        vo.setStartTime(booking.getStartTime());
        vo.setEndTime(booking.getEndTime());
        vo.setDuration(calculateDuration(booking.getStartTime(), booking.getEndTime()));
        vo.setBookerId(booking.getBookerId());
        vo.setBookerName(booking.getBookerName());
        vo.setBookerPosition(booking.getBookerPosition());
        vo.setDepartmentName(booking.getDepartmentName());
        vo.setParticipantCount(booking.getParticipantCount());
        vo.setAttendees(parseAttendees(booking.getParticipantIds()));
        vo.setAgenda(booking.getAgenda());
        vo.setEquipment(parseJsonArray(booking.getEquipment()));
        vo.setLevel(booking.getLevel());
        vo.setLevelName(MeetingLevel.fromCode(booking.getLevel()).getName());
        vo.setReminder(booking.getReminder());
        vo.setStatus(booking.getStatus());
        vo.setStatusName(BookingStatus.fromCode(booking.getStatus()).getName());
        vo.setCreatedAt(booking.getCreatedAt());
        vo.setUpdatedAt(booking.getUpdatedAt());
        return vo;
    }

    private List<AttendeeVO> parseAttendees(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            List<String> userIds = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            List<AttendeeVO> attendees = new ArrayList<>();

            if (!userIds.isEmpty()) {
                List<Employee> employees = employeeMapper.selectBatchIds(userIds);

                for (String userId : userIds) {
                    AttendeeVO attendee = new AttendeeVO();
                    attendee.setUserId(userId);

                    employees.stream()
                            .filter(emp -> emp.getId().equals(userId))
                            .findFirst()
                            .ifPresent(emp -> {
                                attendee.setUserName(emp.getName());
                                String departmentName = null;
                                if (emp.getDepartmentId() != null) {
                                    Department dept = departmentMapper.selectById(emp.getDepartmentId());
                                    if (dept != null) {
                                        departmentName = dept.getName();
                                    }
                                }
                                attendee.setDepartmentName(departmentName);
                            });

                    attendees.add(attendee);
                }
            }

            return attendees;
        } catch (JsonProcessingException e) {
            log.error("解析参会人JSON失败: {}", json, e);
            return new ArrayList<>();
        }
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
