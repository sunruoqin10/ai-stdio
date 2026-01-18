package com.example.oa_system_backend.module.leave.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@Component
public class AnnualQuotaCalculator {

    private static final BigDecimal BASE_QUOTA = new BigDecimal("5");
    private static final BigDecimal MAX_QUOTA = new BigDecimal("15");

    public BigDecimal calculateAnnualQuota(LocalDate hireDate, LocalDate calculationDate) {
        if (hireDate == null) {
            return BASE_QUOTA;
        }

        Period tenure = Period.between(hireDate, calculationDate);
        int years = tenure.getYears();

        if (years < 1) {
            return BASE_QUOTA;
        } else if (years < 10) {
            return BASE_QUOTA.add(new BigDecimal("5"));
        } else if (years < 20) {
            return BASE_QUOTA.add(new BigDecimal("10"));
        } else {
            return MAX_QUOTA;
        }
    }

    public BigDecimal calculateProRatedQuota(LocalDate hireDate, LocalDate startDate, LocalDate endDate) {
        if (hireDate == null) {
            return calculateAnnualQuota(null, startDate);
        }

        BigDecimal fullYearQuota = calculateAnnualQuota(hireDate, startDate);

        if (hireDate.isAfter(startDate)) {
            int totalDaysInYear = startDate.isLeapYear() ? 366 : 365;
            int daysWorked = (int) java.time.temporal.ChronoUnit.DAYS.between(hireDate, endDate.plusDays(1));
            return fullYearQuota.multiply(new BigDecimal(daysWorked))
                    .divide(new BigDecimal(totalDaysInYear), 1, java.math.RoundingMode.HALF_UP);
        }

        return fullYearQuota;
    }
}
