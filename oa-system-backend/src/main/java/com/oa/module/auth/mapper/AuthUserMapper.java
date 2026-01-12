package com.oa.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.module.auth.entity.AuthUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthUserMapper extends BaseMapper<AuthUser> {
    @Select("SELECT * FROM auth_user WHERE username = #{account} OR email = #{account} OR mobile = #{account} LIMIT 1")
    AuthUser selectByUsernameOrEmailOrMobile(String account);
}
