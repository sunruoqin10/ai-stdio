# expense Specification

## ADDED Requirements

### Requirement: 报销单已完成状态
系统 SHALL 支持"已完成"状态，用于表示报销流程已全部结束。系统 SHALL 在用户上传打款凭证成功后，自动将报销单状态从"待打款"更新为"已完成"。

**状态定义**：
- **Value**: `completed`
- **Label**: `已完成`
- **Color Type**: `success`
- **Color**: `#67C23A`（绿色）
- **Sort Order**: `6`（在所有状态之后）

#### Scenario: 上传打款凭证后状态更新
- **GIVEN** 报销单当前状态为"待打款"（`paid`）
- **AND** 用户在"打款管理"页面点击"上传凭证"按钮
- **AND** 用户已选择凭证图片
- **WHEN** 用户点击"确认上传"按钮
- **THEN** 系统 SHALL 保存凭证URL到数据库
- **AND** 系统 SHALL 将报销单状态更新为"已完成"（`completed`）
- **AND** 系统 SHALL 更新 `updatedAt` 时间戳
- **AND** 系统 SHALL 记录操作日志

#### Scenario: 状态更新后的列表显示
- **GIVEN** 用户上传凭证成功
- **AND** 报销单状态已更新为"已完成"
- **WHEN** 用户查看"我的报销"列表
- **THEN** 系统 SHALL 显示该报销单的状态为"已完成"
- **AND** 状态标签 SHALL 显示为绿色（success）
- **AND** 状态图标 SHALL 表示流程已完成

#### Scenario: 状态更新后的详情显示
- **GIVEN** 用户上传凭证成功
- **AND** 报销单状态已更新为"已完成"
- **WHEN** 用户查看报销单详情
- **THEN** 系统 SHALL 显示"已完成"状态
- **AND** 系统 SHALL 显示打款凭证（可点击查看）
- **AND** 系统 SHALL 禁用所有编辑和审批操作

#### Scenario: 历史数据不受影响
- **GIVEN** 系统中存在旧的"待打款"报销单
- **WHEN** 系统升级或重启
- **THEN** 旧报销单的状态 SHALL 保持不变
- **AND** 只有新上传凭证的报销单才会变为"已完成"
- **AND** 用户可以继续为旧报销单上传凭证

### Requirement: 数据字典与枚举一致性
系统 SHALL 确保数据字典和枚举定义的状态值保持完全一致。

#### Scenario: 数据字典配置
- **GIVEN** 系统使用数据字典管理报销状态
- **WHEN** 添加新的"已完成"状态
- **THEN** 数据字典中 SHALL 添加对应的字典项
- **AND** 字典项的 value SHALL 为 `completed`
- **AND** 字典项的 label SHALL 为 `已完成`
- **AND** 字典项的颜色 SHALL 为绿色

#### Scenario: 枚举定义同步
- **GIVEN** 后端使用枚举定义报销状态
- **WHEN** 数据字典添加新状态
- **THEN** 后端枚举 SHALL 同步添加 `COMPLETED` 值
- **AND** 枚举的 code SHALL 为 `completed`
- **AND** 枚举的 name SHALL 为 `已完成`
- **AND** 前端和后端 SHALL 使用相同的状态值
