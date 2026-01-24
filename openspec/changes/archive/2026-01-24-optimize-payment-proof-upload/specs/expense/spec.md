# expense Specification

## MODIFIED Requirements

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
