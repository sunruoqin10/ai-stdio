# 登录认证模块开发规范

> **模块类型**: 核心基础
> **复杂度**: ⭐⭐⭐⭐ (4星)
> **创建日期**: 2026-01-10
> **最后更新**: 2026-01-10

---

## 📋 文档目录

本模块采用**三层架构规范**,分为三个独立的文档:

- **[第一层: 功能需求规范](./login_Functional.md)** - 描述"做什么"
- **[第二层: 技术实现规范](./login_Technical.md)** - 描述"怎么做"
- **[第三层: UI/UX设计规范](./login_Design.md)** - 描述"长什么样"

---

## 🎯 模块概述

### 功能简介

登录认证模块是OA系统的核心基础模块,提供用户身份认证、会话管理、权限验证等安全功能。支持多种登录方式、记住密码、找回密码、自动登录等功能,确保系统安全性和用户体验的平衡。

### 核心功能

- ✅ 账号密码登录(支持员工编号/邮箱/手机号)
- ✅ JWT Token认证与会话管理
- ✅ 记住我功能(7天免登录)
- ✅ 找回密码(邮箱/手机验证)
- ✅ 图形验证码(防机器人)
- ✅ 登录安全防护(错误次数限制、IP限流)
- ⏳ 异地登录检测(P2)

### 技术栈

- **前端**: Vue 3 + TypeScript + Element Plus + Pinia + Vue Router
- **后端**: Node.js + Express + TypeScript
- **数据库**: MySQL + Redis
- **认证**: JWT (JSON Web Token)
- **加密**: Bcrypt (密码加密)

---

## 📚 如何使用本文档

### 按角色阅读

**产品经理/业务分析师**:
- 重点阅读: [第一层: 功能需求规范](./login_Functional.md)
- 关注: 用户故事、功能清单、业务规则

**UI/UX设计师**:
- 重点阅读: [第一层](./login_Functional.md)、[第三层](./login_Design.md)
- 关注: 交互流程、页面布局、样式规范

**前端开发工程师**:
- 重点阅读: [第二层](./login_Technical.md)、[第三层](./login_Design.md)
- 关注: 数据结构、API接口、组件选择、交互规范

**后端开发工程师**:
- 重点阅读: [第一层](./login_Functional.md)、[第二层](./login_Technical.md)
- 关注: 业务规则、数据结构、API接口、安全机制

**测试工程师**:
- 重点阅读: [第一层](./login_Functional.md)、[第二层](./login_Technical.md)
- 关注: 业务规则、验证规则、错误处理

---

## 🚀 快速开始

### 1. 理解业务需求

首先阅读 [login_Functional.md](./login_Functional.md),理解登录模块的业务需求:

- 核心用户故事
- 功能清单(P0/P1/P2)
- 交互流程
- 业务规则

### 2. 了解技术实现

然后阅读 [login_Technical.md](./login_Technical.md),了解技术实现方案:

- 数据结构(TypeScript类型定义、数据库设计)
- API接口(请求/响应格式、错误码)
- 验证规则(前端/后端验证)
- 安全机制(密码加密、Token管理、防暴力破解)

### 3. 参考UI设计

最后阅读 [login_Design.md](./login_Design.md),参考UI/UX设计:

- 页面布局(登录页、找回密码页、完成页)
- 组件选择(Element Plus组件、自定义组件)
- 交互规范(加载状态、错误处理、操作反馈)
- 样式规范(颜色、字体、间距、圆角、阴影)

### 4. AI辅助开发

使用以下提示词让AI辅助开发:

```bash
# 生成登录功能
"根据 specs/auth/login/ 三层架构规范,生成完整的登录功能模块:
- 参考第一层理解业务需求
- 参考第二层实现技术方案
- 参考第三层实现UI设计
- 使用 Vue 3 + TypeScript + Element Plus
- 参考路径: src/modules/auth/"

# 生成登录表单组件
"根据 specs/auth/login/login_Design.md 第2.2.1节,生成完整的登录表单组件:
- 实现账号密码输入
- 实现记住我功能
- 实现忘记密码链接
- 实现表单验证"

# 生成登录API
"根据 specs/auth/login/login_Technical.md 第2节,实现登录相关API:
- POST /api/auth/login - 用户登录
- POST /api/auth/logout - 退出登录
- POST /api/auth/refresh - 刷新Token
- POST /api/auth/send-code - 发送验证码
- POST /api/auth/reset-password - 重置密码"
```

---

## 📂 文件结构

参考实现后的文件结构:

```
src/modules/auth/
├── types/
│   └── index.ts              # TypeScript类型定义
├── api/
│   └── index.ts              # API接口封装
├── store/
│   └── index.ts              # Pinia Store(用户状态、Token管理)
├── components/
│   ├── LoginForm.vue         # 登录表单组件
│   └── ResetPasswordForm.vue # 重置密码表单组件
└── views/
    ├── Login.vue             # 登录页面
    └── ResetPassword.vue     # 重置密码页面
```

---

## 🔐 安全要点

### 密码安全

- ✅ 使用Bcrypt加密(cost=10)
- ✅ 每个密码独立盐值
- ✅ 密码复杂度要求(8-20位,包含大小写字母和数字)
- ✅ 密码历史验证(不能与最近3次相同)
- ❌ 不在日志中记录明文密码

### Token安全

- ✅ Access Token存储在HttpOnly Cookie
- ✅ Refresh Token存储在LocalStorage + 数据库
- ✅ Token有效期: Access Token 2小时,Refresh Token 7天
- ✅ Token即将过期前自动刷新
- ✅ Token签名使用HS256算法

### 防暴力破解

- ✅ 密码连续错误5次锁定账号30分钟
- ✅ 同一IP 1小时内失败10次临时封禁
- ✅ 连续失败3次出现图形验证码
- ✅ 验证码有效期5分钟

### 其他安全措施

- ✅ 输入过滤(防XSS攻击)
- ✅ CSRF Token验证
- ✅ 参数化查询(防SQL注入)
- ✅ 记录登录日志(审计追溯)

---

## 📊 功能优先级

### P0 (核心功能 - 必须实现)

- 账号密码登录
- JWT Token认证和验证
- 自动刷新Token
- 基础安全防护(错误次数限制)

### P1 (重要功能 - 应该实现)

- 记住我功能
- 退出登录
- 找回密码
- 图形验证码
- 登录日志记录

### P2 (增强功能 - 可选实现)

- 异地登录检测
- 二次验证(2FA)
- 多设备管理
- 登录行为分析

---

## 🧪 测试要点

### 功能测试

- 正常登录流程
- 错误账号登录
- 错误密码登录
- 账号锁定后登录
- Token过期后自动刷新
- 退出登录流程
- 找回密码完整流程

### 安全测试

- SQL注入测试
- XSS攻击测试
- CSRF攻击测试
- 暴力破解测试
- 会话劫持测试
- Token伪造测试

### 性能测试

- 并发登录(1000用户同时登录)
- 登录响应时间(目标 < 2秒)
- Token验证性能(目标 < 50ms)
- 密码加密性能(目标 < 100ms)

### 兼容性测试

- 主流浏览器(Chrome、Firefox、Safari、Edge)
- 移动端浏览器(iOS、Android)
- 不同分辨率设备

---

## 🔗 相关文档

- [三层架构说明](../../_template/三层架构说明.md)
- [规范模板v2](../../_template/spec-template-v2.md)
- [员工管理模块](../../core/employee/) - 参考模块

---

## 📝 更新日志

| 版本 | 日期 | 更新内容 | 更新人 |
|------|------|---------|--------|
| v1.0.0 | 2026-01-10 | 初始版本,创建完整的三层架构规范 | AI开发助手 |

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-10
