# expense Specification

## MODIFIED Requirements

### Requirement: 状态更新的空值安全
系统 SHALL 在更新报销单状态之前进行充分的空值检查，确保不会访问 null 对象的属性。

#### Scenario: 从打款管理页面上传凭证
- **GIVEN** 用户在"打款管理"页面点击"上传凭证"按钮
- **AND** store 中的 `currentExpense` 为 null（因为该页面未初始化）
- **WHEN** 用户点击"确认上传"按钮
- **THEN** 系统 SHALL 检查 `currentExpense.value` 是否存在
- **AND** 系统 SHALL 仅在 `currentExpense.value` 存在时更新其属性
- **AND** 系统 SHALL 不抛出空值访问错误

#### Scenario: 从我的报销页面上传凭证
- **GIVEN** 用户在"我的报销"页面查看报销单详情
- **AND** store 中的 `currentExpense` 已初始化
- **WHEN** 用户上传打款凭证
- **THEN** 系统 SHALL 更新 `currentExpense.value` 的状态
- **AND** 系统行为 SHALL 与之前保持一致

#### Scenario: 空值检查的防御性编程
- **GIVEN** 代码中需要访问 `currentExpense.value` 的属性
- **WHEN** 执行任何属性访问操作
- **THEN** 系统 SHALL 使用可选链操作符或显式的空值检查
- **AND** 系统 SHALL 确保不会访问 null 或 undefined 对象的属性
