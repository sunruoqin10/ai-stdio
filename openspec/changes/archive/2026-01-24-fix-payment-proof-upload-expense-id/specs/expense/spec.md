# expense Specification

## MODIFIED Requirements

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
