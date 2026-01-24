# 报销单状态流转文档

**版本**: 2.0
**更新日期**: 2026-01-24
**模块**: 费用报销

---

## 状态流转图

```
┌──────────────────────────────────────────────────────────────┐
│                    报销单完整流程                              │
└──────────────────────────────────────────────────────────────┘

  ┌────────┐    ┌───────────┐    ┌─────────────┐    ┌────────┐
  │  草稿   │ →  │ 待部门审批  │ →  │ 待财务审批   │ →  │ 待打款 │
  └────────┘    └───────────┘    └─────────────┘    └────────┘
      │              │                  │               │
      │              │                  │               ↓
      │              │                  │         ┌────────┐
      │              │                  │         │ 已完成 │ ✨ 新增
      │              │                  │         └────────┘
      │              │                  │               ↑
      ↓              ↓                  ↓               │
   可编辑        可取消              可取消         [上传凭证]

  ┌────────┐    ┌───────────┐    ┌─────────────┐
  │ 已驳回  │ ←  │ 已驳回  │ ←  │ 已驳回      │
  └────────┘    └───────────┘    └─────────────┘
      ↑              ↑                  ↑
      └──────────────┴──────────────────┘
                  (任何阶段可驳回)
```

---

## 状态详细说明

### 1. 草稿 (draft)
- **说明**: 报销单创建后的初始状态
- **可操作**: 编辑、删除、提交
- **下一步**: 提交后进入"待部门审批"
- **颜色**: 灰色 (info)
- **代码**: `draft`

### 2. 待部门审批 (dept_pending)
- **说明**: 等待部门主管审批
- **可操作**: 取消
- **下一步**: 审批通过 → "待财务审批"，驳回 → "已驳回"
- **颜色**: 橙色 (warning)
- **代码**: `dept_pending`

### 3. 待财务审批 (finance_pending)
- **说明**: 等待财务人员审批
- **可操作**: 取消
- **下一步**: 审批通过 → "待打款"，驳回 → "已驳回"
- **颜色**: 蓝色 (primary)
- **代码**: `finance_pending`

### 4. 待打款 (paid)
- **说明**: 财务审批已完成，等待财务打款
- **可操作**: 无（等待打款）
- **下一步**: 上传凭证 → "已完成" ✨
- **颜色**: 绿色 (success)
- **代码**: `paid`
- **提示**: "审批已完成，等待财务打款"

### 5. 已完成 (completed) ✨ 新增
- **说明**: 报销流程已全部结束，打款凭证已上传
- **可操作**: 查看详情、查看凭证
- **不可操作**: 编辑、删除、审批
- **颜色**: 绿色 (success)
- **代码**: `completed`
- **提示**: "报销流程已完成，打款凭证已上传"
- **触发条件**: 财务上传打款凭证成功后自动更新

### 6. 已驳回 (rejected)
- **说明**: 报销单被审批人驳回
- **可操作**: 修改后重新提交
- **下一步**: 重新提交 → "待部门审批"
- **颜色**: 红色 (danger)
- **代码**: `rejected`

---

## 状态变更规则

### 自动流转
1. **草稿** → **待部门审批**: 用户提交报销单
2. **待部门审批** → **待财务审批**: 部门主管审批通过
3. **待财务审批** → **待打款**: 财务人员审批通过
4. **待打款** → **已完成**: 上传打款凭证 ✨

### 人工操作
1. **任何审批阶段** → **已驳回**: 审批人驳回
2. **已驳回** → **草稿**: 用户修改后重新提交

### 可逆操作
- **待部门审批** → **草稿**: 撤销提交
- **待财务审批** → **草稿**: 撤销提交

### 终止状态
- **已完成**: 流程结束，不可再操作
- **已驳回**: 需要重新提交才能继续

---

## 数据字典配置

### 字典类型: expense_status

| 序号 | Label | Value | Color Type | Color | Sort |
|------|-------|-------|------------|-------|------|
| 1 | 草稿 | draft | | #909399 | 1 |
| 2 | 待部门审批 | dept_pending | | #E6A23C | 2 |
| 3 | 待财务审批 | finance_pending | | #409EFF | 3 |
| 4 | 待打款 | paid | | #67C23A | 4 |
| 5 | **已完成** | **completed** | **success** | **#67C23A** | **6** ✨ |
| 6 | 已驳回 | rejected | | #F56C6C | 7 |

---

## API 相关

### 上传打款凭证 API

**端点**: `POST /expense/{expenseId}/payment-proof`

**请求参数**:
- `expenseId` (路径参数): 报销单号
- `proofUrl` (查询参数): 打款凭证URL

**状态更新逻辑**:
```java
// 只有状态为"待打款"时才允许上传
if (!ExpenseStatus.PAID.getCode().equals(expense.getStatus())) {
    throw new BusinessException(4015, "当前状态不允许上传打款凭证");
}

// 保存凭证URL
expense.setPaymentProof(proofUrl);

// ✨ 更新状态为"已完成"
expense.setStatus(ExpenseStatus.COMPLETED.getCode());

// 更新时间戳
expense.setUpdatedAt(LocalDateTime.now());

// 保存到数据库
updateById(expense);
```

---

## 前端状态映射

### TypeScript 类型
```typescript
export type ExpenseStatus =
  | 'draft'
  | 'dept_pending'
  | 'finance_pending'
  | 'paid'
  | 'completed' ✨ 新增
  | 'rejected'
```

### 状态名称映射
```typescript
const statusMap: Record<ExpenseStatus, string> = {
  draft: '草稿',
  dept_pending: '部门审批',
  finance_pending: '财务审批',
  paid: '待打款',
  completed: '已完成',  ✨ 新增
  rejected: '已驳回'
}
```

### 状态标签类型
```typescript
const typeMap: Record<ExpenseStatus, string> = {
  draft: 'info',
  dept_pending: 'warning',
  finance_pending: 'primary',
  paid: 'success',
  completed: 'success',  ✨ 新增
  rejected: 'danger'
}
```

### 状态提示信息
```typescript
const tipMap: Record<ExpenseStatus, string> = {
  draft: '草稿状态的报销单可以编辑和删除',
  dept_pending: '等待部门主管审批',
  finance_pending: '等待财务人员审批',
  paid: '审批已完成，等待财务打款',
  completed: '报销流程已完成，打款凭证已上传',  ✨ 新增
  rejected: '报销已被驳回,可以修改后重新提交'
}
```

---

## 后端枚举定义

### ExpenseStatus.java

```java
public enum ExpenseStatus {
    DRAFT("draft", "草稿"),
    DEPT_PENDING("dept_pending", "待部门审批"),
    FINANCE_PENDING("finance_pending", "待财务审批"),
    PAID("paid", "待打款"),
    COMPLETED("completed", "已完成"),  ✨ 新增
    REJECTED("rejected", "已拒绝");
}
```

---

## 数据库表结构

### expense 表

| 字段名 | 类型 | 说明 | 变更 |
|--------|------|------|------|
| status | varchar(50) | 报销单状态 | 新增 'completed' 值 |
| payment_proof | text | 打款凭证URL | 与 completed 配合使用 |
| updated_at | datetime | 更新时间 | 上传凭证时更新 |

---

## 版本历史

| 版本 | 日期 | 变更内容 | 变更人 |
|------|------|----------|--------|
| 2.0 | 2026-01-24 | 添加"已完成"状态 | Claude Code |
| 1.0 | 2026-01-12 | 初始版本（5个状态） | 系统 |

---

## 注意事项

### 重要变更
1. ✨ 新增"已完成"状态，完善报销流程
2. ✨ 上传凭证后自动更新为"已完成"
3. ✨ "待打款"状态的含义：审批已完成，等待打款

### 向后兼容
- ✅ 旧数据保持原状态（"待打款"）
- ✅ 只有新上传凭证的才会变为"已完成"
- ✅ API 接口保持兼容

### 权限要求
- 上传凭证需要财务权限（`expense:payment`）
- 只有"待打款"状态的报销单可以上传凭证

---

**文档维护**: 费用报销模块团队
**最后审核**: 2026-01-24
