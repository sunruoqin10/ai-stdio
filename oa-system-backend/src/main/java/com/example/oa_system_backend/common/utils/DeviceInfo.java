package com.example.oa_system_backend.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeviceInfo {
    private String browser;
    private String os;
    private String deviceType;
}
