package com.example.oa_system_backend.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.oa_system_backend.module.*.mapper")
public class MyBatisPlusConfig {
    
}
