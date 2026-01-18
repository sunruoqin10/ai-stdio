package com.example.oa_system_backend.module.leave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oa_system_backend.module.leave.dto.LeaveQueryRequest;
import com.example.oa_system_backend.module.leave.entity.LeaveRequest;
import com.example.oa_system_backend.module.leave.vo.LeaveRequestVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface LeaveRequestMapper extends BaseMapper<LeaveRequest> {

    IPage<LeaveRequestVO> selectPageByCondition(Page<LeaveRequest> page, @Param("query") LeaveQueryRequest query);

    Long countTimeConflict(@Param("applicantId") String applicantId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime,
                        @Param("excludeId") String excludeId);

    List<Map<String, Object>> selectStatisticsByType(@Param("applicantId") String applicantId,
                                                     @Param("year") Integer year);

    List<Map<String, Object>> selectStatisticsByStatus(@Param("applicantId") String applicantId,
                                                      @Param("year") Integer year);

    List<Map<String, Object>> selectStatisticsByMonth(@Param("applicantId") String applicantId,
                                                     @Param("year") Integer year);
}
