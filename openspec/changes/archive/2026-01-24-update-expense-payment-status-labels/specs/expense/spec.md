# 费用报销状态显示规范

## ADDED Requirements

### Requirement: 报销状态名称应准确反映业务阶段
系统 SHALL 使用清晰、准确的状态名称，避免用户混淆。

#### Scenario: 财务审批完成后的状态显示
- **GIVEN** 报销单已完成财务审批
- **AND** 已生成打款记录
- **BUT** 财务尚未实际打款
- **WHEN** 用户在"我的报销"页面查看该报销单
- **THEN** 状态 SHALL 显示为"待打款"
- **AND** 提示信息 SHALL 为"审批已完成，等待财务打款"

#### Scenario: 状态名称在前后端保持一致
- **GIVEN** 报销单状态为 `paid`
- **WHEN** 前端调用 `getExpenseStatusName('paid')`
- **THEN** 应返回"待打款"
- **AND** 后端 `ExpenseStatus.PAID.getName()` SHALL 返回"待打款"

### Requirement: 打款记录与报销单状态应保持语义一致性
系统 MUST 确保不同表（报销单表、打款记录表）对于相同业务阶段的状态描述保持一致。

#### Scenario: 打款管理页面的状态显示
- **GIVEN** 报销单已完成审批，生成打款记录
- **AND** 打款记录状态为 `pending`
- **WHEN** 用户在"打款管理"页面查看该记录
- **THEN** 状态 SHALL 显示为"待打款"
- **AND** 与"我的报销"页面的状态描述 SHALL 保持一致

#### Scenario: 完成打款后的状态显示
- **GIVEN** 财务已完成打款
- **AND** 上传了打款凭证
- **WHEN** 用户在"打款管理"页面查看该记录
- **THEN** 打款记录状态 SHALL 显示为"已完成"
