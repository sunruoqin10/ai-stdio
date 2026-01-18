package com.example.oa_system_backend.module.leave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oa_system_backend.module.leave.entity.Holiday;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

public interface HolidayMapper extends BaseMapper<Holiday> {

    List<Holiday> selectByCondition(@Param("year") Integer year,
                                    @Param("type") String type,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate,
                                    @Param("keyword") String keyword);

    @Select("SELECT * FROM approval_holiday WHERE date = #{date}")
    Holiday selectByDate(@Param("date") LocalDate date);

    @Select("SELECT * FROM approval_holiday WHERE date >= #{startDate} AND date <= #{endDate} ORDER BY date")
    List<Holiday> selectByDateRange(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    @Select("SELECT * FROM approval_holiday WHERE year = #{year} ORDER BY date")
    List<Holiday> selectByYear(@Param("year") Integer year);

    @Select("SELECT COUNT(*) FROM approval_holiday WHERE date >= #{startDate} AND date <= #{endDate}")
    Long countByDateRange(@Param("startDate") LocalDate startDate,
                         @Param("endDate") LocalDate endDate);
}
