# Change: 修复打款凭证上传参数错误

## Why

用户在打款管理页面点击"上传凭证"，选择图片并点击"确认上传"后，系统报错"报销单不存在"。

**根本原因**：
- 前端传递的参数是**支付记录ID** (`PaymentRecord.id`，数字类型)
- 后端API期望的参数是**报销单号** (`Expense.expenseId`，字符串类型)
- 导致后端无法找到报销单，抛出 `BusinessException: 报销单不存在`

**错误日志**：
```
Transaction synchronization deregistering SqlSession
Transaction synchronization closing SqlSession
Resolved [BusinessException: 报销单不存在]
```

## What Changes

修复前端调用参数，使用正确的报销单号而非支付记录ID。

**变更内容**：
- 修改 `PaymentManagement.vue` 中的 `handleUploadProof` 函数
- 将 `currentPaymentId.value = row.id` 改为 `currentPaymentId.value = row.expenseId`
- 更新相关的变量命名和类型定义，使其更符合业务语义

## Impact

- **受影响的规范**: expense（打款管理功能）
- **受影响的代码**:
  - `oa-system-frontend/src/modules/expense/components/PaymentManagement.vue`
- **修复效果**:
  - ✅ 用户可以正常上传打款凭证
  - ✅ 后端能够正确找到报销单并保存凭证URL
  - ✅ 打款流程恢复正常

## Risk Assessment

**风险等级**: 🟢 低风险

- ✅ 仅修改前端代码，不涉及后端
- ✅ 不影响数据库结构
- ✅ 不影响其他功能
- ✅ 变更范围小且明确
- ⚠️ 需要测试验证上传功能
