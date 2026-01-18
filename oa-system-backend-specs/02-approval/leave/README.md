# 请假管理模块 - 后端技术规范

> **模块**: leave
> **版本**: v1.0.0
> **更新日期**: 2026-01-18
> **技术栈**: Spring Boot + MyBatis Plus + MySQL

---

## 📋 目录

- [实体类规范](entity-specification.md)
- [DTO规范](dto-specification.md)
- [VO规范](vo-specification.md)
- [Service层规范](service-specification.md)
- [Controller层规范](controller-specification.md)
- [Mapper层规范](mapper-specification.md)
- [业务规则](business-rules.md)

---

## 🎯 模块概述

请假管理模块是OA系统的核心审批模块之一，提供完整的请假申请、审批、年假管理等功能。

### 核心功能

1. **请假申请管理**
   - 创建、编辑、删除、提交、撤销请假申请
   - 支持多种请假类型（年假、病假、事假、调休、婚假、产假）
   - 自动计算请假时长（工作日）
   - 附件上传

2. **多级审批流程**
   - 根据请假天数自动确定审批层级
   - 1级审批：≤3天（部门负责人）
   - 2级审批：4-7天（部门负责人 + 人事专员）
   - 3级审批：>7天（部门负责人 + 人事专员 + 总经理）

3. **年假管理**
   - 年假余额查询
   - 年假自动扣减（审批通过后）
   - 年假额度根据工龄自动计算
   - 年假使用记录

4. **统计分析**
   - 请假统计（按类型、状态、部门）
   - 年假使用情况统计
   - 审批效率统计

---

## 📦 数据库表

### 表列表

| 表名 | 说明 | 主键 |
|------|------|------|
| approval_leave_request | 请假申请表 | id (VARCHAR) |
| approval_leave_approval | 审批记录表 | id (BIGINT) |
| approval_leave_balance | 年假余额表 | id (BIGINT) |
| approval_leave_usage_log | 年假使用记录表 | id (BIGINT) |
| approval_holiday | 节假日表 | id (BIGINT) |

---

## 🏗️ 项目结构

```
com/oa/system/module/leave/
├── entity/                    # 实体类
│   ├── LeaveRequest.java
│   ├── LeaveApproval.java
│   ├── LeaveBalance.java
│   ├── LeaveUsageLog.java
│   └── Holiday.java
├── dto/                       # 数据传输对象
│   ├── request/
│   │   ├── LeaveCreateDTO.java
│   │   ├── LeaveUpdateDTO.java
│   │   ├── LeaveQueryDTO.java
│   │   ├── LeaveSubmitDTO.java
│   │   ├── ApprovalDTO.java
│   │   └── BalanceQueryDTO.java
│   └── response/
│       └── LeaveStatisticsVO.java
├── vo/                        # 视图对象
│   ├── LeaveRequestVO.java
│   ├── LeaveDetailVO.java
│   ├── ApprovalRecordVO.java
│   ├── LeaveBalanceVO.java
│   └── LeaveStatisticsVO.java
├── mapper/                    # Mapper接口
│   ├── LeaveRequestMapper.java
│   ├── LeaveApprovalMapper.java
│   ├── LeaveBalanceMapper.java
│   ├── LeaveUsageLogMapper.java
│   └── HolidayMapper.java
├── service/                   # Service接口
│   ├── LeaveRequestService.java
│   ├── LeaveApprovalService.java
│   ├── LeaveBalanceService.java
│   ├── HolidayService.java
│   └── LeaveStatisticsService.java
├── service/impl/              # Service实现
│   ├── LeaveRequestServiceImpl.java
│   ├── LeaveApprovalServiceImpl.java
│   ├── LeaveBalanceServiceImpl.java
│   ├── HolidayServiceImpl.java
│   └── LeaveStatisticsServiceImpl.java
├── controller/               # Controller
│   ├── LeaveRequestController.java
│   ├── LeaveApprovalController.java
│   ├── LeaveBalanceController.java
│   └── LeaveStatisticsController.java
├── enums/                    # 枚举类
│   ├── LeaveType.java
│   ├── LeaveStatus.java
│   └── ApprovalStatus.java
├── util/                     # 工具类
│   ├── LeaveDurationCalculator.java
│   ├── WorkdayCalculator.java
│   └── LeaveIdGenerator.java
└── exception/                # 异常类
    ├── LeaveBalanceInsufficientException.java
    └── LeaveTimeConflictException.java
```

---

## 🔗 外键约束

### 外键列表

| 表名 | 外键字段 | 引用表 | 引用字段 | 删除规则 | 更新规则 |
|------|---------|--------|---------|---------|---------|
| approval_leave_request | applicant_id | sys_employee | id | RESTRICT | CASCADE |
| approval_leave_request | department_id | sys_department | id | RESTRICT | CASCADE |
| approval_leave_approval | request_id | approval_leave_request | id | CASCADE | CASCADE |
| approval_leave_approval | approver_id | sys_employee | id | RESTRICT | CASCADE |
| approval_leave_balance | employee_id | sys_employee | id | CASCADE | CASCADE |
| approval_leave_usage_log | employee_id | sys_employee | id | CASCADE | CASCADE |

---

## ✅ 检查约束

### 检查约束列表

| 表名 | 约束名称 | 约束条件 |
|------|---------|---------|
| approval_leave_request | chk_leave_duration | duration > 0 |
| approval_leave_request | chk_leave_time | end_time > start_time |
| approval_leave_balance | chk_leave_total | annual_total >= 0 |
| approval_leave_balance | chk_leave_used | annual_used >= 0 AND annual_used <= annual_total |
| approval_leave_balance | chk_leave_remaining | annual_remaining = annual_total - annual_used |

---

## 📊 业务规则

### 请假类型规则

| 类型 | 编码 | 需要附件 | 扣减年假 | 审批层级 |
|------|------|---------|---------|---------|
| 年假 | annual | 否 | 是 | 根据天数 |
| 病假 | sick | 建议 | 否 | 根据天数 |
| 事假 | personal | 否 | 否 | 根据天数 |
| 调休 | comp_time | 否 | 否 | 根据天数 |
| 婚假 | marriage | 是 | 否 | 根据天数 |
| 产假 | maternity | 是 | 否 | 根据天数 |

### 年假额度规则

| 工作年限 | 年假天数 |
|---------|---------|
| <1年 | 5天 |
| 1-9年 | 10天 |
| 10-19年 | 15天 |
| ≥20年 | 20天 |

### 审批层级规则

| 请假天数 | 审批层级 | 审批人 |
|---------|---------|--------|
| ≤3天 | 1级 | 部门负责人 |
| 4-7天 | 2级 | 部门负责人 + 人事专员 |
| >7天 | 3级 | 部门负责人 + 人事专员 + 总经理 |

---

## 🔐 权限控制

### 权限列表

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

---

## 📝 API接口列表

### 请假申请接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/leave/requests | 获取请假申请列表 |
| GET | /api/leave/requests/{id} | 获取请假申请详情 |
| POST | /api/leave/requests | 创建请假申请 |
| PUT | /api/leave/requests/{id} | 更新请假申请 |
| DELETE | /api/leave/requests/{id} | 删除请假申请 |
| POST | /api/leave/requests/{id}/submit | 提交请假申请 |
| POST | /api/leave/requests/{id}/cancel | 撤销请假申请 |
| POST | /api/leave/requests/{id}/resubmit | 重新提交请假申请 |

### 审批接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/leave/approvals/pending | 获取待审批列表 |
| GET | /api/leave/approvals/history | 获取已审批列表 |
| GET | /api/leave/approvals/{requestId} | 获取审批记录 |
| POST | /api/leave/approvals/{requestId}/approve | 审批请假申请 |

### 年假管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/leave/balance | 获取年假余额 |
| GET | /api/leave/balance/list | 获取年假余额列表 |
| PUT | /api/leave/balance/quota | 更新年假额度 |

### 统计接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/leave/statistics | 获取请假统计 |
| GET | /api/leave/statistics/department | 获取部门请假统计 |
| GET | /api/leave/statistics/employee | 获取员工请假统计 |

### 节假日接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/leave/holidays | 获取节假日列表 |
| POST | /api/leave/holidays | 添加节假日 |
| DELETE | /api/leave/holidays/{id} | 删除节假日 |

---

## 🎨 前后端交互规范

### 请求格式

所有请求使用JSON格式，Content-Type为`application/json`。

### 响应格式

统一响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1642579200000
}
```

### 错误码

| 错误码 | 说明 |
|--------|------|
| 3001 | 请假申请不存在 |
| 3002 | 年假余额不足 |
| 3003 | 请假时间冲突 |
| 3004 | 审批权限不足 |
| 3005 | 当前状态不允许操作 |
| 3006 | 请假时长计算错误 |
| 3007 | 审批流程配置错误 |

---

## 🚀 部署说明

### 环境要求

- JDK 11+
- MySQL 8.0+
- Spring Boot 2.7+
- MyBatis Plus 3.5+

### 配置文件

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/oa_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  mapper-locations: classpath*:mapper/leave/**/*Mapper.xml
  type-aliases-package: com.oa.system.module.leave.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-18
