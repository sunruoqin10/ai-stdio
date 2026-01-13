package com.example.oa_system_backend.module.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oa_system_backend.module.employee.entity.EmployeeOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工操作日志Mapper接口
 */
@Mapper
public interface EmployeeOperationLogMapper extends BaseMapper<EmployeeOperationLog> {
    // 使用MyBatis Plus提供的CRUD方法
}
