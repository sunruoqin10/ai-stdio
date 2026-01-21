package com.example.oa_system_backend.module.expense.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ExpenseIdGenerator {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicInteger counter = new AtomicInteger(1);
    private String lastDate = "";

    public synchronized String generate() {
        String currentDate = LocalDateTime.now().format(formatter);

        if (!currentDate.equals(lastDate)) {
            lastDate = currentDate;
            counter.set(1);
        }

        int sequence = counter.getAndIncrement();
        if (sequence > 9999) {
            throw new RuntimeException("当日报销单号已用完");
        }

        return String.format("EXP%s%04d", currentDate, sequence);
    }
}
