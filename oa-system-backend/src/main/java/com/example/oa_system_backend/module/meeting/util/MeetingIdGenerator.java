package com.example.oa_system_backend.module.meeting.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MeetingIdGenerator {
    private static final AtomicInteger counter = new AtomicInteger(1);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public String generate() {
        return "MTG" + LocalDateTime.now().format(formatter) + String.format("%04d", counter.getAndIncrement());
    }
}
