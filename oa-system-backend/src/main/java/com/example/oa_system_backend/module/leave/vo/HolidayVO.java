package com.example.oa_system_backend.module.leave.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HolidayVO {

    private Long id;

    private LocalDate date;

    private String name;

    private String type;

    private String typeLabel;

    private String typeName;

    private Integer year;

    private Integer isWorkday;

    private String isWorkdayName;
}
