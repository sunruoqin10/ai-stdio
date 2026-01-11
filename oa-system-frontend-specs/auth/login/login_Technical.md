# 登录技术实现规范

> **规范类型**: 第二层 - 技术实现规范
> **模块**: 登录认证
> **版本**: v1.0.0
> **创建日期**: 2026-01-10

---

## 📋 目录

- [1. 数据结构](#1-数据结构)
- [2. API接口](#2-api接口)
- [3. 验证规则](#3-验证规则)
- [4. 算法实现](#4-算法实现)
- [5. 安全机制](#5-安全机制)

---

## 1. 数据结构

### 1.1 TypeScript类型定义

```typescript
/**
 * 登录请求参数
 */
interface LoginRequest {
  /** 账号(员工编号/邮箱/手机号) */
  username: string
  /** 密码 */
  password: string
  /** 验证码(连续失败3次后必填) */
  captcha?: string
  /** 验证码Key */
  captchaKey?: string
  /** 是否记住登录 */
  remember?: boolean
}

/**
 * 登录响应数据
 */
interface LoginResponse {
  /** 访问Token */
  accessToken: string
  /** 刷新Token */
  refreshToken: string
  /** Token类型(固定为Bearer) */
  tokenType: 'Bearer'
  /** 过期时间(秒) */
  expiresIn: number
  /** 用户信息 */
  userInfo: UserInfo
}

/**
 * 用户信息
 */
interface UserInfo {
  /** 用户ID */
  id: string
  /** 员工编号 */
  employeeNo: string
  /** 姓名 */
  name: string
  /** 邮箱 */
  email: string
  /** 手机号 */
  phone: string
  /** 头像 */
  avatar?: string
  /** 部门ID */
  departmentId: string
  /** 部门名称 */
  departmentName: string
  /** 职位 */
  position: string
  /** 角色列表 */
  roles: Role[]
  /** 权限列表 */
  permissions: string[]
}

/**
 * 角色信息
 */
interface Role {
  /** 角色ID */
  id: string
  /** 角色名称 */
  name: string
  /** 角色编码 */
  code: string
  /** 角色类型 */
  type: 'system' | 'department' | 'custom'
}

/**
 * Token刷新请求
 */
interface RefreshTokenRequest {
  /** 刷新Token */
  refreshToken: string
}

/**
 * Token刷新响应
 */
interface RefreshTokenResponse {
  /** 新的访问Token */
  accessToken: string
  /** 新的刷新Token */
  refreshToken: string
  /** 过期时间(秒) */
  expiresIn: number
}

/**
 * 找回密码请求
 */
interface ResetPasswordRequest {
  /** 找回方式(email/mobile) */
  type: 'email' | 'mobile'
  /** 邮箱或手机号 */
  account: string
  /** 验证码 */
  code: string
  /** 新密码 */
  newPassword: string
}

/**
 * 发送验证码请求
 */
interface SendCodeRequest {
  /** 发送类型(email/mobile) */
  type: 'email' | 'mobile'
  /** 邮箱或手机号 */
  account: string
  /** 验证类型(forgot_password/login) */
  scene: 'forgot_password' | 'login'
}

/**
 * 登录日志
 */
interface LoginLog {
  /** 日志ID */
  id: string
  /** 用户ID */
  userId: string
  /** 用户名 */
  username: string
  /** 登录IP */
  ip: string
  /** 登录地点 */
  location?: string
  /** 设备信息 */
  device: string
  /** 浏览器 */
  browser: string
  /** 操作系统 */
  os: string
  /** 登录状态 */
  status: 'success' | 'failed'
  /** 失败原因 */
  failureReason?: string
  /** 登录时间 */
  loginTime: string
}

/**
 * 用户会话
 */
interface UserSession {
  /** 会话ID */
  id: string
  /** 用户ID */
  userId: string
  /** Access Token */
  accessToken: string
  /** Refresh Token */
  refreshToken: string
  /** 过期时间 */
  expiresAt: string
  /** 登录IP */
  ip: string
  /** 设备信息 */
  device: string
  /** 登录时间 */
  loginTime: string
  /** 最后活跃时间 */
  lastActiveTime: string
}
```

### 1.2 字段说明表

| 字段名 | 类型 | 必填 | 默认值 | 说明 | 验证规则 |
|-------|------|------|--------|------|---------|
| username | string | ✅ | - | 账号 | 长度3-50字符 |
| password | string | ✅ | - | 密码 | 长度8-20字符 |
| captcha | string | 条件 | - | 验证码 | 长度4-6字符 |
| remember | boolean | ❌ | false | 记住我 | - |
| accessToken | string | ✅ | - | 访问Token | JWT格式 |
| refreshToken | string | ✅ | - | 刷新Token | UUID格式 |
| expiresIn | number | ✅ | - | 过期时间 | 秒数(7200) |

### 1.3 数据库设计

```sql
-- 用户表(假设基于employees表)
CREATE TABLE users (
  id VARCHAR(20) PRIMARY KEY COMMENT '用户ID(关联employees.id)',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希(bcrypt)',
  salt VARCHAR(100) COMMENT '密码盐值',
  email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
  mobile VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
  status ENUM('active', 'locked', 'disabled') DEFAULT 'active' COMMENT '账号状态',
  login_attempts INT DEFAULT 0 COMMENT '登录失败次数',
  locked_until TIMESTAMP NULL COMMENT '锁定到期时间',
  last_login_time TIMESTAMP NULL COMMENT '最后登录时间',
  last_login_ip VARCHAR(50) COMMENT '最后登录IP',
  password_changed_at TIMESTAMP NULL COMMENT '密码最后修改时间',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_username (username),
  INDEX idx_email (email),
  INDEX idx_mobile (mobile),
  INDEX idx_status (status)
) COMMENT='用户表';

-- 用户会话表
CREATE TABLE user_sessions (
  id VARCHAR(64) PRIMARY KEY COMMENT '会话ID',
  user_id VARCHAR(20) NOT NULL COMMENT '用户ID',
  access_token VARCHAR(500) NOT NULL COMMENT '访问Token(JWT)',
  refresh_token VARCHAR(100) NOT NULL UNIQUE COMMENT '刷新Token',
  expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
  login_ip VARCHAR(50) COMMENT '登录IP',
  login_location VARCHAR(100) COMMENT '登录地点',
  device_info VARCHAR(200) COMMENT '设备信息',
  browser VARCHAR(100) COMMENT '浏览器',
  os VARCHAR(100) COMMENT '操作系统',
  login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  last_active_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',

  INDEX idx_user_id (user_id),
  INDEX idx_refresh_token (refresh_token),
  INDEX idx_expires_at (expires_at),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT='用户会话表';

-- 登录日志表
CREATE TABLE login_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
  user_id VARCHAR(20) COMMENT '用户ID',
  username VARCHAR(50) COMMENT '登录用户名',
  login_ip VARCHAR(50) COMMENT '登录IP',
  login_location VARCHAR(100) COMMENT '登录地点',
  device_info VARCHAR(200) COMMENT '设备信息',
  browser VARCHAR(100) COMMENT '浏览器',
  os VARCHAR(100) COMMENT '操作系统',
  status ENUM('success', 'failed') NOT NULL COMMENT '登录状态',
  failure_reason VARCHAR(200) COMMENT '失败原因',
  login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',

  INDEX idx_user_id (user_id),
  INDEX idx_status (status),
  INDEX idx_login_time (login_time),
  INDEX idx_login_ip (login_ip),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) COMMENT='登录日志表';

-- 验证码表
CREATE TABLE verification_codes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  type ENUM('email', 'mobile') NOT NULL COMMENT '类型',
  account VARCHAR(100) NOT NULL COMMENT '邮箱/手机号',
  scene ENUM('forgot_password', 'login', 'register') NOT NULL COMMENT '场景',
  code VARCHAR(10) NOT NULL COMMENT '验证码',
  expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
  used BOOLEAN DEFAULT FALSE COMMENT '是否已使用',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_type_account (type, account),
  INDEX idx_expires_at (expires_at)
) COMMENT='验证码表';

-- 密码历史表
CREATE TABLE password_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id VARCHAR(20) NOT NULL COMMENT '用户ID',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
  changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',

  INDEX idx_user_id (user_id),
  INDEX idx_changed_at (changed_at),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT='密码历史表';
```

---

## 2. API接口

### 2.1 RESTful API设计

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/auth/login | 用户登录 | 公开 |
| POST | /api/auth/logout | 退出登录 | 已认证 |
| POST | /api/auth/refresh | 刷新Token | 公开(使用RefreshToken) |
| POST | /api/auth/captcha | 获取验证码 | 公开 |
| POST | /api/auth/send-code | 发送验证码 | 公开 |
| POST | /api/auth/reset-password | 重置密码 | 公开 |
| GET | /api/auth/sessions | 获取活跃会话 | 已认证 |
| DELETE | /api/auth/sessions/:id | 删除会话 | 已认证 |
| GET | /api/auth/login-logs | 获取登录日志 | 已认证 |

### 2.2 接口详细定义

#### 2.2.1 用户登录

**请求**:
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "zhangsan",
  "password": "Password123",
  "captcha": "A1B2",
  "captchaKey": "captcha_123456",
  "remember": true
}
```

**响应**:
```typescript
interface LoginResponse {
  code: number                    // 200-成功
  message: string                 // "登录成功"
  data: {
    accessToken: string           // JWT Token
    refreshToken: string          // Refresh Token
    tokenType: 'Bearer'
    expiresIn: number             // 7200秒
    userInfo: UserInfo
  }
}
```

**示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "userInfo": {
      "id": "EMP20260109001",
      "employeeNo": "EMP20260109001",
      "name": "张三",
      "email": "zhangsan@company.com",
      "phone": "13800138000",
      "avatar": "https://example.com/avatar.jpg",
      "departmentId": "DEPT001",
      "departmentName": "技术部",
      "position": "软件工程师",
      "roles": [
        { "id": "1", "name": "普通员工", "code": "employee", "type": "custom" }
      ],
      "permissions": ["employee:view", "leave:apply"]
    }
  }
}
```

**错误响应**:
```json
// 账号不存在
{ "code": 1001, "message": "账号不存在,请联系管理员" }

// 密码错误
{ "code": 1002, "message": "密码错误,请重新输入", "data": { "attemptsLeft": 3 } }

// 账号已锁定
{ "code": 1003, "message": "账号已锁定,请30分钟后再试", "data": { "lockedUntil": "2026-01-10 15:30:00" } }

// 验证码错误
{ "code": 1004, "message": "验证码错误,请重新输入" }
```

#### 2.2.2 退出登录

**请求**:
```http
POST /api/auth/logout
Authorization: Bearer {accessToken}
```

**响应**:
```typescript
interface LogoutResponse {
  code: number                    // 200
  message: string                 // "退出成功"
}
```

#### 2.2.3 刷新Token

**请求**:
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**响应**:
```typescript
interface RefreshTokenResponse {
  code: number                    // 200
  message: string                 // "刷新成功"
  data: {
    accessToken: string
    refreshToken: string
    expiresIn: number
  }
}
```

**错误响应**:
```json
// RefreshToken无效或已过期
{ "code": 1005, "message": "登录已过期,请重新登录" }
```

#### 2.2.4 获取验证码

**请求**:
```http
GET /api/auth/captcha
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "captchaKey": "captcha_123456",
    "captchaImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
  }
}
```

#### 2.2.5 发送验证码

**请求**:
```http
POST /api/auth/send-code
Content-Type: application/json

{
  "type": "email",
  "account": "zhangsan@company.com",
  "scene": "forgot_password"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "验证码已发送,请注意查收"
}
```

**错误响应**:
```json
// 发送频率限制
{ "code": 1006, "message": "发送过于频繁,请1分钟后再试" }

// 账号不存在
{ "code": 1007, "message": "该账号未注册" }
```

#### 2.2.6 重置密码

**请求**:
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "type": "email",
  "account": "zhangsan@company.com",
  "code": "123456",
  "newPassword": "NewPassword123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "密码修改成功,请使用新密码登录"
}
```

#### 2.2.7 获取活跃会话

**请求**:
```http
GET /api/auth/sessions
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 2,
    "list": [
      {
        "id": "session_001",
        "device": "Windows PC - Chrome",
        "browser": "Chrome 120.0",
        "os": "Windows 10",
        "loginIp": "192.168.1.100",
        "loginLocation": "北京市",
        "loginTime": "2026-01-10 09:00:00",
        "lastActiveTime": "2026-01-10 14:30:00",
        "isCurrent": true
      },
      {
        "id": "session_002",
        "device": "iPhone - Safari",
        "browser": "Safari 17.0",
        "os": "iOS 17",
        "loginIp": "192.168.1.101",
        "loginLocation": "上海市",
        "loginTime": "2026-01-09 15:00:00",
        "lastActiveTime": "2026-01-09 18:00:00",
        "isCurrent": false
      }
    ]
  }
}
```

### 2.3 错误码定义

| 错误码 | 说明 | HTTP状态码 |
|-------|------|-----------|
| 200 | 成功 | 200 |
| 400 | 参数错误 | 400 |
| 401 | 未认证 | 401 |
| 403 | 无权限 | 403 |
| 1001 | 账号不存在 | 400 |
| 1002 | 密码错误 | 400 |
| 1003 | 账号已锁定 | 403 |
| 1004 | 验证码错误或已过期 | 400 |
| 1005 | Token无效或已过期 | 401 |
| 1006 | 验证码发送频率限制 | 429 |
| 1007 | 验证码错误次数过多 | 400 |
| 1008 | 密码不符合安全策略 | 400 |
| 1009 | 新密码不能与旧密码相同 | 400 |
| 1010 | 验证码错误 | 400 |

---

## 3. 验证规则

### 3.1 前端验证

```typescript
// src/modules/auth/components/LoginForm.vue
const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        // 支持员工编号/邮箱/手机号
        const isEmployeeNo = /^EMP\d{17}$/.test(value)
        const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
        const isMobile = /^1[3-9]\d{9}$/.test(value)

        if (!isEmployeeNo && !isEmail && !isMobile) {
          callback(new Error('请输入正确的员工编号/邮箱/手机号'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 20, message: '长度在 8 到 20 个字符', trigger: 'blur' }
  ],
  captcha: [
    {
      validator: (rule, value, callback) => {
        // 失败次数>=3时,验证码必填
        if (loginAttempts.value >= 3 && !value) {
          callback(new Error('请输入验证码'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 找回密码表单验证
const resetPasswordRules = {
  account: [
    { required: true, message: '请输入邮箱或手机号', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
        const isMobile = /^1[3-9]\d{9}$/.test(value)

        if (!isEmail && !isMobile) {
          callback(new Error('请输入正确的邮箱或手机号'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 20, message: '长度在 8 到 20 个字符', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        // 必须包含大小写字母和数字
        const hasUpperCase = /[A-Z]/.test(value)
        const hasLowerCase = /[a-z]/.test(value)
        const hasNumber = /\d/.test(value)

        if (!hasUpperCase || !hasLowerCase || !hasNumber) {
          callback(new Error('密码必须包含大小写字母和数字'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetPasswordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}
```

### 3.2 后端验证

```typescript
// 后端验证规则
const loginValidation = {
  username: {
    type: 'string',
    minLength: 3,
    maxLength: 50,
    required: true,
    pattern: /^EMP\d{17}$|^[^\s@]+@[^\s@]+\.[^\s@]+$|^1[3-9]\d{9}$/
  },
  password: {
    type: 'string',
    minLength: 8,
    maxLength: 20,
    required: true
  },
  captcha: {
    type: 'string',
    minLength: 4,
    maxLength: 6,
    required: false // 根据失败次数动态决定
  }
}

const passwordValidation = {
  newPassword: {
    type: 'string',
    minLength: 8,
    maxLength: 20,
    required: true,
    pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/, // 至少包含大小写字母和数字
    notMatchField: ['password'], // 不能与当前密码相同
    notMatchHistory: 3 // 不能与最近3次密码相同
  }
}
```

---

## 4. 算法实现

### 4.1 密码加密算法

```typescript
/**
 * 密码加密(Bcrypt + Salt)
 *
 * @param password 明文密码
 * @returns 密文密码
 *
 * @example
 * hashPassword('Password123') // '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
 */
import bcrypt from 'bcrypt'

async function hashPassword(password: string): Promise<string> {
  // 生成盐值(cost=10)
  const salt = await bcrypt.genSalt(10)

  // 加密密码
  const hash = await bcrypt.hash(password, salt)

  return hash
}

/**
 * 验证密码
 *
 * @param password 明文密码
 * @param hash 密文密码
 * @returns 是否匹配
 */
async function verifyPassword(password: string, hash: string): Promise<boolean> {
  return await bcrypt.compare(password, hash)
}
```

### 4.2 JWT Token生成算法

```typescript
import jwt from 'jsonwebtoken'

const JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key'
const JWT_EXPIRES_IN = '2h'
const REFRESH_TOKEN_EXPIRES_IN = '7d'

/**
 * 生成访问Token
 *
 * @param userInfo 用户信息
 * @returns JWT Token
 */
function generateAccessToken(userInfo: UserInfo): string {
  const payload = {
    userId: userInfo.id,
    username: userInfo.employeeNo,
    roles: userInfo.roles.map(r => r.code),
    type: 'access'
  }

  const options = {
    expiresIn: JWT_EXPIRES_IN,
    issuer: 'oa-system',
    audience: 'oa-system-web'
  }

  return jwt.sign(payload, JWT_SECRET, options)
}

/**
 * 生成刷新Token
 *
 * @returns UUID格式Token
 */
function generateRefreshToken(): string {
  return uuidv4()
}

/**
 * 验证Token
 *
 * @param token JWT Token
 * @returns 解码后的Payload
 */
function verifyToken(token: string): any {
  try {
    return jwt.verify(token, JWT_SECRET)
  } catch (error) {
    if (error.name === 'TokenExpiredError') {
      throw new Error('Token已过期')
    } else if (error.name === 'JsonWebTokenError') {
      throw new Error('Token无效')
    }
    throw error
  }
}

/**
 * 解析Token(不验证过期)
 */
function decodeToken(token: string): any {
  return jwt.decode(token)
}
```

### 4.3 登录限制算法

```typescript
/**
 * 检查登录限制
 *
 * @param username 用户名
 * @param ip IP地址
 * @returns 是否允许登录
 */
async function checkLoginRestriction(username: string, ip: string): Promise<{
  allowed: boolean
  reason?: string
  attemptsLeft?: number
  lockedUntil?: string
}> {
  // 1. 检查账号是否被锁定
  const user = await getUserByUsername(username)
  if (user.lockedUntil && new Date(user.lockedUntil) > new Date()) {
    return {
      allowed: false,
      reason: '账号已锁定',
      lockedUntil: user.lockedUntil
    }
  }

  // 2. 检查IP限流
  const ipFailCount = await getLoginFailCountByIP(ip, 3600) // 1小时内
  if (ipFailCount >= 10) {
    return {
      allowed: false,
      reason: '登录失败次数过多,请1小时后再试'
    }
  }

  // 3. 检查账号失败次数
  if (user.loginAttempts >= 5) {
    return {
      allowed: false,
      reason: '密码错误次数过多',
      attemptsLeft: 0
    }
  }

  return {
    allowed: true,
    attemptsLeft: 5 - (user.loginAttempts || 0)
  }
}

/**
 * 记录登录失败
 *
 * @param username 用户名
 * @param ip IP地址
 */
async function recordLoginFailure(username: string, ip: string): Promise<void> {
  // 1. 增加账号失败次数
  await incrementLoginAttempts(username)

  // 2. 记录IP失败次数
  await incrementIPFailCount(ip)

  // 3. 检查是否需要锁定账号
  const user = await getUserByUsername(username)
  if (user.loginAttempts >= 5) {
    // 锁定30分钟
    const lockedUntil = new Date(Date.now() + 30 * 60 * 1000)
    await lockUserAccount(username, lockedUntil)

    // 发送账号锁定通知
    await sendAccountLockedNotification(user.email, user.phone, lockedUntil)
  }
}

/**
 * 重置登录失败次数
 *
 * @param username 用户名
 */
async function resetLoginAttempts(username: string): Promise<void> {
  await updateUser(username, {
    loginAttempts: 0,
    lockedUntil: null,
    lastLoginTime: new Date()
  })
}
```

### 4.4 验证码生成算法

```typescript
import svgCaptcha from 'svg-captcha'

/**
 * 生成图形验证码
 *
 * @returns 验证码数据
 */
function generateCaptcha(): {
  key: string
  image: string
  text: string
} {
  const captcha = svgCaptcha.create({
    size: 4,                  // 验证码长度
    ignoreChars: '0o1il',     // 排除容易混淆的字符
    noise: 2,                 // 干扰线条数量
    color: true,              // 彩色
    background: '#f2f6fc',    // 背景色
    width: 120,
    height: 40,
    fontSize: 36
  })

  const key = `captcha_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`

  // 存储验证码到Redis(5分钟过期)
  redis.setex(key, 300, captcha.text.toLowerCase())

  return {
    key,
    image: `data:image/svg+xml;base64,${Buffer.from(captcha.data).toString('base64')}`,
    text: captcha.text // 仅用于测试,生产环境不要返回
  }
}

/**
 * 验证图形验证码
 *
 * @param key 验证码Key
 * @param code 用户输入的验证码
 * @returns 是否正确
 */
async function verifyCaptcha(key: string, code: string): Promise<boolean> {
  const storedCode = await redis.get(key)

  if (!storedCode) {
    return false // 验证码已过期
  }

  // 删除验证码(一次性使用)
  await redis.del(key)

  return storedCode.toLowerCase() === code.toLowerCase()
}

/**
 * 生成短信/邮箱验证码
 *
 * @param type 类型(email/mobile)
 * @param account 账号
 * @returns 验证码
 */
function generateVerificationCode(type: 'email' | 'mobile', account: string): string {
  // 生成6位数字验证码
  const code = Math.floor(100000 + Math.random() * 900000).toString()

  // 存储验证码到Redis(10分钟过期)
  const key = `verify_code_${type}_${account}`
  redis.setex(key, 600, code)

  return code
}
```

---

## 5. 安全机制

### 5.1 密码安全

**加密算法**:
- 使用Bcrypt加密算法(cost=10)
- 每个密码使用独立的盐值
- 不在日志中记录明文密码

**密码策略**:
- 最小长度8位
- 必须包含大小写字母和数字
- 不能与最近3次密码相同
- 90天过期提醒

**密码存储**:
```typescript
// 示例:密码存储到数据库
const passwordHash = await hashPassword('Password123')

// 存储到数据库
await db.users.create({
  username: 'zhangsan',
  password_hash: passwordHash, // $2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
  email: 'zhangsan@company.com'
})

// 存储密码历史
await db.passwordHistory.create({
  user_id: userId,
  password_hash: passwordHash
})
```

### 5.2 Token安全

**Access Token**:
- 存储方式:HttpOnly Cookie(防止XSS攻击)
- 有效期:2小时
- 签名算法:HS256
- 包含信息:userId、roles、过期时间

**Refresh Token**:
- 存储方式:LocalStorage + 数据库
- 有效期:7天
- 格式:UUID
- 单次使用,使用后失效

**Token刷新机制**:
```typescript
// axios请求拦截器
axios.interceptors.request.use(
  config => {
    const accessToken = getAccessToken()
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`
    }
    return config
  },
  error => Promise.reject(error)
)

// axios响应拦截器
axios.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config

    // Token过期,尝试刷新
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        const refreshToken = getRefreshToken()
        const { data } = await axios.post('/api/auth/refresh', { refreshToken })

        // 更新Token
        setAccessToken(data.accessToken)
        setRefreshToken(data.refreshToken)

        // 重试原请求
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`
        return axios(originalRequest)
      } catch (refreshError) {
        // 刷新失败,跳转登录页
        clearTokens()
        router.push('/login')
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)
```

### 5.3 防暴力破解

**IP限流**:
```typescript
import rateLimit from 'express-rate-limit'

// IP限流中间件
const loginLimiter = rateLimit({
  windowMs: 60 * 60 * 1000, // 1小时
  max: 10,                   // 最多10次
  message: '登录失败次数过多,请1小时后再试',
  standardHeaders: true,
  legacyHeaders: false,
  keyGenerator: (req) => req.ip, // 使用IP作为key
  handler: (req, res) => {
    res.status(429).json({
      code: 429,
      message: '请求过于频繁,请稍后再试'
    })
  }
})

app.use('/api/auth/login', loginLimiter)
```

**账号锁定**:
```typescript
// 登录失败处理
app.post('/api/auth/login', async (req, res) => {
  const { username, password } = req.body

  // 检查限制
  const restriction = await checkLoginRestriction(username, req.ip)
  if (!restriction.allowed) {
    return res.status(403).json({
      code: 1003,
      message: restriction.reason,
      data: {
        lockedUntil: restriction.lockedUntil
      }
    })
  }

  // 验证密码
  const user = await getUserByUsername(username)
  const isValid = await verifyPassword(password, user.password_hash)

  if (!isValid) {
    // 记录失败
    await recordLoginFailure(username, req.ip)

    return res.status(400).json({
      code: 1002,
      message: '密码错误',
      data: {
        attemptsLeft: restriction.attemptsLeft! - 1
      }
    })
  }

  // 登录成功,重置失败次数
  await resetLoginAttempts(username)

  // ...后续登录逻辑
})
```

### 5.4 XSS防护

**输入过滤**:
```typescript
import DOMPurify from 'dompurify'

// 过滤用户输入
function sanitizeInput(input: string): string {
  return DOMPurify.sanitize(input, {
    ALLOWED_TAGS: [],     // 不允许任何HTML标签
    ALLOWED_ATTR: []      // 不允许任何属性
  })
}

// 登录接口使用
app.post('/api/auth/login', async (req, res) => {
  const username = sanitizeInput(req.body.username)
  const password = req.body.password // 密码不过滤

  // ...
})
```

**Cookie安全**:
```typescript
// 设置HttpOnly Cookie
res.cookie('accessToken', accessToken, {
  httpOnly: true,      // 防止JavaScript访问
  secure: true,        // 仅HTTPS传输
  sameSite: 'strict',  // 防止CSRF攻击
  maxAge: 2 * 60 * 60 * 1000, // 2小时
  path: '/'
})
```

### 5.5 CSRF防护

**CSRF Token**:
```typescript
// 生成CSRF Token
const csrf = require('csurf')

const csrfProtection = csrf({ cookie: true })

app.use(csrfProtection)

// 登录接口需要CSRF Token
app.post('/api/auth/login', csrfProtection, async (req, res) => {
  // ...
})

// 提供CSRF Token
app.get('/api/auth/csrf-token', csrfProtection, (req, res) => {
  res.json({ csrfToken: req.csrfToken() })
})
```

### 5.6 SQL注入防护

**参数化查询**:
```typescript
// ❌ 错误:字符串拼接(容易SQL注入)
const user = await db.query(
  `SELECT * FROM users WHERE username = '${username}'`
)

// ✅ 正确:参数化查询
const user = await db.query(
  'SELECT * FROM users WHERE username = ?',
  [username]
)

// 或使用ORM
const user = await User.findOne({
  where: { username }
})
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-10
