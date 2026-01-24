# Change: 添加报销单"已完成"状态（基于数据字典）

## Why

用户上传打款凭证后，报销单状态没有发生变化，仍显示为"待打款"（枚举）或"已打款"（数据字典），导致用户不清楚报销流程是否完成。

**根本原因**：
1. **状态流转不完整**：当前只有 5 个状态，缺少"已完成"状态来表示报销流程已结束
2. **枚举和字典不一致**：
   - 数据字典中 `paid` = "已打款"
   - 枚举中 `PAID` = "待打款"（之前修复时修改的）
3. **上传凭证逻辑缺陷**：
   - 上传凭证后，只保存了凭证URL
   - 没有将状态更新为"已完成"
   - 用户无法从状态上确认打款是否完成

**业务流程**：
```
草稿 → 待部门审批 → 待财务审批 → 待打款 → [上传凭证] → 已完成✅
                                                   ↑
                                               当前卡在这里
```

## What Changes

基于数据字典添加"已完成"状态，完善报销单的状态流转。

**变更内容**：
1. **数据字典**：添加 `completed` 状态到 `expense_status` 字典类型
2. **后端枚举**：在 `ExpenseStatus` 中添加 `COMPLETED` 枚举值
3. **后端逻辑**：修改 `uploadPaymentProof` 方法，上传凭证后将状态改为 `COMPLETED`
4. **前端类型**：更新 TypeScript 类型定义和状态映射
5. **前端逻辑**：更新 store 中的状态更新逻辑

**数据字典配置**：
```sql
INSERT INTO sys_dict_item (
  dict_type_id, dict_type_code, type_code,
  label, value, color_type, color,
  sort_order, status
) VALUES (
  10, 'expense_status', 'expense_status',
  '已完成', 'completed', 'success', '#67C23A',
  6, 'enabled'
);
```

## Impact

- **受影响的规范**: expense（费用报销功能）
- **受影响的代码**:
  - 数据库：
    - `sys_dict_item` 表 - 添加 completed 状态
  - 后端：
    - `ExpenseStatus.java` - 添加 COMPLETED 状态
    - `ExpenseServiceImpl.java` - 上传凭证后更新状态
  - 前端：
    - `types/index.ts` - 更新状态类型
    - `utils/index.ts` - 更新状态映射
    - `store/index.ts` - 更新状态更新逻辑
- **修复效果**:
  - ✅ 用户上传凭证后，状态变为"已完成"
  - ✅ 报销流程状态清晰，用户知道流程已结束
  - ✅ 数据字典和枚举保持一致

## Risk Assessment

**风险等级**: 🟡 中等风险

- ✅ 基于数据字典，符合系统架构
- ✅ 状态流转逻辑清晰，符合业务需求
- ⚠️ 需要更新前后端代码
- ⚠️ 需要数据库添加字典项
- ⚠️ 需要测试所有相关功能
- ✅ 向后兼容（旧数据会保持原状态）

## Implementation Notes

**状态定义**：
- Label: `已完成`
- Value: `completed`
- Color Type: `success`
- Color: `#67C23A`（绿色）
- Sort Order: `6`（最后）

**触发条件**：
- 用户上传打款凭证成功后

**权限要求**：
- 需要财务权限（`expense:payment`）

**数据一致性**：
- 确保枚举和数据字典的值一致
- 确保前端和后端使用相同的状态值
