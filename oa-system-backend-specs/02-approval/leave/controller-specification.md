# 请假管理模块 - Controller层规范

> **模块**: leave
> **版本**: v1.0.0
> **更新日期**: 2026-01-18

---

## 🎯 Controller层架构

### Controller列表

```
LeaveRequestController (请假申请控制器)
LeaveApprovalController (审批控制器)
LeaveBalanceController (年假余额控制器)
LeaveStatisticsController (统计控制器)
HolidayController (节假日控制器)
```

---

## 📦 核心Controller类

### 1. LeaveRequestController (请假申请控制器)

**文件路径**: `com/oa/system/module/leave/controller/LeaveRequestController.java`

```java
package com.oa.system.module.leave.controller;

import com.oa.system.common.dto.ApiResponse;
import com.oa.system.common.dto.PageResult;
import com.oa.system.module.leave.dto.request.LeaveCreateDTO;
import com.oa.system.module.leave.dto.request.LeaveQueryDTO;
import com.oa.system.module.leave.dto.request.LeaveUpdateDTO;
import com.oa.system.module.leave.service.LeaveRequestService;
import com.oa.system.module.leave.vo.LeaveDetailVO;
import com.oa.system.module.leave.vo.LeaveRequestVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Slf4j
@RestController
@RequestMapping("/api/leave/requests")
@Api(tags = "请假申请管理")
@RequiredArgsConstructor
@Validated
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @GetMapping
    @ApiOperation("获取请假申请列表")
    @PreAuthorize("hasAuthority('leave:request:view')")
    public ApiResponse<PageResult<LeaveRequestVO>> getLeaveRequests(
            @Valid LeaveQueryDTO queryDTO) {
        log.info("获取请假申请列表,查询条件: {}", queryDTO);
        PageResult<LeaveRequestVO> result = leaveRequestService.getLeaveRequests(queryDTO);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取请假申请详情")
    @PreAuthorize("hasAuthority('leave:request:view')")
    public ApiResponse<LeaveDetailVO> getLeaveDetail(
            @ApiParam("申请ID") @PathVariable @NotBlank String id) {
        log.info("获取请假申请详情,申请ID: {}", id);
        LeaveDetailVO detail = leaveRequestService.getLeaveDetail(id);
        return ApiResponse.success(detail);
    }

    @PostMapping
    @ApiOperation("创建请假申请")
    @PreAuthorize("hasAuthority('leave:request:create')")
    public ApiResponse<String> createLeaveRequest(
            @Valid @RequestBody LeaveCreateDTO createDTO) {
        log.info("创建请假申请,创建信息: {}", createDTO);
        String id = leaveRequestService.createLeaveRequest(createDTO);
        return ApiResponse.success("创建成功", id);
    }

    @PutMapping("/{id}")
    @ApiOperation("更新请假申请")
    @PreAuthorize("hasAuthority('leave:request:edit')")
    public ApiResponse<Void> updateLeaveRequest(
            @ApiParam("申请ID") @PathVariable @NotBlank String id,
            @Valid @RequestBody LeaveUpdateDTO updateDTO) {
        log.info("更新请假申请,申请ID: {}, 更新信息: {}", id, updateDTO);
        leaveRequestService.updateLeaveRequest(id, updateDTO);
        return ApiResponse.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除请假申请")
    @PreAuthorize("hasAuthority('leave:request:delete')")
    public ApiResponse<Void> deleteLeaveRequest(
            @ApiParam("申请ID") @PathVariable @NotBlank String id) {
        log.info("删除请假申请,申请ID: {}", id);
        leaveRequestService.deleteLeaveRequest(id);
        return ApiResponse.success("删除成功");
    }

    @PostMapping("/{id}/submit")
    @ApiOperation("提交请假申请")
    @PreAuthorize("hasAuthority('leave:request:submit')")
    public ApiResponse<Void> submitLeaveRequest(
            @ApiParam("申请ID") @PathVariable @NotBlank String id) {
        log.info("提交请假申请,申请ID: {}", id);
        leaveRequestService.submitLeaveRequest(id);
        return ApiResponse.success("提交成功");
    }

    @PostMapping("/{id}/cancel")
    @ApiOperation("撤销请假申请")
    @PreAuthorize("hasAuthority('leave:request:cancel')")
    public ApiResponse<Void> cancelLeaveRequest(
            @ApiParam("申请ID") @PathVariable @NotBlank String id) {
        log.info("撤销请假申请,申请ID: {}", id);
        leaveRequestService.cancelLeaveRequest(id);
        return ApiResponse.success("撤销成功");
    }

    @PostMapping("/{id}/resubmit")
    @ApiOperation("重新提交请假申请")
    @PreAuthorize("hasAuthority('leave:request:submit')")
    public ApiResponse<Void> resubmitLeaveRequest(
            @ApiParam("申请ID") @PathVariable @NotBlank String id) {
        log.info("重新提交请假申请,申请ID: {}", id);
        leaveRequestService.resubmitLeaveRequest(id);
        return ApiResponse.success("重新提交成功");
    }
}
```

---

### 2. LeaveApprovalController (审批控制器)

**文件路径**: `com/oa/system/module/leave/controller/LeaveApprovalController.java`

```java
package com.oa.system.module.leave.controller;

import com.oa.system.common.dto.ApiResponse;
import com.oa.system.common.dto.PageResult;
import com.oa.system.module.leave.dto.request.ApprovalDTO;
import com.oa.system.module.leave.dto.request.LeaveQueryDTO;
import com.oa.system.module.leave.service.LeaveApprovalService;
import com.oa.system.module.leave.vo.LeaveDetailVO;
import com.oa.system.module.leave.vo.LeaveRequestVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Slf4j
@RestController
@RequestMapping("/api/leave/approvals")
@Api(tags = "请假审批管理")
@RequiredArgsConstructor
@Validated
public class LeaveApprovalController {

    private final LeaveApprovalService leaveApprovalService;

    @GetMapping("/pending")
    @ApiOperation("获取待审批列表")
    @PreAuthorize("hasAuthority('leave:approval:approve')")
    public ApiResponse<PageResult<LeaveRequestVO>> getPendingApprovals(
            @Valid LeaveQueryDTO queryDTO) {
        log.info("获取待审批列表,查询条件: {}", queryDTO);
        PageResult<LeaveRequestVO> result = leaveApprovalService.getPendingApprovals(queryDTO);
        return ApiResponse.success(result);
    }

    @GetMapping("/history")
    @ApiOperation("获取已审批列表")
    @PreAuthorize("hasAuthority('leave:approval:approve')")
    public ApiResponse<PageResult<LeaveRequestVO>> getApprovedRequests(
            @Valid LeaveQueryDTO queryDTO) {
        log.info("获取已审批列表,查询条件: {}", queryDTO);
        PageResult<LeaveRequestVO> result = leaveApprovalService.getApprovedRequests(queryDTO);
        return ApiResponse.success(result);
    }

    @PostMapping("/{requestId}/approve")
    @ApiOperation("审批请假申请")
    @PreAuthorize("hasAuthority('leave:approval:approve')")
    public ApiResponse<LeaveDetailVO> approveRequest(
            @ApiParam("申请ID") @PathVariable @NotBlank String requestId,
            @Valid @RequestBody ApprovalDTO approvalDTO) {
        log.info("审批请假申请,申请ID: {}, 审批信息: {}", requestId, approvalDTO);
        LeaveDetailVO detail = leaveApprovalService.approveRequest(requestId, approvalDTO);
        return ApiResponse.success("审批成功", detail);
    }
}
```

---

### 3. LeaveBalanceController (年假余额控制器)

**文件路径**: `com/oa/system/module/leave/controller/LeaveBalanceController.java`

```java
package com.oa.system.module.leave.controller;

import com.oa.system.common.dto.ApiResponse;
import com.oa.system.common.dto.PageResult;
import com.oa.system.module.leave.dto.request.BalanceQueryDTO;
import com.oa.system.module.leave.dto.request.BalanceUpdateDTO;
import com.oa.system.module.leave.service.LeaveBalanceService;
import com.oa.system.module.leave.vo.LeaveBalanceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/leave/balance")
@Api(tags = "年假余额管理")
@RequiredArgsConstructor
@Validated
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    @GetMapping
    @ApiOperation("获取年假余额")
    @PreAuthorize("hasAuthority('leave:balance:view')")
    public ApiResponse<LeaveBalanceVO> getBalance(
            @ApiParam("员工ID") @RequestParam(required = false) String employeeId,
            @ApiParam("年份") @RequestParam(required = false) Integer year) {
        log.info("获取年假余额,员工ID: {}, 年份: {}", employeeId, year);

        if (year == null) {
            year = LocalDate.now().getYear();
        }

        if (employeeId == null) {
            employeeId = getCurrentUserId();
        }

        LeaveBalanceVO balance = leaveBalanceService.getBalance(employeeId, year);
        return ApiResponse.success(balance);
    }

    @GetMapping("/list")
    @ApiOperation("获取年假余额列表")
    @PreAuthorize("hasAuthority('leave:balance:view')")
    public ApiResponse<PageResult<LeaveBalanceVO>> getBalanceList(
            @Valid BalanceQueryDTO queryDTO) {
        log.info("获取年假余额列表,查询条件: {}", queryDTO);
        PageResult<LeaveBalanceVO> result = leaveBalanceService.getBalanceList(queryDTO);
        return ApiResponse.success(result);
    }

    @PutMapping("/quota")
    @ApiOperation("更新年假额度")
    @PreAuthorize("hasAuthority('leave:balance:manage')")
    public ApiResponse<Void> updateBalance(
            @Valid @RequestBody BalanceUpdateDTO updateDTO) {
        log.info("更新年假额度,更新信息: {}", updateDTO);
        leaveBalanceService.updateBalance(updateDTO);
        return ApiResponse.success("更新成功");
    }

    private String getCurrentUserId() {
        return "EMP000001";
    }
}
```

---

### 4. LeaveStatisticsController (统计控制器)

**文件路径**: `com/oa/system/module/leave/controller/LeaveStatisticsController.java`

```java
package com.oa.system.module.leave.controller;

import com.oa.system.common.dto.ApiResponse;
import com.oa.system.module.leave.dto.request.LeaveQueryDTO;
import com.oa.system.module.leave.service.LeaveStatisticsService;
import com.oa.system.module.leave.vo.LeaveStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/leave/statistics")
@Api(tags = "请假统计管理")
@RequiredArgsConstructor
@Validated
public class LeaveStatisticsController {

    private final LeaveStatisticsService leaveStatisticsService;

    @GetMapping
    @ApiOperation("获取请假统计")
    @PreAuthorize("hasAuthority('leave:statistics:view')")
    public ApiResponse<LeaveStatisticsVO> getStatistics(
            @Valid LeaveQueryDTO queryDTO) {
        log.info("获取请假统计,查询条件: {}", queryDTO);
        LeaveStatisticsVO statistics = leaveStatisticsService.getStatistics(queryDTO);
        return ApiResponse.success(statistics);
    }

    @GetMapping("/department")
    @ApiOperation("获取部门请假统计")
    @PreAuthorize("hasAuthority('leave:statistics:view')")
    public ApiResponse<LeaveStatisticsVO> getDepartmentStatistics(
            @ApiParam("部门ID") @RequestParam(required = false) String departmentId,
            @ApiParam("年份") @RequestParam(required = false) Integer year) {
        log.info("获取部门请假统计,部门ID: {}, 年份: {}", departmentId, year);
        LeaveStatisticsVO statistics = leaveStatisticsService.getDepartmentStatistics(departmentId, year);
        return ApiResponse.success(statistics);
    }

    @GetMapping("/employee")
    @ApiOperation("获取员工请假统计")
    @PreAuthorize("hasAuthority('leave:statistics:view')")
    public ApiResponse<LeaveStatisticsVO> getEmployeeStatistics(
            @ApiParam("员工ID") @RequestParam(required = false) String employeeId,
            @ApiParam("年份") @RequestParam(required = false) Integer year) {
        log.info("获取员工请假统计,员工ID: {}, 年份: {}", employeeId, year);
        LeaveStatisticsVO statistics = leaveStatisticsService.getEmployeeStatistics(employeeId, year);
        return ApiResponse.success(statistics);
    }
}
```

---

### 5. HolidayController (节假日控制器)

**文件路径**: `com/oa/system/module/leave/controller/HolidayController.java`

```java
package com.oa.system.module.leave.controller;

import com.oa.system.common.dto.ApiResponse;
import com.oa.system.module.leave.dto.request.HolidayCreateDTO;
import com.oa.system.module.leave.dto.request.HolidayQueryDTO;
import com.oa.system.module.leave.service.HolidayService;
import com.oa.system.module.leave.vo.HolidayVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/leave/holidays")
@Api(tags = "节假日管理")
@RequiredArgsConstructor
@Validated
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping
    @ApiOperation("获取节假日列表")
    @PreAuthorize("hasAuthority('leave:holiday:view')")
    public ApiResponse<List<HolidayVO>> getHolidays(
            @Valid HolidayQueryDTO queryDTO) {
        log.info("获取节假日列表,查询条件: {}", queryDTO);
        List<HolidayVO> holidays = holidayService.getHolidays(queryDTO);
        return ApiResponse.success(holidays);
    }

    @PostMapping
    @ApiOperation("添加节假日")
    @PreAuthorize("hasAuthority('leave:holiday:manage')")
    public ApiResponse<Void> createHoliday(
            @Valid @RequestBody HolidayCreateDTO createDTO) {
        log.info("添加节假日,创建信息: {}", createDTO);
        holidayService.createHoliday(createDTO);
        return ApiResponse.success("添加成功");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除节假日")
    @PreAuthorize("hasAuthority('leave:holiday:manage')")
    public ApiResponse<Void> deleteHoliday(
            @ApiParam("节假日ID") @PathVariable @NotBlank Long id) {
        log.info("删除节假日,节假日ID: {}", id);
        holidayService.deleteHoliday(id);
        return ApiResponse.success("删除成功");
    }
}
```

---

## 🔐 权限控制

### 权限注解说明

| 权限编码 | 权限名称 | 说明 |
|---------|---------|------|
| leave:request:create | 创建请假申请 | 创建请假申请 |
| leave:request:edit | 编辑请假申请 | 编辑草稿状态的申请 |
| leave:request:delete | 删除请假申请 | 删除草稿状态的申请 |
| leave:request:submit | 提交请假申请 | 提交请假申请 |
| leave:request:cancel | 撤销请假申请 | 撤销待审批的申请 |
| leave:request:view | 查看请假申请 | 查看请假申请详情 |
| leave:approval:approve | 审批请假申请 | 审批请假申请 |
| leave:balance:view | 查看年假余额 | 查看年假余额 |
| leave:balance:manage | 管理年假额度 | 管理年假额度 |
| leave:statistics:view | 查看请假统计 | 查看请假统计 |
| leave:holiday:view | 查看节假日 | 查看节假日 |
| leave:holiday:manage | 管理节假日 | 管理节假日 |

### 权限使用示例

```java
@PreAuthorize("hasAuthority('leave:request:create')")
public ApiResponse<String> createLeaveRequest(@Valid @RequestBody LeaveCreateDTO createDTO) {
}

@PreAuthorize("hasAuthority('leave:approval:approve')")
public ApiResponse<LeaveDetailVO> approveRequest(
        @PathVariable @NotBlank String requestId,
        @Valid @RequestBody ApprovalDTO approvalDTO) {
}

@PreAuthorize("hasAuthority('leave:balance:manage')")
public ApiResponse<Void> updateBalance(@Valid @RequestBody BalanceUpdateDTO updateDTO) {
}
```

---

## 📝 API文档

### Swagger注解说明

| 注解 | 说明 |
|------|------|
| @Api | 标记Controller为Swagger资源 |
| @ApiOperation | 描述API接口 |
| @ApiParam | 描述参数 |
| @ApiModel | 描述DTO/VO |
| @ApiModelProperty | 描述字段 |

### Swagger配置

**文件路径**: `com/oa/system/config/SwaggerConfig.java`

```java
package com.oa.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.oa.system.module.leave.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("OA系统 - 请假管理模块API")
                .description("请假管理模块接口文档")
                .version("v1.0.0")
                .build();
    }
}
```

---

## 🚀 异常处理

### 全局异常处理器

**文件路径**: `com/oa/system/common/exception/GlobalExceptionHandler.java`

```java
package com.oa.system.common.exception;

import com.oa.system.common.dto.ApiResponse;
import com.oa.system.module.leave.exception.LeaveBalanceInsufficientException;
import com.oa.system.module.leave.exception.LeaveTimeConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(LeaveBalanceInsufficientException.class)
    public ApiResponse<Void> handleLeaveBalanceInsufficientException(LeaveBalanceInsufficientException e) {
        log.error("年假余额不足: {}", e.getMessage());
        return ApiResponse.error(3002, e.getMessage());
    }

    @ExceptionHandler(LeaveTimeConflictException.class)
    public ApiResponse<Void> handleLeaveTimeConflictException(LeaveTimeConflictException e) {
        log.error("请假时间冲突: {}", e.getMessage());
        return ApiResponse.error(3003, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.error("参数校验失败: {}", message);
        return ApiResponse.error(400, message);
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        log.error("参数绑定失败: {}", message);
        return ApiResponse.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ApiResponse.error(500, "系统异常,请联系管理员");
    }
}
```

---

## 📊 请求日志

### 日志配置

**文件路径**: `resources/logback-spring.xml`

```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/oa-system.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/oa-system.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="com.oa.system.module.leave" level="DEBUG"/>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-18
