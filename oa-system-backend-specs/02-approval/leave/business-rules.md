# 请假管理模块 - 业务规则

> **模块**: leave
> **版本**: v1.0.0
> **更新日期**: 2026-01-18

---

## 📋 目录

- [请假类型规则](#请假类型规则)
- [请假状态规则](#请假状态规则)
- [审批流程规则](#审批流程规则)
- [年假管理规则](#年假管理规则)
- [请假时长计算规则](#请假时长计算规则)
- [请假时间冲突规则](#请假时间冲突规则)
- [外键约束规则](#外键约束规则)
- [检查约束规则](#检查约束规则)
- [业务验证规则](#业务验证规则)

---

## 请假类型规则

### 请假类型定义

| 类型 | 编码 | 说明 | 需要附件 | 扣减年假 | 审批层级 |
|------|------|------|---------|---------|---------|
| 年假 | annual | 带薪年假 | 否 | 是 | 根据天数 |
| 病假 | sick | 因病请假 | 建议 | 否 | 根据天数 |
| 事假 | personal | 个人事务 | 否 | 否 | 根据天数 |
| 调休 | comp_time | 加班调休 | 否 | 否 | 根据天数 |
| 婚假 | marriage | 结婚请假 | 是 | 否 | 根据天数 |
| 产假 | maternity | 生育请假 | 是 | 否 | 根据天数 |

### 请假类型规则

1. **年假**
   - 需提前申请
   - 需有年假余额
   - 审批通过后自动扣减年假
   - 支持半天请假

2. **病假**
   - 建议提供医院证明
   - 不扣减年假
   - 支持半天请假

3. **事假**
   - 扣除工资
   - 不扣减年假
   - 支持半天请假

4. **调休**
   - 需有加班时长
   - 不扣减年假
   - 支持半天请假

5. **婚假**
   - 需提供结婚证明
   - 标准假期3天
   - 晚婚可延长至15天
   - 不扣减年假

6. **产假**
   - 需提供生育证明
   - 标准假期98天
   - 难产增加15天
   - 多胞胎每多一婴增加15天
   - 不扣减年假

---

## 请假状态规则

### 请假状态定义

| 状态 | 编码 | 说明 | 可执行操作 |
|------|------|------|-----------|
| 草稿 | draft | 未提交的申请 | 编辑、删除、提交 |
| 待审批 | pending | 已提交待审批 | 撤销 |
| 审批中 | approving | 正在审批中 | 无 |
| 已通过 | approved | 审批通过 | 无 |
| 已拒绝 | rejected | 审批拒绝 | 重新提交 |
| 已取消 | cancelled | 已撤销 | 无 |

### 状态转换规则

```
草稿 → 待审批 → 审批中 → 已通过
  ↓        ↓         ↓
已撤销   已拒绝   已拒绝
           ↓
        重新提交 → 待审批
```

### 状态转换说明

1. **草稿 → 待审批**
   - 条件：用户提交申请
   - 验证：年假余额充足（年假类型）

2. **待审批 → 审批中**
   - 条件：一级审批通过
   - 操作：进入下一级审批

3. **审批中 → 已通过**
   - 条件：所有审批层级通过
   - 操作：扣减年假（年假类型）

4. **待审批 → 已拒绝**
   - 条件：任意审批层级拒绝
   - 操作：记录拒绝原因

5. **审批中 → 已拒绝**
   - 条件：任意审批层级拒绝
   - 操作：记录拒绝原因

6. **待审批 → 已取消**
   - 条件：用户撤销申请
   - 操作：删除审批记录

7. **已拒绝 → 待审批**
   - 条件：用户重新提交
   - 操作：创建新的审批流程

---

## 审批流程规则

### 审批层级定义

| 请假天数 | 审批层级 | 审批人 |
|---------|---------|--------|
| ≤3天 | 1级 | 部门负责人 |
| 4-7天 | 2级 | 部门负责人 + 人事专员 |
| >7天 | 3级 | 部门负责人 + 人事专员 + 总经理 |

### 审批流程说明

1. **一级审批（≤3天）**
   - 审批人：部门负责人
   - 通过条件：部门负责人批准
   - 拒绝条件：部门负责人拒绝

2. **二级审批（4-7天）**
   - 第一级：部门负责人
   - 第二级：人事专员
   - 通过条件：两人都批准
   - 拒绝条件：任意一人拒绝

3. **三级审批（>7天）**
   - 第一级：部门负责人
   - 第二级：人事专员
   - 第三级：总经理
   - 通过条件：三人都批准
   - 拒绝条件：任意一人拒绝

### 审批规则

1. **审批顺序**
   - 按审批层级顺序审批
   - 上一级通过后才能进入下一级
   - 任意一级拒绝则流程结束

2. **审批权限**
   - 只有当前审批人可以审批
   - 审批人不能审批自己的申请
   - 审批人不能重复审批同一申请

3. **审批意见**
   - 通过时意见可选
   - 拒绝时意见必填
   - 意见长度1-500字符

4. **审批时效**
   - 待审批申请需要在3个工作日内处理
   - 超时自动提醒审批人
   - 超时7天自动升级到上级

---

## 年假管理规则

### 年假额度标准

| 工作年限 | 年假天数 | 说明 |
|---------|---------|------|
| <1年 | 5天 | 新员工 |
| 1-9年 | 10天 | 普通员工 |
| 10-19年 | 15天 | 资深员工 |
| ≥20年 | 20天 | 高级员工 |

### 年假额度计算

```java
public BigDecimal calculateQuota(Integer workYears) {
    if (workYears < 1) {
        return new BigDecimal("5");
    } else if (workYears < 10) {
        return new BigDecimal("10");
    } else if (workYears < 20) {
        return new BigDecimal("15");
    } else {
        return new BigDecimal("20");
    }
}
```

### 年假使用规则

1. **年假余额**
   - 年假余额 = 年假总额 - 已使用天数
   - 年假余额不能为负数
   - 年假余额跨年不累计

2. **年假扣减**
   - 审批通过后自动扣减
   - 扣减金额 = 请假时长
   - 记录使用日志

3. **年假回退**
   - 申请被拒绝后自动回退
   - 申请被撤销后自动回退
   - 记录回退日志

4. **年假余额不足**
   - 余额不足时禁止提交年假申请
   - 提示用户剩余年假天数
   - 建议用户选择其他请假类型

### 年假余额警告

| 余额 | 警告级别 | 提示信息 |
|------|---------|---------|
| ≥3天 | 正常 | 无提示 |
| 1-3天 | 警告 | 年假余额不足3天 |
| <1天 | 严重 | 年假余额不足1天 |

---

## 请假时长计算规则

### 计算规则

1. **工作日计算**
   - 只计算工作日（周一到周五）
   - 排除法定节假日
   - 排除调休工作日

2. **时长单位**
   - 1天 = 8小时
   - 支持半天请假（0.5天）
   - 支持按小时请假（0.125天）

3. **计算公式**
   ```
   时长(天) = 工作日天数 + (工作日小时数 / 8)
   ```

### 计算示例

| 开始时间 | 结束时间 | 工作日 | 时长(天) |
|---------|---------|--------|---------|
| 2026-01-20 09:00 | 2026-01-20 18:00 | 1 | 1.0 |
| 2026-01-20 09:00 | 2026-01-20 13:00 | 1 | 0.5 |
| 2026-01-20 09:00 | 2026-01-22 18:00 | 3 | 3.0 |
| 2026-01-20 09:00 | 2026-01-23 18:00 | 4 | 4.0 |

### 跨天计算

1. **跨周末**
   - 周末不计入工作日
   - 自动跳过周末
   - 只计算工作日

2. **跨节假日**
   - 节假日不计入工作日
   - 自动跳过节假日
   - 只计算工作日

3. **跨月**
   - 按实际工作日计算
   - 不考虑月份界限
   - 只计算工作日

---

## 请假时间冲突规则

### 冲突检测规则

1. **冲突定义**
   - 时间段重叠
   - 时间段包含
   - 时间段被包含

2. **冲突检测范围**
   - 只检测待审批、审批中、已通过的申请
   - 不检测草稿、已拒绝、已取消的申请
   - 不检测其他员工的申请

3. **冲突检测逻辑**
   ```
   冲突条件：
   (开始时间 <= 其他开始时间 AND 结束时间 > 其他开始时间)
   OR (开始时间 < 其他结束时间 AND 结束时间 >= 其他结束时间)
   OR (开始时间 >= 其他开始时间 AND 结束时间 <= 其他结束时间)
   ```

### 冲突处理

1. **创建申请时**
   - 检测时间冲突
   - 冲突时提示用户
   - 禁止创建冲突申请

2. **更新申请时**
   - 检测时间冲突
   - 排除当前申请
   - 冲突时提示用户
   - 禁止更新为冲突时间

3. **冲突提示**
   ```
   "请假时间与以下申请冲突：
   - LEAVE20260115001: 2026-01-20 至 2026-01-22"
   ```

---

## 外键约束规则

### 外键约束说明

由于数据库层面的外键约束可能影响性能，建议在Service层实现外键约束逻辑：

### 1. 申请人外键约束

```java
public void validateApplicantId(String applicantId) {
    Employee employee = employeeService.getById(applicantId);
    if (employee == null) {
        throw new BusinessException(3001, "申请人不存在");
    }
    if (employee.getIsDeleted() == 1) {
        throw new BusinessException(3002, "申请人已被删除");
    }
    if (!"active".equals(employee.getStatus())) {
        throw new BusinessException(3003, "申请人状态异常");
    }
}
```

### 2. 部门外键约束

```java
public void validateDepartmentId(String departmentId) {
    Department department = departmentService.getById(departmentId);
    if (department == null) {
        throw new BusinessException(3004, "部门不存在");
    }
    if (department.getIsDeleted() == 1) {
        throw new BusinessException(3005, "部门已被删除");
    }
    if (!"active".equals(department.getStatus())) {
        throw new BusinessException(3006, "部门状态异常");
    }
}
```

### 3. 审批人外键约束

```java
public void validateApproverId(String approverId) {
    Employee employee = employeeService.getById(approverId);
    if (employee == null) {
        throw new BusinessException(3007, "审批人不存在");
    }
    if (employee.getIsDeleted() == 1) {
        throw new BusinessException(3008, "审批人已被删除");
    }
    if (!"active".equals(employee.getStatus())) {
        throw new BusinessException(3009, "审批人状态异常");
    }
}
```

### 4. 申请外键约束

```java
public void validateRequestId(String requestId) {
    LeaveRequest request = leaveRequestService.getById(requestId);
    if (request == null) {
        throw new BusinessException(3010, "请假申请不存在");
    }
    if (request.getIsDeleted() == 1) {
        throw new BusinessException(3011, "请假申请已被删除");
    }
}
```

---

## 检查约束规则

### 检查约束说明

在Service层实现检查约束逻辑：

### 1. 请假时长检查

```java
public void validateDuration(BigDecimal duration) {
    if (duration == null) {
        throw new BusinessException(3012, "请假时长不能为空");
    }
    if (duration.compareTo(BigDecimal.ZERO) <= 0) {
        throw new BusinessException(3013, "请假时长必须大于0");
    }
    if (duration.compareTo(new BigDecimal("30")) > 0) {
        throw new BusinessException(3014, "请假时长不能超过30天");
    }
}
```

### 2. 时间范围检查

```java
public void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime == null) {
        throw new BusinessException(3015, "开始时间不能为空");
    }
    if (endTime == null) {
        throw new BusinessException(3016, "结束时间不能为空");
    }
    if (!endTime.isAfter(startTime)) {
        throw new BusinessException(3017, "结束时间必须晚于开始时间");
    }
    if (startTime.isBefore(LocalDateTime.now())) {
        throw new BusinessException(3018, "开始时间不能早于当前时间");
    }
}
```

### 3. 年假总额检查

```java
public void validateAnnualTotal(BigDecimal annualTotal) {
    if (annualTotal == null) {
        throw new BusinessException(3019, "年假总额不能为空");
    }
    if (annualTotal.compareTo(BigDecimal.ZERO) < 0) {
        throw new BusinessException(3020, "年假总额不能小于0");
    }
    if (annualTotal.compareTo(new BigDecimal("30")) > 0) {
        throw new BusinessException(3021, "年假总额不能超过30天");
    }
}
```

### 4. 年假使用检查

```java
public void validateAnnualUsed(BigDecimal annualUsed, BigDecimal annualTotal) {
    if (annualUsed == null) {
        throw new BusinessException(3022, "已使用天数不能为空");
    }
    if (annualUsed.compareTo(BigDecimal.ZERO) < 0) {
        throw new BusinessException(3023, "已使用天数不能小于0");
    }
    if (annualUsed.compareTo(annualTotal) > 0) {
        throw new BusinessException(3024, "已使用天数不能大于年假总额");
    }
}
```

### 5. 年假余额检查

```java
public void validateAnnualRemaining(BigDecimal annualRemaining, BigDecimal annualTotal, BigDecimal annualUsed) {
    if (annualRemaining == null) {
        throw new BusinessException(3025, "剩余天数不能为空");
    }
    if (annualRemaining.compareTo(BigDecimal.ZERO) < 0) {
        throw new BusinessException(3026, "剩余天数不能小于0");
    }
    BigDecimal expectedRemaining = annualTotal.subtract(annualUsed);
    if (annualRemaining.compareTo(expectedRemaining) != 0) {
        throw new BusinessException(3027, "剩余天数计算错误");
    }
}
```

---

## 业务验证规则

### 1. 创建申请验证

```java
public void validateCreateRequest(LeaveCreateDTO createDTO) {
    validateType(createDTO.getType());
    validateTimeRange(createDTO.getStartTime(), createDTO.getEndTime());
    validateDuration(createDTO.getDuration());
    validateReason(createDTO.getReason());
    validateAttachments(createDTO.getAttachments());
    validateApplicantId(getCurrentUserId());
    validateDepartmentId(getCurrentUserDepartmentId());
    validateTimeConflict(getCurrentUserId(), createDTO.getStartTime(), createDTO.getEndTime(), null);
}
```

### 2. 更新申请验证

```java
public void validateUpdateRequest(LeaveRequest request, LeaveUpdateDTO updateDTO) {
    validateStatusForEdit(request.getStatus());
    validateVersion(request.getVersion(), updateDTO.getVersion());
    validateType(updateDTO.getType());
    validateTimeRange(updateDTO.getStartTime(), updateDTO.getEndTime());
    validateDuration(updateDTO.getDuration());
    validateReason(updateDTO.getReason());
    validateAttachments(updateDTO.getAttachments());
    validateTimeConflict(request.getApplicantId(), updateDTO.getStartTime(), updateDTO.getEndTime(), request.getId());
}
```

### 3. 提交申请验证

```java
public void validateSubmitRequest(LeaveRequest request) {
    validateStatusForSubmit(request.getStatus());
    validateDuration(request.getDuration());
    validateAttachmentsForType(request.getType(), request.getAttachments());
    validateBalanceForAnnualLeave(request);
}
```

### 4. 删除申请验证

```java
public void validateDeleteRequest(LeaveRequest request) {
    validateStatusForDelete(request.getStatus());
    validateVersion(request.getVersion(), request.getVersion());
}
```

### 5. 撤销申请验证

```java
public void validateCancelRequest(LeaveRequest request) {
    validateStatusForCancel(request.getStatus());
    validateVersion(request.getVersion(), request.getVersion());
}
```

### 6. 重新提交申请验证

```java
public void validateResubmitRequest(LeaveRequest request) {
    validateStatusForResubmit(request.getStatus());
    validateVersion(request.getVersion(), request.getVersion());
}
```

### 7. 审批申请验证

```java
public void validateApproveRequest(LeaveRequest request, ApprovalDTO approvalDTO) {
    validateStatusForApprove(request.getStatus());
    validateApproverPermission(request, getCurrentUserId());
    validateApprovalStatus(approvalDTO.getStatus());
    validateApprovalOpinion(approvalDTO.getStatus(), approvalDTO.getOpinion());
}
```

---

## 错误码定义

| 错误码 | 说明 |
|--------|------|
| 3001 | 请假申请不存在 |
| 3002 | 年假余额不足 |
| 3003 | 请假时间冲突 |
| 3004 | 审批权限不足 |
| 3005 | 当前状态不允许操作 |
| 3006 | 请假时长计算错误 |
| 3007 | 审批流程配置错误 |
| 3008 | 申请人不存在 |
| 3009 | 申请人已被删除 |
| 3010 | 申请人状态异常 |
| 3011 | 部门不存在 |
| 3012 | 部门已被删除 |
| 3013 | 部门状态异常 |
| 3014 | 审批人不存在 |
| 3015 | 审批人已被删除 |
| 3016 | 审批人状态异常 |
| 3017 | 请假申请已被删除 |
| 3018 | 请假时长不能为空 |
| 3019 | 请假时长必须大于0 |
| 3020 | 请假时长不能超过30天 |
| 3021 | 开始时间不能为空 |
| 3022 | 结束时间不能为空 |
| 3023 | 结束时间必须晚于开始时间 |
| 3024 | 开始时间不能早于当前时间 |
| 3025 | 年假总额不能为空 |
| 3026 | 年假总额不能小于0 |
| 3027 | 年假总额不能超过30天 |
| 3028 | 已使用天数不能为空 |
| 3029 | 已使用天数不能小于0 |
| 3030 | 已使用天数不能大于年假总额 |
| 3031 | 剩余天数不能为空 |
| 3032 | 剩余天数不能小于0 |
| 3033 | 剩余天数计算错误 |

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-18
