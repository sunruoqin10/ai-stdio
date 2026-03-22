package com.example.oa_system_backend.module.meeting.dto;

import lombok.Data;

@Data
public class BookingQueryRequest {
    private Integer page = 1;
    private Integer pageSize = 10;
    private String roomId;
    private String bookerId;
    private String status;
    private String keyword;
    private String startDate;
    private String endDate;
}
