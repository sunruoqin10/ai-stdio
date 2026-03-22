package com.example.oa_system_backend.module.meeting.dto;

import lombok.Data;

@Data
public class RatingForm {
    private String bookingId;
    private Integer rating;
    private String feedback;
}
