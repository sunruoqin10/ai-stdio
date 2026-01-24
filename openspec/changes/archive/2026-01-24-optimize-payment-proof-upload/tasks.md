# 实施任务

## 1. 修改上传组件配置
- [x] 1.1 在 `el-upload` 组件添加 `:auto-upload="false"` 属性
- [x] 1.2 移除或注释 `:action` 属性
- [x] 1.3 添加 `:on-change` 事件处理本地预览
- [x] 1.4 修改 `:on-success` 改为手动上传成功后的处理

## 2. 实现本地预览功能
- [x] 2.1 创建 `handleFileChange` 函数处理文件选择
- [x] 2.2 使用 `URL.createObjectURL()` 生成本地预览URL
- [x] 2.3 在 `uploadForm.proof` 中保存本地预览URL
- [x] 2.4 在 `uploadForm.file` 中保存原始File对象

## 3. 实现手动上传逻辑
- [x] 3.1 创建 `uploadImageToServer` 函数
- [x] 3.2 使用 FormData 上传文件
- [x] 3.3 上传成功后获取服务器返回的URL
- [x] 3.4 调用后端API保存凭证URL

## 4. 修改 `handleConfirmUpload` 函数
- [x] 4.1 检查是否选择了文件
- [x] 4.2 先调用 `uploadImageToServer` 上传图片
- [x] 4.3 上传成功后再调用 `expenseStore.uploadPaymentProof` 保存URL
- [x] 4.4 两个步骤都成功后才关闭对话框

## 5. 添加错误处理
- [x] 5.1 图片上传失败的错误提示
- [x] 5.2 API调用失败的错误提示
- [x] 5.3 上传过程中禁用"确认上传"按钮
- [x] 5.4 显示上传进度（可选）

## 6. 验证
- [x] 6.1 验证选择图片后只做本地预览（网络无上传）
- [x] 6.2 验证点击"确认上传"后才上传到服务器
- [x] 6.3 验证上传成功后正确保存到数据库
- [x] 6.4 验证取消上传不会产生服务器文件
