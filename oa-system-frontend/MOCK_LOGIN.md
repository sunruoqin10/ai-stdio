# 测试账号说明

## 📝 Mock登录功能

已启用Mock API,可以使用以下测试账号登录系统。

## 🔑 测试账号列表

### 1. 系统管理员账号

```
账号: admin
密码: Admin123
权限: 所有权限 (*)
```

**说明**: 拥有系统所有权限,可以访问所有功能模块。

---

### 2. 普通员工账号

```
账号: zhangsan
密码: Password123
权限: 员工查看、员工创建、字典查看
```

**说明**: 技术部软件工程师,拥有基础的员工管理权限。

---

### 3. HR专员账号

```
账号: lisi
密码: Password123
权限: 员工查看、员工创建、员工编辑、员工删除、字典查看
```

**说明**: 人事部HR专员,拥有完整的员工管理权限。

---

## 🎯 快速测试

### 方式一: 直接登录

1. 访问 `http://localhost:5173/login`
2. 输入上述任意账号和密码
3. 点击"登录"按钮

### 方式二: 测试错误密码

1. 输入正确账号,错误密码
2. 连续错误5次后账号将被锁定30分钟
3. 连续错误3次后会显示验证码

### 方式三: 测试"记住我"

1. 勾选"记住我"复选框
2. 登录成功后,关闭浏览器重新打开
3. Token有效期内无需重新登录

---

## 🔧 Mock API说明

### 已实现的Mock接口

- ✅ `POST /api/auth/login` - 用户登录
- ✅ `POST /api/auth/logout` - 退出登录
- ✅ `POST /api/auth/refresh` - 刷新Token
- ✅ `GET /api/auth/captcha` - 获取验证码
- ✅ `POST /api/auth/send-code` - 发送验证码
- ✅ `POST /api/auth/reset-password` - 重置密码

### Mock特性

- **自动Token刷新**: Access Token过期前自动刷新
- **错误模拟**: 支持密码错误、账号锁定等场景
- **验证码**: 失败3次后自动显示图形验证码
- **网络延迟**: 模拟真实网络请求延迟(300-800ms)

### 如何查看Mock验证码

使用"找回密码"功能时,验证码会输出到浏览器控制台:
```
[Mock] 验证码: 123456
```

---

## 🚀 启动项目

```bash
npm run dev
```

访问: `http://localhost:5173/login`

---

## 📌 注意事项

1. **仅开发环境启用**: Mock API只在开发环境(`DEV模式`)下启用
2. **数据不持久**: Mock数据存储在内存中,刷新页面后会重置
3. **Token存储**: Token存储在LocalStorage中
4. **验证码**: 找回密码的验证码会在控制台显示

---

## 🔍 调试技巧

### 查看当前用户信息

登录后,在浏览器控制台执行:

```javascript
// 查看Token
localStorage.getItem('access_token')
localStorage.getItem('refresh_token')

// 查看用户信息(需要先导入store)
import { useAuthStore } from '@/modules/auth/store'
const authStore = useAuthStore()
console.log(authStore.userInfo)
console.log(authStore.userPermissions)
```

### 手动清除登录状态

在浏览器控制台执行:

```javascript
localStorage.clear()
location.reload()
```

---

**版本**: v1.0.0
**创建日期**: 2026-01-10
