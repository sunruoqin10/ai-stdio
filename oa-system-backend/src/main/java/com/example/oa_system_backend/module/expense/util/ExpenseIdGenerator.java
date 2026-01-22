package com.example.oa_system_backend.module.expense.util;

import com.example.oa_system_backend.module.expense.mapper.ExpenseMapper;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ExpenseIdGenerator {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicInteger counter = new AtomicInteger(1);
    private String lastDate = "";
    private final ExpenseMapper expenseMapper;

    public ExpenseIdGenerator(ExpenseMapper expenseMapper) {
        this.expenseMapper = expenseMapper;
    }

    @PostConstruct
    public void init() {
        // 初始化时，从数据库中获取最新的报销单ID，以避免ID冲突
        String currentDate = LocalDateTime.now().format(formatter);
        List<String> expenseIds = expenseMapper.selectIdsByDatePrefix("EXP" + currentDate);
        if (!expenseIds.isEmpty()) {
            // 找到最大的序号
            int maxSequence = 0;
            Pattern pattern = Pattern.compile("EXP" + currentDate + "(\\d{4})");
            for (String id : expenseIds) {
                Matcher matcher = pattern.matcher(id);
                if (matcher.matches()) {
                    int sequence = Integer.parseInt(matcher.group(1));
                    if (sequence > maxSequence) {
                        maxSequence = sequence;
                    }
                }
            }
            if (maxSequence > 0) {
                counter.set(maxSequence + 1);
                lastDate = currentDate;
            }
        }
    }

    public synchronized String generate() {
        String currentDate = LocalDateTime.now().format(formatter);

        if (!currentDate.equals(lastDate)) {
            lastDate = currentDate;
            // 从数据库中获取最新的报销单ID（包括已删除的），以避免ID冲突
            List<String> expenseIds = expenseMapper.selectIdsByDatePrefix("EXP" + currentDate);
            if (!expenseIds.isEmpty()) {
                // 找到最大的序号
                int maxSequence = 0;
                Pattern pattern = Pattern.compile("EXP" + currentDate + "(\\d{4})");
                for (String id : expenseIds) {
                    Matcher matcher = pattern.matcher(id);
                    if (matcher.matches()) {
                        int sequence = Integer.parseInt(matcher.group(1));
                        if (sequence > maxSequence) {
                            maxSequence = sequence;
                        }
                    }
                }
                counter.set(maxSequence + 1);
            } else {
                counter.set(1);
            }
        }

        int sequence = counter.getAndIncrement();
        if (sequence > 9999) {
            throw new RuntimeException("当日报销单号已用完");
        }

        String expenseId = String.format("EXP%s%04d", currentDate, sequence);
        
        // 检查ID是否已存在（包括已删除的记录），如果存在则递增序号
        List<String> existingIds = expenseMapper.selectIdsByDatePrefix("EXP" + currentDate);
        while (existingIds.contains(expenseId)) {
            sequence = counter.getAndIncrement();
            if (sequence > 9999) {
                throw new RuntimeException("当日报销单号已用完");
            }
            expenseId = String.format("EXP%s%04d", currentDate, sequence);
        }
        
        return expenseId;
    }
}
