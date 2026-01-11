# 登录认证数据库设计

> **对应前端规范**: [login_Technical.md](../../../oa-system-frontend-specs/auth/login/login_Technical.md)
> **数据库**: MySQL 8.0+
> **表前缀**: `auth_`
> **版本**: v1.0.0

---

## 数据库表设计

完整的SQL已在前端规格中定义,包含以下表:

| 表名 | 说明 |
|------|------|
| auth_user | 用户账号表 |
| auth_user_session | 用户会话表 |
| auth_login_log | 登录日志表 |

### 安全特性

- 密码使用 bcrypt 哈希(10轮)
- 登录失败5次锁定30分钟
- JWT Token有效期2小时
- Refresh Token有效期7天
- Session自动过期管理

### 初始化账号

- 默认账号: `zhangsan`, `lisi`, `wangwu`, `zhaoliu`
- 默认密码: `123456`
- 密码哈希: `$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`

**文档版本**: v1.0.0
**最后更新**: 2026-01-11
