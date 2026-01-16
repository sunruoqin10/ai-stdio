📋 auth_user vs sys_employee 业务区别
🔐 auth_user (认证用户表)
用途: 系统登录和认证

核心字段:

id - 关联 sys_employee.id
username - 登录用户名
password - 加密密码
email - 登录邮箱
mobile - 登录手机号
status - 账户状态
loginAttempts - 登录失败次数
lockedUntil - 锁定截止时间
lastLoginTime - 最后登录时间
lastLoginIp - 最后登录IP
passwordChangedAt - 密码修改时间
特点:

专注于身份认证和授权
记录登录安全信息(失败次数、锁定状态、登录历史)
只包含登录所需的最小信息集
不是所有员工都有 auth_user 记录(如果员工不需要登录系统)
👤 sys_employee (员工信息表)
用途: 员工人事管理

核心字段:

id - 员工编号 (格式: EMP+日期+序号)
name - 姓名
gender - 性别
birthDate - 出生日期
phone / email - 联系方式
avatar - 头像
departmentId - 部门ID
position - 职位
level - 职级
managerId - 直属上级ID
joinDate - 入职日期
probationStatus - 试用期状态
workYears - 工龄
status - 员工状态
emergencyContact - 紧急联系人
特点:

专注于员工信息和组织关系
包含完整的人事档案信息
记录工作相关的组织结构(部门、上级、职位)
所有员工都在这个表中
🔗 关系设计

sys_employee (员工信息表)
    ↓ 1:1 关系
auth_user (认证表)
关联关系:

auth_user.id = sys_employee.id
一个员工可以有认证账号,也可以没有(如果不需登录系统)
有认证账号的员工才能登录 OA 系统
💡 业务场景举例
场景	使用的表	说明
员工登录系统	auth_user	验证用户名密码,记录登录信息
查看员工档案	sys_employee	查看个人信息、工作经历
组织架构图	sys_employee	显示部门、上下级关系
修改密码	auth_user	更新密码和修改时间
员工离职	sys_employee	更新员工状态为离职
禁用登录账号	auth_user	锁定账号但不影响员工档案
✅ 设计优势
职责分离: 认证和业务信息分开管理
安全性: 认证信息独立存储,降低泄露风险
灵活性: 不是所有员工都需要系统账号
扩展性: 认证表可以轻松扩展(如支持第三方登录)
性能: 登录时只查询认证表,不需要加载完整的员工信息
这是一个**典型的关注点分离(Separation of Concerns)**设计模式! 🎯