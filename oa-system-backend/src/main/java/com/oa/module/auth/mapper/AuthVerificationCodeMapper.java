package com.oa.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.module.auth.entity.AuthVerificationCode;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthVerificationCodeMapper extends BaseMapper<AuthVerificationCode> {
}
