package com.example.oa_system_backend.module.meeting.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingDetailVO {
    private String id;
    private String title;
    private String roomId;
    private String roomName;
    private String roomLocation;
    private Integer roomCapacity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private String bookerId;
    private String bookerName;
    private String bookerPosition;
    private String bookerPhone;
    private String bookerEmail;
    private String departmentId;
    private String departmentName;
    private Integer participantCount;
    private List<AttendeeVO> attendees;
    private String agenda;
    private List<String> equipment;
    private String level;
    private String levelName;
    private String reminder;
    private String recurringRule;
    private String status;
    private String statusName;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private String checkInUser;
    private String checkOutUser;
    private Integer rating;
    private String feedback;
    private String approverId;
    private String approverName;
    private String approvalOpinion;
    private LocalDateTime approvalTime;
    private String rejectionReason;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
