package com.example.oa_system_backend.module.meeting.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MeetingRoomIdGenerator {
    private static final AtomicInteger counter = new AtomicInteger(1);

    public String generate() {
        return "ROOM" + String.format("%03d", counter.getAndIncrement());
    }
}
