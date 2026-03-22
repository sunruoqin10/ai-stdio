package com.example.oa_system_backend.module.meeting.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoomForm {
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
}
