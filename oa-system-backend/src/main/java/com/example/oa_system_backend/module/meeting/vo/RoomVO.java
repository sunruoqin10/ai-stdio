package com.example.oa_system_backend.module.meeting.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoomVO {
    private String id;
    private String name;
    private String location;
    private Integer capacity;
    private Integer floor;
    private Integer area;
    private List<String> equipment;
    private String description;
    private String images;
    private String status;
    private String statusName;
    private Integer bookingCount7Days;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
