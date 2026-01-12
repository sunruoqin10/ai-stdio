package com.oa.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.module.auth.entity.AuthLoginLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthLoginLogMapper extends BaseMapper<AuthLoginLog> {
}
