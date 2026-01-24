# 项目上下文

## 项目目的
OA系统（办公自动化系统）- 一个综合性的企业管理系统，用于处理员工信息、部门组织、身份认证/授权、权限管理、资产管理、请假申请、费用报销、会议室预订等功能。系统提供基于角色的访问控制和审批工作流功能。

## 技术栈

### 前端 (oa-system-frontend)
- **框架**: Vue 3 + TypeScript + 组合式API (`<script setup>`)
- **构建工具**: Vite 7.2.4
- **UI组件库**: Element Plus 2.13.0 及图标库
- **状态管理**: Pinia 3.0.4
- **路由**: Vue Router 4.6.4
- **HTTP客户端**: Axios 1.13.2
- **样式**: SCSS/Sass
- **其他**: ECharts 6.0.0 (图表), vue-draggable-plus (拖拽), vue-tree-chart (组织架构树)

### 后端 (oa-system-backend)
- **框架**: Spring Boot 3.5.10-SNAPSHOT
- **语言**: Java 17
- **安全**: Spring Security + JWT (JJWT 0.12.3)
- **ORM**: MyBatis Plus 3.5.9 (Spring Boot 3 Starter)
- **数据库**: MySQL (mysql-connector-j)
- **缓存**: Caffeine (内存缓存) + Spring Cache 抽象
- **邮件**: Spring Boot Mail
- **工具库**: EasyCaptcha (验证码), UserAgentUtils (设备信息解析)
- **构建**: Maven (WAR打包)
- **热重载**: Spring Boot DevTools

## 项目约定

### 代码风格

#### 前端
- **TypeScript严格模式**: 启用严格类型检查
- **路径别名**: `@/*` 映射到 `src/*`
- **组件结构**: 模块化组织，位于 `src/modules/[模块名]/`
- **文件命名**:
  - 组件: 大驼峰 (PascalCase)，如 `EmployeeForm.vue`
  - 工具/组合式函数: 小驼峰 (camelCase)，如 `useMenu.ts`
  - 类型定义: 每个模块的 `types/index.ts`
- **代码检查**: 强制启用 `noUnusedLocals`, `noUnusedParameters`, `noFallthroughCasesInSwitch`

#### 后端
- **包结构**: `com.example.oa_system_backend.module.[模块]`
- **分层架构**:
  - `controller/` - REST接口 (`@RestController`)
  - `service/` 和 `service/impl/` - 业务逻辑层
  - `mapper/` - MyBatis Plus 数据访问层
  - `entity/` - 数据库实体
  - `dto/` - 请求对象
  - `vo/` - 响应对象
- **命名规范**:
  - 实体类: 单数形式，如 `Employee`, `Department`
  - DTO: `[实体]操作Request`，如 `EmployeeCreateRequest`
  - VO: `[实体]VO` 或描述性名称，如 `EmployeeDetailVO`, `DepartmentStatisticsVO`
- **数据库**: snake_case 字段自动映射到 camelCase Java 属性
- **逻辑删除**: `isDeleted` 字段 (1=已删除, 0=正常)

### 架构模式

#### 前端
- **模块化组织**: 每个功能是独立的模块，包含组件、视图、状态管理、API调用、类型和工具
- **组合式函数**: 共享逻辑提取到 `composables/` (如 `useMenu`, `useMenuTree`)
- **状态管理模式**: 模块级别的Pinia store (如 `modules/menu/stores/menuStore.ts`)
- **自定义指令**: 跨切面关注点的自定义指令 (如权限控制 `v-auth`)

#### 后端
- **模块分离**: 每个领域区域 (auth, employee, department等) 有独立的包
- **全局异常处理**: 使用 `@RestControllerAdvice` 的 `GlobalExceptionHandler`
- **JWT过滤器**: `JwtAuthenticationFilter` 用于令牌验证
- **元对象处理器**: `MyMetaObjectHandler` 自动填充审计字段
- **定时任务**: Spring `@Scheduled` 用于清理和提醒

### 测试策略
- 后端: 使用 Spring Boot Test 和 JUnit 5
- 测试位置: `src/test/java/`
- 前端暂无明确的测试配置 (按需添加)

### Git 工作流
- **主分支**: `main`
- **提交格式**: 中文提交信息 (如 "bug修改", "QA_20260123")
- **最近模式**: Bug修复和QA更新

## 领域知识

### 核心模块
1. **认证授权** (`auth`): 登录、登出、JWT令牌、验证码、会话管理、登录日志
2. **员工管理** (`employee`): 增删改查、状态更新、统计、操作日志、定时提醒 (生日、试用期、工龄)
3. **部门管理** (`department`): 树形结构、部门移动、成员管理、统计信息
4. **字典管理** (`dict`): 类型-项目层级字典、缓存、批量操作
5. **菜单管理** (`menu`): 动态菜单生成、图标选择、权限过滤、路由转换
6. **权限管理** (`permission`): 角色、角色-权限映射、角色成员、权限配置
7. **资产管理** (`asset`): 资产、借用记录、维护记录、分类
8. **请假管理** (`leave`): 请假申请、假期余额、节假日、统计、审批工作流
9. **费用管理** (`expense`): 费用报销、付款、统计、审批工作流
10. **会议管理** (`meeting`): 会议室预订、审批、我的预订、会议室管理

### 关键实体
- **用户**: `AuthUser` (认证), `Employee` (业务数据)
- **角色与权限**: 多对多关系，关联菜单/权限访问
- **部门**: 层级树形结构
- **字典**: 类型 → 项目 层级，用于下拉框、状态等

### 业务规则
- 员工同时关联 `AuthUser` (登录) 和 `Employee` (档案) 记录
- 基于角色的访问控制决定菜单可见性和按钮权限
- 请假和费用申请的多步审批工作流
- 逻辑删除防止数据丢失，同时过滤已删除记录

## 重要约束

### 技术约束
- **数据库**: 需要MySQL (当前配置为远程MySQL实例)
- **Java版本**: 需要 Java 17 以支持 Spring Boot 3.5+
- **Node版本**: 兼容 Vite 7.x (推荐 Node 18+)
- **文件上传**: 单文件最大10MB
- **JWT过期时间**: 访问令牌2小时，刷新令牌7天

### 安全注意事项
- **JWT密钥**: 生产环境应更改默认值 (当前为: `YourSecretKeyForJWTTokenGenerationShouldBeLongEnough`)
- **邮件凭证**: 占位符值 - 需要配置真实的SMTP
- **数据库凭证**: 在 application.yml 中暴露 - 生产环境应使用环境变量
- **CORS**: 需要为前后端通信适当配置

### 部署说明
- 后端运行在端口 8080
- 前端开发服务器在端口 3000，代理到后端
- 文件上传存储路径: `c:/Users/sunru/Downloads/oa-system/uploads` (Windows特定路径)

## 外部依赖

### 后端API
- **邮件服务**: SMTP服务器 (需要配置)
- **数据库**: 远程MySQL `8.140.210.133:3306`

### 前端API
- 所有API调用通过 `/api` 代理到后端 `http://localhost:8080`
- 文件访问通过 `/uploads` 端点

### 集成点
- **JWT认证**: 每次请求的无状态令牌验证
- **验证码**: 使用EasyCaptcha库生成
- **用户代理解析**: 用于登录日志的设备/浏览器检测
