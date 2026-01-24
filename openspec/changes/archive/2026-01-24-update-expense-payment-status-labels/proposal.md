# Change: 优化费用报销状态显示文案

## Why
当前费用报销模块存在状态显示混淆的问题：
- 报销单状态 `paid` 显示为"已打款"
- 打款记录状态 `pending` 显示为"待打款"

这导致用户困惑：同一个报销单在"我的报销"页面显示"已打款"，在"打款管理"页面显示"待打款"，给用户造成钱已经到账的错误印象。

实际上，`paid` 状态表示"审批完成，已生成打款记录"，但财务还未实际打款。当前文案"已打款"具有误导性。

## What Changes
- **后端**：将 `ExpenseStatus.PAID` 的描述从"已打款"改为"待打款"
- **前端**：同步更新状态映射和提示信息
  - `getExpenseStatusName()`: `paid` → "待打款"
  - `getStatusTip()`: 更新提示文案为"审批已完成，等待财务打款"

## Impact
- **受影响的规范**: 无（这是首次创建规范）
- **受影响的代码**:
  - `oa-system-backend/src/main/java/com/example/oa_system_backend/module/expense/enums/ExpenseStatus.java`
  - `oa-system-frontend/src/modules/expense/utils/index.ts`
- **用户体验**: 消除状态显示混淆，让用户准确理解报销单当前所处阶段
