# 数据库设置说明

本文件说明如何设置和初始化 OA 系统的数据库。

## 前置条件

1. 安装 MySQL 8.0 或更高版本
2. 确保MySQL服务正在运行
3. 安装 Redis（用于验证码缓存）

## 数据库创建

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS oa_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE oa_system;
```

## 导入表结构

数据库表结构SQL文件位于：`数据库和表SQL/` 目录

请按照以下顺序执行SQL文件：

1. 执行认证相关的表创建脚本（已包含在项目中）

## 表结构说明

### 认证模块表 (auth)

1. **auth_user** - 用户表
   - 存储用户认证信息
   - 关联 sys_employee 表

2. **auth_user_session** - 用户会话表
   - 存储用户登录会话
   - 支持多设备同时登录

3. **auth_login_log** - 登录日志表
   - 记录所有登录尝试（成功和失败）
   - 用于安全审计

4. **auth_verification_code** - 验证码表
   - 存储邮箱/短信验证码
   - 支持密码重置、登录等场景

## 配置文件更新

更新 `src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/oa_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: your_username
    password: your_password

  # Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0

  # 邮件配置
  mail:
    host: smtp.example.com
    port: 587
    username: your-email@example.com
    password: your-email-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

jwt:
  secret: YourSecretKeyForJWTTokenGenerationShouldBeLongEnough  # 生产环境请修改为复杂的密钥
  expiration: 7200000      # 2小时
  refresh-expiration: 604800000  # 7天
```

## 初始化测试数据

### 创建测试用户

```sql
-- 插入测试用户（密码为：Admin123）
INSERT INTO auth_user (id, username, password, email, mobile, status, login_attempts, created_at, updated_at)
VALUES (
    'test-user-001',
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- Admin123
    'admin@example.com',
    '13800138000',
    'active',
    0,
    NOW(),
    NOW()
);
```

### 验证安装

运行以下SQL验证表是否创建成功：

```sql
-- 查看所有表
SHOW TABLES;

-- 查看用户表结构
DESC auth_user;

-- 查看测试用户
SELECT * FROM auth_user;
```

## Redis 配置

### Windows 安装 Redis

1. 下载 Redis for Windows：https://github.com/microsoftarchive/redis/releases
2. 解压并运行 `redis-server.exe`
3. 默认端口：6379

### Linux/Mac 安装 Redis

```bash
# Ubuntu/Debian
sudo apt-get install redis-server

# macOS
brew install redis

# 启动 Redis
redis-server
```

### 测试 Redis 连接

```bash
redis-cli ping
# 应该返回：PONG
```

## 邮件服务配置

### Gmail 配置示例

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password  # 使用应用专用密码，不是账户密码
```

### QQ 邮箱配置示例

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your-email@qq.com
    password: your-authorization-code  # 使用授权码
```

## 启动应用

完成以上配置后，运行应用：

```bash
mvn spring-boot:run
```

或者使用 IDE 运行 `OaSystemBackendApplication.java`

## 测试 API

应用启动后，可以测试以下 API：

### 1. 获取验证码
```bash
curl -X GET http://localhost:8080/api/auth/captcha
```

### 2. 用户登录
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin123"
  }'
```

### 3. 刷新令牌
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "your-refresh-token"
  }'
```

## 常见问题

### 1. 数据库连接失败
- 检查 MySQL 服务是否运行
- 验证用户名和密码
- 确认数据库名称正确

### 2. Redis 连接失败
- 确认 Redis 服务正在运行
- 检查 Redis 端口（默认 6379）
- 验证 Redis 密码配置

### 3. 邮件发送失败
- 检查 SMTP 服务器配置
- 确认使用正确的端口和认证方式
- 某些邮箱需要使用应用专用密码/授权码

### 4. JWT 验证失败
- 确保 jwt.secret 配置正确
- 检查 token 是否过期

## 生产环境建议

1. **修改默认密钥**：
   - 更改 JWT secret
   - 使用强密码策略

2. **数据库安全**：
   - 创建专用的数据库用户
   - 限制数据库访问权限
   - 启用 SSL 连接

3. **Redis 安全**：
   - 设置 Redis 密码
   - 限制 Redis 网络访问

4. **日志管理**：
   - 配置日志轮转
   - 敏感信息脱敏

5. **备份策略**：
   - 定期备份数据库
   - 保留备份历史
