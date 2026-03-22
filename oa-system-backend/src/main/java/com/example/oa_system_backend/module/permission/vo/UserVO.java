package com.example.oa_system_backend.module.permission.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private String id;
    private String username;
    private String email;
    private String mobile;
    private String status;
    private LocalDateTime createdAt;
}