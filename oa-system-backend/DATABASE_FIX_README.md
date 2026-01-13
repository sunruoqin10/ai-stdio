# OA系统前后端集成 - 数据库修复指南

## 问题说明

后端代码使用MyBatis Plus，需要以下条件才能正常工作：
1. 数据库表必须包含 `is_deleted` 字段（逻辑删除）
2. Java实体类字段名必须与数据库列名匹配
3. 用户密码必须使用BCrypt加密存储

## 已修复的问题

### 1. ✅ Java实体类字段映射已修复

文件：`oa-system-backend/src/main/java/com/example/oa_system_backend/module/auth/entity/AuthUser.java`

已添加 `@TableField` 注解，使Java字段名正确映射到数据库列名：
- `loginAttempts` → `failed_login_attempts`
- `lastLoginTime` → `last_login_at`
- `lastLoginIp` → `last_login_ip`
- `passwordChangedAt` → `password_changed_at`
- `createdAt` → `created_at`
- `updatedAt` → `updated_at`
- `isDeleted` → `is_deleted`

### 2. ✅ 数据库字段已验证

所有认证相关表都包含 `is_deleted` 字段：
- `auth_user.is_deleted` ✓
- `auth_login_log.is_deleted` ✓
- `auth_user_session.is_deleted` ✓
- `auth_verification_code.is_deleted` ✓

## 需要手动执行的步骤

### 更新用户密码为BCrypt格式

由于MCP MySQL工具的限制，请手动执行以下任一方式：

#### 方式1：双击运行批处理文件（推荐）

```
建库和表SQL\execute_add_is_deleted.bat
```

这个脚本会自动：
1. 更新所有测试用户的密码为BCrypt格式
2. 为所有表添加 `is_deleted` 字段（如果不存在）

#### 方式2：在MySQL客户端执行SQL

执行文件：
```
建库和表SQL\update_test_passwords.sql
```

或在MySQL Workbench中运行：

```sql
USE oa_system;

-- 更新密码为BCrypt格式（原始密码: 12345678）
UPDATE auth_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi' WHERE username = 'zhangsan';
UPDATE auth_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi' WHERE username = 'lisi';
UPDATE auth_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi' WHERE username = 'wangwu';
UPDATE auth_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi' WHERE username = 'zhaoliu';
```

## 测试账号

执行完上述SQL后，可以使用以下账号登录：

| 账号 | 密码 | 邮箱 |
|------|------|------|
| zhangsan | 12345678 | zhangsan@company.com |
| lisi | 12345678 | lisi@company.com |
| wangwu | 12345678 | wangwu@company.com |
| zhaoliu | 12345678 | zhaoliu@company.com |

## 启动应用

### 1. 启动后端服务器

```bash
cd oa-system-backend
mvn spring-boot:run
```

后端将在 `http://localhost:8080/api` 启动

### 2. 启动前端开发服务器

```bash
cd oa-system-frontend
npm run dev
```

前端将在 `http://localhost:3000` 启动，自动代理API请求到后端

### 3. 访问应用

在浏览器中打开：`http://localhost:3000`

## 验证集成

1. 前端登录页面输入测试账号密码
2. 点击登录按钮
3. 成功登录后应跳转到主页面
4. 检查浏览器网络请求，可以看到：
   - POST `/api/auth/login` - 登录请求
   - 返回JWT Token
   - 后续请求自动携带Token

## API端点

基础URL: `http://localhost:8080/api`

### 认证接口

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 退出登录
- `POST /api/auth/refresh` - 刷新Token
- `GET /api/auth/captcha` - 获取验证码
- `POST /api/auth/send-code` - 发送验证码
- `POST /api/auth/reset-password` - 重置密码
- `GET /api/auth/sessions?page=1&size=10` - 获取活跃会话
- `DELETE /api/auth/sessions/{id}` - 撤销会话
- `GET /api/auth/login-logs` - 获取登录日志

## 配置说明

### 前端代理配置

`vite.config.ts`:
```typescript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

### 后端配置

`application.yml`:
```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/oa_system
    username: root
    password: root
```

### CORS配置

后端已配置完整的CORS支持（`SecurityConfig.java`），允许前端跨域请求。

## 故障排查

### 问题1: "Unknown column 'is_deleted' in 'where clause'"

**原因**: 表缺少 `is_deleted` 字段

**解决**: 运行 `建库和表SQL\execute_add_is_deleted.bat`

### 问题2: "Unknown column 'login_attempts' in 'field list'"

**原因**: Java字段名与数据库列名不匹配

**解决**: 已修复 `AuthUser.java`，添加 `@TableField` 注解

### 问题3: "用户名或密码错误"

**原因**: 密码未使用BCrypt加密

**解决**: 运行 `建库和表SQL\update_test_passwords.sql`

### 问题4: 前端无法连接后端

**检查**:
1. 后端是否正常启动（端口8080）
2. 前端代理配置是否正确
3. 浏览器控制台是否有CORS错误

## 下一步

数据库修复完成后，前后端集成应该可以正常工作。如果还有问题，请检查：

1. 后端日志：`oa-system-backend/logs/`
2. 浏览器控制台（F12）的网络请求
3. MySQL数据是否正确更新

---

生成时间：2026-01-13
