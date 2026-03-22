package com.example.oa_system_backend.module.meeting.vo;

import lombok.Data;

import java.util.List;

@Data
public class ConflictVO {
    private Boolean hasConflict;
    private List<BookingVO> conflicts;
}
