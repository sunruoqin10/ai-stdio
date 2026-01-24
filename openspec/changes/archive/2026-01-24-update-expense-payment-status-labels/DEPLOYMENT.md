# 状态文案优化 - 部署指南

## 变更概述

本次变更优化了费用报销模块的状态显示文案，解决了"已打款"和"待打款"状态混淆的问题。

**修改内容**:
- 后端 `ExpenseStatus.PAID`: "已打款" → "待打款"
- 前端状态映射和提示信息同步更新

## 构建结果

✅ **后端构建成功**
- WAR包位置: `oa-system-backend/target/oa-system-backend-0.0.1-SNAPSHOT.war`
- 构建时间: 16.4秒
- 状态: BUILD SUCCESS

✅ **前端构建成功**
- 构建目录: `oa-system-frontend/dist/`
- 构建时间: 7.83秒
- 状态: ✓ built in 7.83s

## 部署步骤

### 1. 后端部署

```bash
# 停止当前运行的后端服务
# Windows
net stop oa-system-backend

# 或 Linux
systemctl stop oa-system-backend

# 备份当前版本
cp oa-system-backend.war oa-system-backend.war.backup.$(date +%Y%m%d_%H%M%S)

# 部署新版本
cp target/oa-system-backend-0.0.1-SNAPSHOT.war /path/to/deployment/oa-system-backend.war

# 启动服务
# Windows
net start oa-system-backend

# 或 Linux
systemctl start oa-system-backend

# 或直接运行
java -jar oa-system-backend.war
```

### 2. 前端部署

```bash
# 备份当前版本
cp -r /path/to/nginx/html/oa /path/to/nginx/html/oa.backup.$(date +%Y%m%d_%H%M%S)

# 部署新版本
cp -r dist/* /path/to/nginx/html/oa/

# 或使用 rsync
rsync -av --delete dist/ /path/to/nginx/html/oa/

# 重启 Nginx (如果需要)
# Linux
nginx -s reload

# Windows
# 重启 Nginx 服务
```

### 3. 数据库迁移

✅ **无需数据库迁移** - 本次变更只涉及显示文案，不涉及数据库结构变更

## 验证清单

### 后端验证
- [ ] 服务启动成功
- [ ] 端口 8080 正常监听
- [ ] 日志无错误信息

### 前端验证
- [ ] 页面加载正常
- [ ] 静态资源加载正常

### 功能验证
- [ ] **我的报销**页面：
  - 状态为 `paid` 的报销单显示"待打款"
  - 状态提示显示"审批已完成，等待财务打款"

- [ ] **打款管理**页面：
  - 状态为 `pending` 的打款记录显示"待打款"
  - 状态为 `completed` 的打款记录显示"已完成"

- [ ] **一致性检查**：
  - 同一个报销单在两个页面的状态描述保持一致

### 测试账号
- 报销单号: EXP202601230001
- 预期状态: "待打款"（而非"已打款"）

## 回滚方案

如果部署后出现问题，按以下步骤回滚：

```bash
# 后端回滚
cp oa-system-backend.war.backup.YYYYMMDD_HHMMSS oa-system-backend.war
systemctl restart oa-system-backend

# 前端回滚
cp -r /path/to/nginx/html/oa.backup.YYYYMMDD_HHMMSS/* /path/to/nginx/html/oa/
nginx -s reload
```

## 注意事项

1. **构建警告**:
   - 前端构建时有 TypeScript 类型检查警告
   - 这些警告不影响功能，属于项目技术债务
   - 建议后续修复这些类型问题

2. **数据库凭证**:
   - 生产环境请使用环境变量配置数据库连接
   - 不要在 application.yml 中硬编码敏感信息

3. **JWT密钥**:
   - 生产环境必须更换默认 JWT 密钥
   - 配置项: `jwt.secret`

## 联系信息

如有部署问题，请联系：
- 开发团队
- 技术支持

## 附录

### OpenSpec 变更记录
- 变更ID: `update-expense-payment-status-labels`
- 提案文件: `openspec/changes/update-expense-payment-status-labels/`
- 归档命令: `openspec archive update-expense-payment-status-labels`
