package com.example.oa_system_backend.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.auth.entity.AuthUserSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthUserSessionMapper extends BaseMapper<AuthUserSession> {

    AuthUserSession selectActiveSessionByRefreshToken(@Param("refreshToken") String refreshToken);

    IPage<AuthUserSession> selectActiveSessionsByUserId(Page<AuthUserSession> page, @Param("userId") String userId);
}
