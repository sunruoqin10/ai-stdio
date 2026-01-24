# expense Specification

## Purpose
TBD - created by archiving change update-expense-payment-status-labels. Update Purpose after archive.
## Requirements
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

### Requirement: 打款凭证上传流程
用户上传打款凭证时，系统 SHALL 先进行本地预览，仅在用户确认后才上传图片到服务器。

#### Scenario: 选择图片时只做本地预览
- **GIVEN** 用户在"打款管理"页面点击"上传凭证"按钮
- **AND** 上传对话框已打开
- **WHEN** 用户选择了一张图片
- **THEN** 系统 SHALL 在对话框中显示图片的本地预览
- **AND** 系统 SHALL 不会将图片上传到服务器
- **AND** 系统 SHALL 保存原始文件对象

#### Scenario: 点击确认上传后才上传到服务器
- **GIVEN** 用户已选择图片并看到本地预览
- **WHEN** 用户点击"确认上传"按钮
- **THEN** 系统 SHALL 将图片上传到服务器
- **AND** 上传成功后获取图片URL
- **AND** 调用后端API保存凭证URL到数据库
- **AND** 两个步骤都成功后才关闭对话框并刷新列表

#### Scenario: 取消上传不应产生服务器文件
- **GIVEN** 用户选择了图片并看到本地预览
- **WHEN** 用户点击"取消"按钮或关闭对话框
- **THEN** 系统 SHALL 关闭对话框
- **AND** 服务器上 SHALL 不存在该图片文件

#### Scenario: 上传失败时的错误处理
- **GIVEN** 用户点击"确认上传"按钮
- **WHEN** 图片上传到服务器失败
- **THEN** 系统 SHALL 显示错误提示信息
- **AND** 对话框 SHALL 保持打开状态
- **AND** 用户可以重新选择图片或重试

### Requirement: 打款凭证上传参数正确性
系统 SHALL 在调用上传打款凭证API时，传递正确的报销单号作为参数。

#### Scenario: 上传打款凭证时传递报销单号
- **GIVEN** 用户在"打款管理"页面点击"上传凭证"按钮
- **AND** 选择了要上传的打款凭证图片
- **WHEN** 用户点击"确认上传"按钮
- **THEN** 系统 SHALL 传递报销单号（expenseId）给后端API
- **AND** 系统 SHALL 不传递支付记录ID（PaymentRecord.id）
- **AND** 后端 SHALL 能够成功找到对应的报销单
- **AND** 凭证URL SHALL 被正确保存到数据库

#### Scenario: 参数验证失败时的错误处理
- **GIVEN** 用户在"打款管理"页面点击"上传凭证"按钮
- **AND** 打款记录缺少有效的报销单号
- **WHEN** 用户点击"确认上传"按钮
- **THEN** 系统 SHALL 显示"无效的打款记录"错误提示
- **AND** 对话框 SHALL 保持打开状态
- **AND** 系统 SHALL 不调用后端API

#### Scenario: 上传成功后的正确反馈
- **GIVEN** 用户点击"确认上传"按钮
- **AND** 传递了正确的报销单号
- **WHEN** 后端成功保存凭证URL
- **THEN** 系统 SHALL 显示"凭证上传成功"提示
- **AND** 对话框 SHALL 关闭
- **AND** 打款列表 SHALL 刷新以显示最新数据

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

