# Change: 优化打款凭证上传流程

## Why
当前的打款凭证上传流程存在用户体验问题：
- 用户选择图片后立即上传到服务器
- 但如果用户改变主意或关闭对话框，已上传的图片会造成服务器存储浪费
- 流程不够清晰：用户不知道选择图片后是否已经上传成功

## What Changes
优化打款凭证上传流程，将图片上传动作延迟到用户点击"确认上传"按钮之后：
- 选择图片时：只做本地预览，不上传到服务器
- 点击"确认上传"时：才将图片上传到服务器，然后调用后端API保存

## Impact
- **受影响的规范**: expense（打款管理功能）
- **受影响的代码**:
  - `oa-system-frontend/src/modules/expense/components/PaymentManagement.vue`
  - `oa-system-frontend/src/modules/expense/api/index.ts` (可能需要添加上传API)
- **用户体验**: 更清晰的上传流程，避免无效上传造成的存储浪费
