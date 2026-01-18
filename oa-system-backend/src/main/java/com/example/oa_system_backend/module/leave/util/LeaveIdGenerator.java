package com.example.oa_system_backend.module.leave.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class LeaveIdGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    public String generate() {
        String datePart = LocalDateTime.now().format(DATE_FORMATTER);
        long sequence = SEQUENCE.incrementAndGet() % 10000;
        return "LR" + datePart + String.format("%04d", sequence);
    }

    public String generateWithPrefix(String prefix) {
        String datePart = LocalDateTime.now().format(DATE_FORMATTER);
        long sequence = SEQUENCE.incrementAndGet() % 10000;
        return prefix + datePart + String.format("%04d", sequence);
    }
}
