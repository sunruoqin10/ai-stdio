package com.example.oa_system_backend.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.auth.entity.AuthLoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface AuthLoginLogMapper extends BaseMapper<AuthLoginLog> {

    IPage<AuthLoginLog> selectLogsByUserIdAndDateRange(Page<AuthLoginLog> page,
                                                        @Param("userId") String userId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    IPage<AuthLoginLog> selectLogsByUsernameAndDateRange(Page<AuthLoginLog> page,
                                                         @Param("username") String username,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);
}
