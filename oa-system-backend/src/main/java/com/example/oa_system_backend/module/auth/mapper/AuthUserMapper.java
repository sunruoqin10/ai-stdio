package com.example.oa_system_backend.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oa_system_backend.module.auth.entity.AuthUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthUserMapper extends BaseMapper<AuthUser> {

    AuthUser selectByUsernameOrEmailOrMobile(@Param("username") String username,
                                              @Param("email") String email,
                                              @Param("mobile") String mobile);

    int updateLoginStats(@Param("userId") String userId,
                         @Param("lastLoginTime") String lastLoginTime,
                         @Param("lastLoginIp") String lastLoginIp,
                         @Param("loginAttempts") Integer loginAttempts,
                         @Param("status") String status,
                         @Param("lockedUntil") String lockedUntil);
}
