# 登录认证模块后端规范

> **对应前端规范**: `oa-system-frontend-specs/auth/login`
> **对应数据库规范**: `oa-system-database-specs/04-auth`
> **技术栈**: Spring Boot + MyBatis + MySQL
> **包路径**: `com.oa.module.auth`

---

## 1. 实体类设计 (Entity)

> 实体类对应数据库表结构，位于 `entity` 包下。

### 1.1 AuthUser (用户表)
```java
@Data
@TableName("auth_user")
public class AuthUser {
    @TableId(type = IdType.INPUT)
    private String id; // 关联 sys_employee.id
    private String username;
    private String password;
    private String email;
    private String mobile;
    private String status; // active, locked, disabled
    private Integer loginAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
```

### 1.2 AuthUserSession (会话表)
```java
@Data
@TableName("auth_user_session")
public class AuthUserSession {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String userId;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiresAt;
    private String loginIp;
    private String loginLocation;
    private String deviceInfo;
    private String browser;
    private String os;
    private LocalDateTime loginTime;
    private LocalDateTime lastActiveTime;
    private Integer isActive;
    private LocalDateTime logoutTime;
}
```

### 1.3 AuthLoginLog (登录日志)
```java
@Data
@TableName("auth_login_log")
public class AuthLoginLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String username;
    private String loginIp;
    private String loginLocation;
    private String deviceInfo;
    private String browser;
    private String os;
    private String status; // success, failed
    private String failureReason;
    private LocalDateTime loginTime;
}
```

### 1.4 AuthVerificationCode (验证码)
```java
@Data
@TableName("auth_verification_code")
public class AuthVerificationCode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type; // email, mobile
    private String account;
    private String scene; // forgot_password, login, register
    private String code;
    private Integer isUsed;
    private LocalDateTime usedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
```

---

## 2. 数据传输对象 (DTO/VO)

> DTO 用于接收前端参数，VO 用于返回前端数据，位于 `dto` 和 `vo` 包下。

### 2.1 LoginRequest (登录请求)
```java
@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username; // 支持 用户名/邮箱/手机号
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private String captcha;
    private String captchaKey;
    private Boolean remember;
}
```

### 2.2 LoginResponse (登录响应)
```java
@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // 秒
    private UserInfoVO userInfo;
}
```

### 2.3 UserInfoVO (用户信息)
```java
@Data
public class UserInfoVO {
    private String id;
    private String employeeNo;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private String departmentId;
    private String departmentName;
    private String position;
    private List<RoleVO> roles;
    private List<String> permissions;
}
```

---

## 3. Controller 层接口定义

> 路径前缀: `/api/auth`

| 方法 | 路径 | 描述 | 请求参数 | 响应数据 |
| --- | --- | --- | --- | --- |
| POST | `/login` | 用户登录 | `LoginRequest` | `ApiResponse<LoginResponse>` |
| POST | `/logout` | 退出登录 | - | `ApiResponse<Void>` |
| POST | `/refresh` | 刷新Token | `RefreshTokenRequest` | `ApiResponse<RefreshTokenResponse>` |
| GET | `/captcha` | 获取图形验证码 | - | `ApiResponse<CaptchaResponse>` |
| POST | `/send-code` | 发送验证码 | `SendCodeRequest` | `ApiResponse<Void>` |
| POST | `/reset-password` | 重置密码 | `ResetPasswordRequest` | `ApiResponse<Void>` |
| GET | `/sessions` | 获取活跃会话 | - | `ApiResponse<Page<UserSessionVO>>` |
| DELETE | `/sessions/{id}` | 踢出/删除会话 | Path: id | `ApiResponse<Void>` |
| GET | `/login-logs` | 获取登录日志 | Query: page, size, dates | `ApiResponse<Page<LoginLogVO>>` |

---

## 4. Service 层业务逻辑与约束实现

> 核心业务逻辑实现，**重点包含被数据库层忽略的约束逻辑**。

### 4.1 登录逻辑 (login)
1.  **参数校验**: 使用 JSR-303 校验必填项。
2.  **验证码校验**: 如果连续失败次数 > 3，强制校验图形验证码。
3.  **用户查询**: 
    *   根据 username/email/mobile 查询 `auth_user`。
    *   若用户不存在，抛出 `BadCredentialsException` (模糊提示)。
4.  **状态检查 (Check Constraints)**:
    *   检查 `status`: 若为 `disabled`，抛出“账号已禁用”。
    *   检查 `status`: 若为 `locked`，检查 `locked_until`。若当前时间 < `locked_until`，抛出“账号锁定中，请于xx后重试”；否则自动解锁（重置 `login_attempts=0`, `status=active`）。
5.  **密码验证**:
    *   比对明文密码 (生产环境应使用 BCrypt)。
    *   **失败处理**: 
        *   `login_attempts` + 1。
        *   若 `login_attempts` >= 5，设置 `status=locked`, `locked_until = now + 30min`。
        *   记录失败日志 (`auth_login_log`)。
    *   **成功处理**:
        *   重置 `login_attempts=0`, `locked_until=null`。
        *   更新 `last_login_time`, `last_login_ip`。
6.  **关联检查 (Foreign Key Logic)**:
    *   查询 `sys_employee` 信息。若 `auth_user.id` 在 `sys_employee` 中不存在，记录警告日志但允许登录（或根据策略禁止）。
    *   查询 `sys_role` 和 `sys_permission` 组装 `UserInfo`。
7.  **会话创建**:
    *   生成 AccessToken (JWT) 和 RefreshToken (UUID)。
    *   插入 `auth_user_session`。
    *   **约束检查**: 确保 `expires_at > login_time`。
8.  **日志记录**: 插入 `auth_login_log` (status='success')。

### 4.2 发送验证码 (sendCode)
1.  **格式校验 (Check Constraints)**:
    *   **Email**: 正则 `^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$`
    *   **Mobile**: 正则 `^1[3-9][0-9]{9}$`
2.  **频率限制**: 检查 `auth_verification_code` 中同账号1分钟内的记录数，防止轰炸。
3.  **生成与发送**: 生成6位随机码，调用邮件/短信服务。
4.  **保存记录**: 插入 `auth_verification_code`，设置过期时间（邮件10min，短信5min）。

### 4.3 重置密码 (resetPassword)
1.  **验证码校验**:
    *   查询未使用的、未过期的、匹配场景 (`forgot_password`) 的验证码。
    *   若无效，抛出异常。
2.  **密码强度校验 (Check Constraints)**:
    *   长度 >= 8。
    *   包含大小写字母和数字。
    *   不能与最近3次历史密码相同 (查询 `auth_password_history`)。
3.  **执行重置**:
    *   更新 `auth_user` 密码。
    *   将验证码标记为已使用 (`is_used=1`, `used_at=now`)。
    *   插入 `auth_password_history`。
    *   **外键模拟**: 确保 `user_id` 存在于 `auth_user`。

### 4.4 会话管理
1.  **踢出会话**:
    *   逻辑删除: `auth_user_session` 设置 `is_active=0`, `logout_time=now`。
    *   物理删除: 定时任务清理过期数据。

---

## 5. Mapper 层 SQL 规范

> 使用 MyBatis Plus 或 XML 映射。

### 5.1 AuthUserMapper
*   `selectByUsernameOrEmailOrMobile`: 多字段匹配查询用户。
*   `updateLoginStats`: 更新登录统计信息 (CAS乐观锁更新建议)。

### 5.2 AuthUserSessionMapper
*   `insertSession`: 插入会话。
*   `selectActiveSessionByRefreshToken`: 根据RefreshToken查询有效会话。

### 5.3 AuthLoginLogMapper
*   `insertLog`: 插入日志。
*   **注意**: 这里的 `user_id` 虽然数据库无外键，但 Mapper 层应确保插入的值有效，或允许 NULL (对于未知用户的登录尝试)。

---

## 6. 异常处理规范

| 异常类 | 描述 | HTTP状态码 |
| --- | --- | --- |
| `AuthenticationException` | 认证失败(用户名密码错误) | 401 |
| `AccountLockedException` | 账号被锁定 | 403 |
| `AccountDisabledException` | 账号被禁用 | 403 |
| `CaptchaException` | 验证码错误或过期 | 400 |
| `TokenInvalidException` | Token无效或过期 | 401 |
| `BusinessException` | 通用业务异常(如发送频率限制) | 400 |

---

## 7. 定时任务 (Schedule)

需在后端实现以下定时任务替代数据库事件：
1.  **CleanExpiredSessions**: 每小时执行，清理 `auth_user_session` 中 `expires_at < now` 的记录。
2.  **CleanExpiredCodes**: 每30分钟执行，清理 `auth_verification_code` 过期数据。
3.  **UnlockAccounts**: 每10分钟执行，扫描 `locked_until < now` 的用户并重置状态 (可选，也可在登录时实时判断)。

---

**文档版本**: v1.0.0
**创建时间**: 2026-01-12
