package com.example.oa_system_backend.module.leave.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LeaveQueryRequest {

    private String applicantId;

    private String departmentId;

    private String type;

    private String status;

    private List<String> statusList;

    private LocalDateTime startTimeStart;

    private LocalDateTime startTimeEnd;

    private LocalDateTime endTimeStart;

    private LocalDateTime endTimeEnd;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer year;

    private String keyword;

    private Integer page = 1;

    private Integer pageSize = 10;

    private String sortBy = "created_at";

    private String sortOrder = "DESC";
}
