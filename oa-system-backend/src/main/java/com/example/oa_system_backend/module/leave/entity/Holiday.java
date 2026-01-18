package com.example.oa_system_backend.module.leave.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("approval_holiday")
public class Holiday {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate date;

    private String name;

    private String type;

    private Integer year;

    @TableField("is_workday")
    private Integer isWorkday;

    @TableField(exist = false)
    private String typeName;

    @TableField(exist = false)
    private String isWorkdayName;
}
