package com.example.oa_system_backend.module.meeting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_meeting_room")
public class MeetingRoom {

    @TableId(type = IdType.INPUT)
    private String id;

    private String name;

    private String location;

    private Integer capacity;

    private Integer floor;

    private Integer area;

    private String facilities;

    private String description;

    private String images;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
