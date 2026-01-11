# OA系统规范文档 - 三层架构版

> **更新日期**: 2026-01-09
> **版本**: v3.0 (三层架构拆分版)
> **状态**: ✅ 所有核心模块已完成三层拆分

---

## 🎉 重要更新

所有规范文档已按照**三层架构**拆分为独立文件!

### 📊 拆分统计

| 模块类别 | 模块数 | 原文档 | 拆分后文件 | 总文件数 |
|---------|--------|--------|-----------|---------|
| 核心基础 | 3 | 3个 | 9个 | 9 |
| 审批流程 | 2 | 2个 | 6个 | 6 |
| 行政协同 | 1 | 1个 | 3个 | 3 |
| **总计** | **6** | **6个** | **18个** | **18** |

---

## 📂 新的文件结构

```
specs/
├── _template/                      # 模板目录
│   ├── spec-template.md           # v1.0 单层模板
│   ├── spec-template-v2.md        # v2.0 三层模板(整版)
│   ├── prompts.md                 # AI提示词模板
│   ├── 三层架构说明.md            # 详细说明
│   └── QUICK_REFERENCE.md         # 快速参考
│
├── core/                           # 核心基础模块 (3个)
│   ├── employee_Functional.md    # ✅ 员工-功能需求
│   ├── employee_Technical.md     # ✅ 员工-技术实现
│   ├── employee_Design.md        # ✅ 员工-UI/UX设计
│   ├── department_Functional.md  # ✅ 部门-功能需求
│   ├── department_Technical.md   # ✅ 部门-技术实现
│   ├── department_Design.md      # ✅ 部门-UI/UX设计
│   ├── asset_Functional.md       # ✅ 资产-功能需求
│   ├── asset_Technical.md        # ✅ 资产-技术实现
│   └── asset_Design.md           # ✅ 资产-UI/UX设计
│
├── approval/                       # 审批流程模块 (2个)
│   ├── leave_Functional.md       # ✅ 请假-功能需求
│   ├── leave_Technical.md        # ✅ 请假-技术实现
│   ├── leave_Design.md           # ✅ 请假-UI/UX设计
│   ├── expense_Functional.md     # ✅ 费用报销-功能需求
│   ├── expense_Technical.md      # ✅ 费用报销-技术实现
│   └── expense_Design.md         # ✅ 费用报销-UI/UX设计
│
└── admin/                          # 行政协同模块 (1个)
    ├── meeting_Functional.md     # ✅ 会议室-功能需求
    ├── meeting_Technical.md      # ✅ 会议室-技术实现
    └── meeting_Design.md         # ✅ 会议室-UI/UX设计
```

**说明**:
- 原有的 `*-spec.md` 文件保留,作为参考
- 新的 `*_Functional.md`, `*_Technical.md`, `*_Design.md` 为推荐使用的三层架构版本

---

## 🎯 三层架构文件说明

### 第一层: 功能需求规范 (*_Functional.md)

**文件后缀**: `_Functional.md`

**目标读者**: 产品经理、业务分析师、测试工程师

**核心内容**:
- ✅ 功能概述(模块简介、业务价值、目标用户)
- ✅ 用户故事(As a...I want...So that...)
- ✅ 功能清单(按优先级P0/P1/P2分类)
- ✅ 交互流程(流程图、泳道图)
- ✅ 业务规则(数据约束、业务逻辑、权限规则)

**示例**:
```markdown
# 员工管理功能需求规范

## 1. 功能概述
### 1.1 模块简介
员工管理模块是OA系统的核心基础模块...

### 1.2 业务价值
- 集中管理员工信息,提高HR工作效率
- 实时统计员工数据,支持管理决策

## 2. 用户故事
### 2.1 故事1: 快速查找员工
**作为** HR管理员,
**我想要** 快速查找员工信息,
**以便** 高效处理日常人事工作

**验收标准**:
- [ ] 支持按姓名、工号、手机号搜索
- [ ] 搜索响应时间 < 1秒

## 3. 业务规则
### 3.1 工龄计算规则
工龄 = 当前年份 - 入职年份
如果当前月份 < 入职月份,工龄 - 1
最小值: 0年
```

---

### 第二层: 技术实现规范 (*_Technical.md)

**文件后缀**: `_Technical.md`

**目标读者**: 前端开发、后端开发、架构师

**核心内容**:
- ✅ 数据结构(TypeScript类型定义、数据库设计)
- ✅ API接口(RESTful设计、请求/响应格式)
- ✅ 验证规则(前端验证、后端验证、业务逻辑验证)
- ✅ 算法实现(核心算法、复杂度分析)
- ✅ 自动化功能(定时任务、自动计算、自动提醒)

**示例**:
```markdown
# 员工管理技术实现规范

## 1. 数据结构
### 1.1 TypeScript类型定义
```typescript
interface Employee {
  id: string
  name: string
  phone: string
  email: string
  departmentId: string
  // ...
}
```

### 1.2 数据库设计
```sql
CREATE TABLE employees (
  id VARCHAR(20) PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) NOT NULL UNIQUE,
  email VARCHAR(100) NOT NULL UNIQUE,
  ...
);
```

## 2. API接口
### 2.1 获取员工列表
GET /api/employees?page=1&pageSize=20

Response:
{
  "code": 200,
  "data": {
    "list": Employee[],
    "total": 100
  }
}

## 3. 算法实现
### 3.1 工龄计算算法
```typescript
function calculateWorkYears(joinDate: string): number {
  const join = new Date(joinDate)
  const now = new Date()
  let years = now.getFullYear() - join.getFullYear()

  if (now.getMonth() < join.getMonth()) {
    years--
  }

  return Math.max(0, years)
}
```
```

---

### 第三层: UI/UX设计规范 (*_Design.md)

**文件后缀**: `_Design.md`

**目标读者**: UI/UX设计师、前端开发工程师

**核心内容**:
- ✅ 页面布局(ASCII布局图、栅格系统)
- ✅ 组件选择(Element Plus组件、自定义组件)
- ✅ 交互规范(加载状态、错误处理、操作反馈)
- ✅ 样式规范(颜色、字体、间距、圆角、阴影)
- ✅ 响应式设计(断点系统、移动端适配)

**示例**:
```markdown
# 员工管理UI/UX设计规范

## 1. 页面布局
### 1.1 列表页布局
```
┌─────────────────────────────────────────┐
│  PageHeader: 员工管理    [新增] [导出]    │
├───────────┬───────────────────────────┬─────────┐
│ Filter    │    Data Table             │  Stats  │
│ Panel     │    - Table View           │  Panel  │
│           │    - Card View            │         │
├───────────┴───────────────────────────┴─────────┤
│  Pagination                                    │
└─────────────────────────────────────────────────┘
```

### 1.2 详情页布局
```
┌─────────────────┬───────────────────────────┐
│  Info Card      │  Tabs: 基本信息 | 工作信息  │
│  - 头像 (150px) │  - 英文名: Tom             │
│  - 姓名         │  - 性别: 男                │
│  - 职位         │  - 手机: 138****8000        │
└─────────────────┴───────────────────────────┘
```

## 2. 组件选择
| 功能 | 组件 | 配置 |
|------|------|------|
| 数据表格 | el-table | stripe, border, row-key |
| 表单 | el-form | :model, :rules |
| 对话框 | el-dialog | width="600px" |

## 3. 样式规范
### 3.1 颜色系统
```scss
$primary-color: #1890FF;
$success-color: #52C41A;
$warning-color: #FAAD14;
$error-color: #F5222D;
```

### 3.2 字体系统
```scss
$font-size-base: 14px;
$font-size-large: 16px;
$font-weight-normal: 400;
$font-weight-bold: 700;
```
```

---

## 💡 使用指南

### 1. 按角色阅读

**产品经理 / 业务分析师**:
```bash
# 只看功能需求规范
cat specs/core/employee_Functional.md
cat specs/core/department_Functional.md
cat specs/approval/leave_Functional.md
```

**后端开发工程师**:
```bash
# 重点看功能需求 + 技术实现
cat specs/core/employee_Functional.md
cat specs/core/employee_Technical.md
```

**前端开发工程师**:
```bash
# 重点看技术实现 + UI/UX设计
cat specs/core/employee_Technical.md
cat specs/core/employee_Design.md
```

**UI/UX设计师**:
```bash
# 只看UI/UX设计规范
cat specs/core/employee_Design.md
cat specs/approval/leave_Design.md
```

### 2. AI辅助开发

**生成单个模块的完整代码**:
```bash
# 使用Claude Code
claude-code

# 提示词:
"根据以下三层架构规范,生成部门管理模块:

功能需求: specs/core/department_Functional.md
技术实现: specs/core/department_Technical.md
UI设计: specs/core/department_Design.md

参考实现: src/modules/employee/

要求:
1. 理解功能需求(用户故事、业务规则)
2. 实现技术方案(数据结构、API接口、算法)
3. 按照UI设计实现界面(布局、组件、样式)
"
```

**生成特定层的代码**:
```bash
# 只生成数据结构
"根据 specs/core/employee_Technical.md 生成:
- TypeScript类型定义
- API接口封装
- Pinia Store
参考: src/modules/employee/types/index.ts"

# 只生成UI组件
"根据 specs/core/employee_Design.md 生成:
- 列表页布局
- 表单组件
- 样式文件
使用: Element Plus组件库"
```

### 3. 团队协作

**并行开发**:
```
第1周:
- 产品经理: 编写/更新 *_Functional.md
- UI设计师: 根据 *_Functional.md 设计原型

第2周:
- 后端工程师: 根据 *_Functional.md + *_Technical.md 实现API
- 前端工程师: 根据 *_Technical.md + *_Design.md 实现界面

第3周:
- 联调测试
```

---

## 📊 模块完成情况

### ✅ 已完成三层拆分的模块

| 模块 | 功能需求 | 技术实现 | UI/UX设计 | 状态 |
|------|---------|---------|----------|------|
| **员工管理** | ✅ | ✅ | ✅ | 完成 |
| **部门管理** | ✅ | ✅ | ✅ | 完成 |
| **资产管理** | ✅ | ✅ | ✅ | 完成 |
| **请假管理** | ✅ | ✅ | ✅ | 完成 |
| **费用报销** | ✅ | ✅ | ✅ | 完成 |
| **会议室预定** | ✅ | ✅ | ✅ | 完成 |

### ⏳ 待开发的模块

| 模块 | 优先级 | 说明 |
|------|--------|------|
| 物品采购 | P1 | 审批流程模块 |
| 公告栏 | P1 | 行政协同模块 |
| 工作周报 | P1 | 行政协同模块 |
| 维基知识库 | P2 | 知识文档模块 |
| SOP管理 | P2 | 知识文档模块 |

---

## 🚀 下一步行动

### 1. 立即可用

现在可以使用三层架构规范文档进行AI辅助开发:

```bash
# 示例: 生成部门管理模块
"根据 specs/core/department_*.md 三层架构规范:
- 部门_Functional.md: 理解业务需求
- 部门_Technical.md: 实现技术方案
- 部门_Design.md: 实现UI界面

生成完整的部门管理模块代码"
```

### 2. 新模块开发

为未来的模块创建三层架构规范:

```bash
# 复制模板
cp specs/_template/spec-template-v2.md specs/approval/procurement-spec-v2.md

# 或者拆分为三个文件
# 分别编写 Functional, Technical, Design
```

### 3. 文档维护

- ✅ 三层文档可以独立更新
- ✅ 修改某一层不影响其他层
- ✅ 使用版本控制管理变更

---

## 📚 参考文档

### 核心文档
- [三层架构详细说明](../_template/三层架构说明.md)
- [快速参考卡片](../_template/QUICK_REFERENCE.md)
- [v2.0三层模板](../_template/spec-template-v2.md)

### 原始文档(参考)
- [员工管理-原始](../core/employee-spec.md)
- [部门管理-原始](../core/department-spec.md)
- [资产管理-原始](../core/asset-spec.md)
- [请假管理-原始](../approval/leave-spec.md)
- [费用报销-原始](../approval/expense-spec.md)
- [会议室预定-原始](../admin/meeting-spec.md)

---

## 🎯 三层架构优势总结

### 1. 清晰的关注点分离
- **功能层**: 只关注"做什么"
- **技术层**: 只关注"怎么做"
- **设计层**: 只关注"长什么样"

### 2. 更好的AI理解
- 结构化信息便于AI处理
- 每层职责明确,生成代码更准确
- 支持针对特定层的精确提示

### 3. 便于团队协作
- 不同角色关注不同文档
- 可以并行工作
- 减少沟通成本

### 4. 易于维护和更新
- 单层修改不影响其他层
- 独立的版本控制
- 更清晰的变更追踪

### 5. 更强的可复用性
- 功能层可以复用到其他项目
- 技术层可以参考实现模式
- 设计层可以统一UI风格

---

**文档版本**: v3.0 (三层架构拆分版)
**创建人**: AI开发助手
**最后更新**: 2026-01-09
**维护团队**: OA系统开发团队

---

## 📞 支持

如有问题或建议,请联系OA系统开发团队。
