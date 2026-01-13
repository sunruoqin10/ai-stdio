package com.example.oa_system_backend.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oa_system_backend.module.auth.entity.AuthVerificationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface AuthVerificationCodeMapper extends BaseMapper<AuthVerificationCode> {

    int countRecentCodesByAccount(@Param("account") String account,
                                   @Param("minutes") int minutes);

    AuthVerificationCode selectValidCode(@Param("account") String account,
                                         @Param("code") String code,
                                         @Param("scene") String scene);
}
