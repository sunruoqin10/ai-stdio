# 部署指南 - 打款凭证上传流程优化

**变更ID**: `optimize-payment-proof-upload`
**变更类型**: 用户体验优化
**影响范围**: 前端
**部署风险**: 🟢 低风险

---

## 📋 变更概述

本次变更优化了打款凭证的上传流程，将图片上传动作从"选择文件时"延迟到"点击确认上传按钮后"。

### 主要改进
1. ✅ 选择图片时只做本地预览，无网络请求
2. ✅ 点击"确认上传"后才上传到服务器
3. ✅ 防止无效上传造成服务器存储浪费
4. ✅ 优化内存管理，防止 blob URL 泄漏

---

## 🎯 受影响的文件

### 前端文件
```
oa-system-frontend/src/modules/expense/components/PaymentManagement.vue
```

**变更内容**:
- 修改 `el-upload` 组件配置（禁用自动上传）
- 新增 `handleFileChange` 函数（本地预览）
- 新增 `uploadImageToServer` 函数（手动上传）
- 修改 `handleConfirmUpload` 函数（两步上传流程）
- 新增 `handleCancelUpload` 函数（清理资源）

---

## 🚀 部署步骤

### 1. 前端部署

#### 1.1 构建前端
```bash
cd oa-system-frontend

# 安装依赖（如果需要）
npm install

# 构建生产版本
npm run build

# 或使用 Vite 直接构建（跳过类型检查）
npx vite build --mode production
```

#### 1.2 验证构建产物
```bash
# 检查构建产物
ls -lh dist/

# 预期输出：
# index.html
# assets/ (包含所有 JS 和 CSS 文件)
```

#### 1.3 部署到服务器
```bash
# 方法 1: 复制到 Web 服务器
cp -r dist/* /path/to/web/server/

# 方法 2: 使用 scp 上传到远程服务器
scp -r dist/* user@server:/path/to/web/root/

# 方法 3: 使用 Docker 镜像
docker build -t oa-system-frontend .
docker push your-registry/oa-system-frontend:latest
```

### 2. 后端部署

⚠️ **注意**: 本次变更不涉及后端代码修改，无需重新部署后端。

但需要确保以下接口正常工作：
- `POST /api/upload` - 文件上传接口
- `POST /api/expense/payment/{id}/proof` - 保存凭证URL接口

### 3. 数据库变更

⚠️ **注意**: 本次变更不涉及数据库结构修改，无需执行数据库迁移。

---

## 🧪 部署验证

### 功能测试清单

#### 测试 1: 本地预览功能
**步骤**:
1. 打开"打款管理"页面
2. 点击"上传凭证"按钮
3. 选择一张图片文件
4. 观察是否立即显示预览

**预期结果**:
- ✅ 对话框中显示图片预览
- ✅ 打开浏览器开发者工具 → Network 标签
- ✅ 确认没有 `/api/upload` 请求（说明未上传到服务器）

#### 测试 2: 确认上传功能
**步骤**:
1. 继续上述测试
2. 点击"确认上传"按钮
3. 观察上传过程

**预期结果**:
- ✅ 按钮显示 loading 状态
- ✅ Network 标签显示 `/api/upload` 请求
- ✅ 上传成功后显示"凭证上传成功"提示
- ✅ 对话框自动关闭
- ✅ 列表自动刷新

#### 测试 3: 取消上传功能
**步骤**:
1. 点击"上传凭证"按钮
2. 选择一张图片
3. 点击"取消"按钮

**预期结果**:
- ✅ 对话框关闭
- ✅ Network 标签确认没有 `/api/upload` 请求
- ✅ 服务器上不存在该图片文件

#### 测试 4: 错误处理
**步骤**:
1. 点击"上传凭证"按钮
2. 选择一张图片
3. 模拟网络故障（断开网络或后端服务停止）
4. 点击"确认上传"按钮

**预期结果**:
- ✅ 显示错误提示信息
- ✅ 对话框保持打开状态
- ✅ 可以重新选择图片或重试

### 性能测试

#### 内存泄漏检查
**步骤**:
1. 打开浏览器开发者工具 → Performance/Memory
2. 记录初始内存
3. 执行 10 次"选择图片 → 取消"操作
4. 检查内存是否释放

**预期结果**:
- ✅ 内存使用稳定，无明显增长
- ✅ blob URL 已正确清理

---

## 📊 回滚计划

### 回滚条件
- 发现严重 bug 影响用户使用
- 上传功能无法正常工作
- 性能问题导致用户投诉

### 回滚步骤

#### 前端回滚
```bash
# 方法 1: Git 回滚
cd oa-system-frontend
git log --oneline -10  # 查看最近提交
git revert <commit-hash>
npm run build
# 重新部署

# 方法 2: 恢复备份
cp -r /path/to/backup/dist/* /path/to/web/server/

# 方法 3: 切换到之前的 Docker 镜像
docker pull your-registry/oa-system-frontend:previous-tag
docker tag your-registry/oa-system-frontend:previous-tag oa-system-frontend:latest
# 重新部署
```

#### 验证回滚
- [ ] 确认旧版本上传功能正常工作
- [ ] 确认用户可以正常访问系统
- [ ] 监控错误日志

---

## ⚠️ 注意事项

### 兼容性
- ✅ 向后兼容，不影响现有功能
- ✅ 不改变 API 接口
- ✅ 不影响数据库结构

### 用户体验
- 📋 建议在部署前发布公告，告知用户上传流程的变化
- 📋 可以在帮助文档中更新操作说明

### 监控指标
部署后需要监控：
- 上传成功率
- 上传失败率
- 平均上传时间
- 用户反馈

---

## 📞 支持联系

如有部署问题，请联系：
- **技术支持**: [您的邮箱/联系方式]
- **紧急联系**: [紧急联系电话]

---

## 📝 变更记录

| 日期 | 版本 | 变更内容 | 操作人 |
|------|------|----------|--------|
| 2026-01-24 | 1.0.0 | 初始版本 | Claude Code |

---

**部署状态**: ⏳ 待部署
**最后更新**: 2026-01-24
