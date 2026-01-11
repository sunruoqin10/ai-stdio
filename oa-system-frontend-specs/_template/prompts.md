# AI辅助开发提示词模板

> 本文档收录了各种AI辅助开发场景的提示词模板,可直接复制使用或根据需要修改。

---

## 📋 目录

- [一、生成新模块](#一生成新模块)
- [二、生成组件](#二生成组件)
- [三、生成API](#三生成api)
- [四、生成Store](#四生成store)
- [五、生成测试](#五生成测试)
- [六、代码优化](#六代码优化)
- [七、Bug修复](#七bug修复)
- [八、文档生成](#八文档生成)
- [九、特殊场景](#九特殊场景)

---

## 一、生成新模块

### 1.1 生成完整模块(推荐)

```
根据 specs/[module]-spec.md 规范文档,生成完整的[模块名称]模块:

## 技术要求
- 框架: Vue 3 Composition API + TypeScript
- UI组件库: Element Plus
- 状态管理: Pinia
- HTTP请求: Axios (使用 @/utils/request.ts)

## 参考模板
参考 src/modules/employee/ 目录的代码结构和风格

## 需要生成的文件

### 1. 类型定义
- 文件: src/modules/[module]/types/index.ts
- 内容:
  * 数据类型定义 (interface [ModuleName])
  * 筛选条件类型 (interface [ModuleName]Filter)
  * 表单数据类型 (interface [ModuleName]Form)
  * 统计数据类型 (interface [ModuleName]Statistics)
  * 相关枚举类型

### 2. Mock数据
- 文件: src/modules/[module]/mock/data.ts
- 内容:
  * 生成10-15条模拟数据
  * 数据要真实合理
  * 包含各种状态和边界情况

### 3. API接口
- 文件: src/modules/[module]/api/index.ts
- 内容:
  * getList() - 获取列表(支持分页和筛选)
  * getDetail() - 获取详情
  * create() - 创建
  * update() - 更新
  * remove() - 删除
  * 其他业务接口
- 所有接口返回Promise<T>
- 使用request封装,延迟300ms模拟网络请求

### 4. Pinia Store
- 文件: src/modules/[module]/store/index.ts
- 内容:
  * State: 列表数据、详情、加载状态、分页、筛选条件
  * Getters: 计算属性
  * Actions: CRUD操作、分页、筛选、重置等
- 使用defineStore()

### 5. 视图组件

#### 列表页
- 文件: src/modules/[module]/views/[Module]List.vue
- 内容:
  * 页面头部(PageHeader)
  * 筛选面板(可折叠)
  * 数据表格(el-table)
  * 统计面板(右侧)
  * 分页器(el-pagination)
  * 操作: 新增、编辑、删除、查看详情
- 支持表格/卡片视图切换

#### 详情页
- 文件: src/modules/[module]/views/[Module]Detail.vue
- 内容:
  * 页面头部(返回按钮)
  * 左侧信息卡片(300px固定)
  * 右侧标签页(el-tabs)
    - 基本信息tab
    - 详细信息tab
    - 相关记录tab(时间轴展示)
  * 操作按钮: 编辑、保存、删除等

#### 表单组件
- 文件: src/modules/[module]/components/[Module]Form.vue
- 内容:
  * 步骤式表单(el-steps)
    - Step 1: 基本信息(必填)
    - Step 2: 详细信息(可选)
    - Step 3: 确认提交
  * 表单验证规则
  * 必填项验证
  * 格式验证(手机号/邮箱/数字等)
  * 实时验证反馈

### 6. 筛选面板组件
- 文件: src/modules/[module]/components/FilterPanel.vue
- 内容:
  * 关键词搜索
  * 多种筛选条件(下拉/日期范围等)
  * 重置按钮
  * 应用筛选按钮

## 代码要求
1. 完整的TypeScript类型定义
2. 遵循ESLint规则
3. 遵循项目代码风格(参考employee模块)
4. 充分的注释说明
5. 错误处理和加载状态
6. 响应式布局
7. 可访问性(a11y)

## 复用要求
- 复用 @/components/common/PageHeader.vue
- 复用 @/components/common/StatusTag.vue
- 复用 @/utils/request.ts
- 复用 @/utils/format.ts 中的工具函数

## 特别注意
[填写模块的特殊要求和注意事项]
```

---

### 1.2 快速生成模块(简化版)

```
参考 src/modules/employee/,生成[模块名称]模块的核心文件:

1. types/index.ts - TypeScript类型定义
2. api/index.ts - API接口(CRUD)
3. store/index.ts - Pinia Store
4. views/[Module]List.vue - 列表页(表格+筛选+分页)
5. views/[Module]Detail.vue - 详情页
6. components/[Module]Form.vue - 表单组件

技术栈: Vue 3 + TypeScript + Element Plus + Pinia
数据结构: [简要描述数据结构]
验证规则: [简要说明验证要求]
```

---

## 二、生成组件

### 2.1 生成表单组件

```
生成[模块名称]的表单组件:

## 表单字段
1. field1 (string, 必填): [说明]
2. field2 (number, 可选): [说明]
3. field3 (date, 必填): [说明]

## 验证规则
- field1: 最小2字符,最大50字符
- field2: 数字类型,大于等于0
- field3: 必须选择今天或之后的日期

## 表单要求
- 使用el-form组件
- 使用el-form-item验证
- 实时验证反馈
- 清晰的错误提示
- 支持重置表单
- 提交前再次验证

## UI布局
- 栅格布局(el-row/el-col)
- 响应式设计
- 必填项红色星号标识
- 添加字段说明文字

## 代码结构
<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance } from 'element-plus'

// 表单数据
const formData = reactive({...})

// 表单引用
const formRef = ref<FormInstance>()

// 验证规则
const rules = {...}

// 提交方法
const handleSubmit = async () => {...}

// 重置方法
const handleReset = () => {...}
</script>

<template>
  <el-form ref="formRef" :model="formData" :rules="rules">
    <!-- 表单项 -->
  </el-form>
</template>
```

---

### 2.2 生成表格组件

```
生成[模块名称]的表格组件:

## 表格列
1. 列1: field1, 宽度150px
2. 列2: field2, 宽度200px
3. 列3: field3, 宽度120px, 居中对齐
4. 操作列: 固定右侧, 包含编辑、删除按钮

## 表格功能
- 多选列(el-table-column type="selection")
- 序号列(type="index")
- 排序功能(sortable)
- 筛选功能(filters)
- 格式化显示(formatter)
- 空状态提示
- 加载状态

## 操作功能
- 编辑: 打开编辑对话框
- 删除: 二次确认后删除
- 查看详情: 跳转到详情页

## UI要求
- 斑马纹(stripe)
- 边框(border)
- 高度自适应或固定高度
- 悬浮效果

## 代码示例
<template>
  <el-table
    :data="tableData"
    stripe
    border
    v-loading="loading"
    @selection-change="handleSelectionChange"
  >
    <el-table-column type="selection" width="55" />
    <el-table-column type="index" label="序号" width="60" />
    <!-- 数据列 -->
    <el-table-column label="操作" fixed="right" width="180">
      <template #default="{ row }">
        <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
        <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>
```

---

### 2.3 生成图表组件

```
使用ECharts生成[图表名称]图表:

## 图表类型
- 类型: [折线图/柱状图/饼图/散点图等]
- 尺寸: 宽度100%, 高度400px

## 数据配置
- X轴: [字段名]
- Y轴: [字段名]
- 系列数据: [数据说明]

## 图表功能
- 响应式resize
- 图例显示
- 提示框(tooltip)
- 工具栏(saveAsImage等)
- 数据缩放(dataZoom,如果需要)

## 样式要求
- 颜色主题: 使用项目主题色
- 字体: 14px
- 动画: 平滑过渡
- 网格: 显示/隐藏

## 代码示例
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts

onMounted(() => {
  chartInstance = echarts.init(chartRef.value!)
  const option = { /* ECharts配置 */ }
  chartInstance.setOption(option)

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})

const handleResize = () => {
  chartInstance?.resize()
}
</script>

<template>
  <div ref="chartRef" style="width: 100%; height: 400px"></div>
</template>
```

---

## 三、生成API

### 3.1 生成RESTful API

```
为[模块名称]生成RESTful API接口封装:

## 基础URL
/api/[module]

## 接口列表
1. GET /api/[module] - 获取列表
   - 参数: page, pageSize, keyword, [其他筛选条件]
   - 返回: { code, message, data: { list, total, page, pageSize } }

2. GET /api/[module]/:id - 获取详情
   - 参数: id (路径参数)
   - 返回: { code, message, data: [DetailType] }

3. POST /api/[module] - 创建
   - 参数: [FormData]
   - 返回: { code, message, data: { id } }

4. PUT /api/[module]/:id - 更新
   - 参数: id (路径参数), [PartialFormData]
   - 返回: { code, message, data: [DetailType] }

5. DELETE /api/[module]/:id - 删除
   - 参数: id (路径参数)
   - 返回: { code, message }

## 代码要求
- 使用 @/utils/request.ts 封装的axios实例
- 完整的TypeScript类型定义
- 统一的错误处理
- Mock延迟300ms
```

---

### 3.2 生成业务API

```
生成[业务名称]的API接口:

## 业务场景
[描述业务场景和需求]

## 接口设计
1. [接口1名称]
   - 方法: POST
   - 路径: /api/[module]/[action]
   - 参数: { [参数定义] }
   - 返回: { [返回定义] }

2. [接口2名称]
   - ...

## 特殊处理
- [说明特殊的请求处理]
- [说明特殊的响应处理]
- [说明错误处理策略]
```

---

## 四、生成Store

### 4.1 生成Pinia Store

```
为[模块名称]生成Pinia Store:

## State定义
- list: 列表数据
- detail: 详情数据
- loading: 加载状态
- pagination: 分页信息 { page, pageSize, total }
- filter: 筛选条件

## Getters定义
- filteredList: 筛选后的列表
- listById: ID查找的映射

## Actions定义
- fetchList(): 获取列表
- fetchDetail(id): 获取详情
- create(data): 创建
- update(id, data): 更新
- remove(id): 删除
- setFilter(filter): 设置筛选
- resetFilter(): 重置筛选
- setPagination(page, pageSize): 设置分页

## 代码要求
- 使用defineStore()
- TypeScript完整类型
- 异步操作用async/await
- 错误处理
- 加载状态管理

## 代码示例
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { [ModuleName], [ModuleName]Filter } from '../types'
import * as api from '../api'

export const use[Module]Store = defineStore('[module]', () => {
  // State
  const list = ref<[ModuleName][]>([])
  const loading = ref(false)
  const pagination = ref({
    page: 1,
    pageSize: 20,
    total: 0
  })

  // Actions
  const fetchList = async (filter?: [ModuleName]Filter) => {
    loading.value = true
    try {
      const res = await api.getList({
        page: pagination.value.page,
        pageSize: pagination.value.pageSize,
        ...filter
      })
      list.value = res.data.list
      pagination.value.total = res.data.total
    } finally {
      loading.value = false
    }
  }

  // ...其他actions

  return {
    list,
    loading,
    pagination,
    fetchList
    // ...其他导出
  }
})
```

---

## 五、生成测试

### 5.1 生成单元测试

```
为[组件名称/函数名称]生成单元测试:

## 测试框架
- Vitest
- Vue Test Utils

## 测试内容

### 组件测试
1. 组件渲染测试
   - 测试组件正常渲染
   - 测试props传递
   - 测试slots内容

2. 用户交互测试
   - 测试按钮点击
   - 测试表单提交
   - 测试输入变化

3. 表单验证测试
   - 测试必填项验证
   - 测试格式验证
   - 测试错误提示

4. 事件发射测试
   - 测试emit事件
   - 测试事件参数

### API测试
1. 正常情况测试
2. 错误情况测试
3. 边界条件测试

### 工具函数测试
1. 正常输入测试
2. 特殊输入测试
3. 边界值测试

## 覆盖率要求
- 代码覆盖率 >= 80%
- 分支覆盖率 >= 70%
- 函数覆盖率 >= 90%

## 代码示例
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import [ComponentName] from '../[ComponentName].vue'

describe('[ComponentName]', () => {
  it('should render correctly', () => {
    const wrapper = mount([ComponentName])
    expect(wrapper.find('.title').text()).toBe('Hello')
  })

  it('should emit click event', async () => {
    const wrapper = mount([ComponentName])
    await wrapper.find('button').trigger('click')
    expect(wrapper.emitted('click')).toBeTruthy()
  })
})
```

---

### 5.2 生成E2E测试

```
为[模块名称]生成E2E测试:

## 测试工具
- Playwright

## 测试场景
1. 用户登录
2. 导航到列表页
3. 创建新记录
4. 编辑记录
5. 删除记录
6. 查看详情
7. 筛选和搜索
8. 分页

## 测试代码示例
import { test, expect } from '@playwright/test'

test.describe('[Module Name]', () => {
  test.beforeEach(async ({ page }) => {
    // 登录
    await page.goto('/login')
    await page.fill('input[name="username"]', 'admin')
    await page.fill('input[name="password"]', 'password')
    await page.click('button[type="submit"]')
  })

  test('should create new record', async ({ page }) => {
    await page.goto('/[module]')
    await page.click('text=新增')
    await page.fill('[name="field1"]', 'Test Value')
    await page.click('text=提交')
    await expect(page.locator('.el-message--success')).toBeVisible()
  })

  test('should delete record', async ({ page }) => {
    await page.goto('/[module]')
    await page.click('tbody tr:first-child button:has-text("删除")')
    await page.click('text=确定')
    await expect(page.locator('.el-message--success')).toBeVisible()
  })
})
```

---

## 六、代码优化

### 6.1 性能优化

```
优化以下代码的性能:

## 文件位置
[文件路径]

## 性能问题
1. [问题描述]
2. [问题描述]

## 优化要求
1. 减少不必要的重渲染
2. 使用计算属性缓存
3. 防抖节流处理
4. 虚拟滚动(长列表)
5. 懒加载组件
6. 图片懒加载
7. 代码分割

## 原始代码
[粘贴原始代码]

请分析性能瓶颈并提供优化后的代码。
```

---

### 6.2 代码重构

```
重构以下代码:

## 文件位置
[文件路径]

## 重构目标
1. 提取重复逻辑
2. 改善命名规范
3. 优化代码结构
4. 提高可读性
5. 增强可维护性

## 原始代码
[粘贴原始代码]

请提供重构后的代码,并说明改进点。
```

---

### 6.3 TypeScript类型优化

```
为以下代码添加完整的TypeScript类型:

## 文件位置
[文件路径]

## 类型要求
1. 添加interface/type定义
2. 添加泛型类型
3. 添加枚举类型
4. 添加类型守卫
5. 避免使用any

## 原始代码
[粘贴原始代码]

请添加完整的类型定义。
```

---

## 七、Bug修复

### 7.1 描述Bug请求修复

```
## Bug描述
[详细描述bug现象]

## 复现步骤
1. 步骤1
2. 步骤2
3. 步骤3

## 期望行为
[描述期望的正确行为]

## 实际行为
[描述实际发生的错误行为]

## 错误信息
[粘贴错误日志或截图]

## 相关代码
[粘贴相关代码片段]

## 环境信息
- 浏览器: [浏览器名称和版本]
- Node版本: [版本号]
- 其他: [其他环境信息]

请分析bug原因并提供修复方案。
```

---

### 7.2 分析错误日志

```
请分析以下错误日志,找出问题所在:

## 错误日志
[粘贴完整的错误日志]

## 代码上下文
[粘贴相关代码]

请提供:
1. 错误原因分析
2. 修复方案
3. 预防措施(如何避免类似错误)
```

---

## 八、文档生成

### 8.1 生成API文档

```
为[模块名称]生成API文档:

## 文档格式
Markdown格式

## 文档内容
每个接口包含:
- 接口名称
- 接口描述
- 请求方法
- 请求URL
- 请求参数(表格形式: 参数名、类型、必填、说明)
- 请求示例
- 响应参数(表格形式)
- 响应示例
- 错误码说明
- 注意事项

## 参考代码
src/modules/[module]/api/index.ts
```

---

### 8.2 生成使用文档

```
为[模块名称/组件名称]生成使用文档:

## 目标用户
- 开发者
- 最终用户

## 文档内容
1. 功能概述
2. 安装/配置说明
3. 快速开始(代码示例)
4. API参考
5. 配置选项
6. 常见问题(FAQ)
7. 示例代码
8. 截图/演示

## 文档风格
- 清晰简洁
- 图文并茂
- 代码示例完整可运行
- 分步骤说明
```

---

### 8.3 生成代码注释

```
为以下代码添加详细注释:

## 注释要求
1. 文件头部注释(功能说明、作者、日期)
2. 函数JSDoc注释(参数、返回值、说明)
3. 复杂逻辑注释(为什么这样写)
4. 关键步骤注释
5. 遵循JSDoc规范

## 代码文件
[文件路径]

请为代码添加完整的注释。
```

---

## 九、特殊场景

### 9.1 树形数据处理

```
生成树形数据处理工具函数:

## 功能需求
1. 扁平数组转树形结构
2. 树形结构转扁平数组
3. 查找节点路径
4. 计算节点层级
5. 统计子节点数量
6. 遍历树形结构

## 数据结构
interface TreeNode {
  id: string
  parentId: string | null
  name: string
  children?: TreeNode[]
}

## 代码要求
- TypeScript完整类型
- 纯函数(无副作用)
- 性能优化(避免重复计算)
- 边界情况处理
- 完整的单元测试
```

---

### 9.2 权限控制

```
实现权限控制系统:

## 权限模型
- 角色(Role): 管理员、部门管理员、普通用户
- 权限(Permission): 增删改查、审核
- 资源(Resource): 数据、页面、按钮

## 功能需求
1. 权限判断函数
2. 路由权限守卫
3. 按钮权限指令
4. 数据权限过滤
5. 权限缓存

## 代码要求
- 提供usePermission组合式函数
- 提供v-permission指令
- 完整的TypeScript类型
- 性能优化
```

---

### 9.3 审批流程引擎

```
实现审批流程引擎:

## 流程要素
- 状态机设计
- 多级审批
- 并行审批
- 条件分支
- 审批超时

## 功能需求
1. 流程定义
2. 流程启动
3. 审批操作(通过/驳回/转交)
4. 流程撤回
5. 流程查询
6. 消息通知

## 技术要求
- 状态机模式
- 责任链模式
- 事务处理
- 并发控制
```

---

### 9.4 数据导出

```
实现数据导出功能:

## 导出格式
- Excel (.xlsx)
- CSV (.csv)
- PDF (.pdf)

## 功能需求
1. 导出当前页数据
2. 导出全部数据
3. 自定义导出字段
4. 导出样式设置
5. 大数据量导出优化

## 技术方案
- Excel: 使用xlsx库
- CSV: 手动生成
- PDF: 使用jspdf库
- 大数据: 流式导出 + 分片处理
```

---

## 使用建议

### 提示词编写技巧

1. **明确性**: 清晰描述需求,避免歧义
2. **具体性**: 提供具体的示例和期望
3. **上下文**: 提供足够的背景信息
4. **结构化**: 使用结构化格式(列表、表格等)
5. **迭代**: 根据AI输出调整提示词

### 最佳实践

1. ✅ 先阅读规范文档,再生成代码
2. ✅ 从小模块开始,逐步复杂化
3. ✅ 保留AI生成的代码,人工审查后使用
4. ✅ 建立代码复用库,避免重复生成
5. ✅ 记录有效的提示词模板

### 常见问题

**Q: AI生成的代码不符合要求?**
A: 增加提示词的详细程度,提供更多示例和约束条件

**Q: 如何提高代码质量?**
A: 明确代码规范要求,添加代码审查步骤

**Q: 如何处理复杂业务逻辑?**
A: 拆分成多个小任务,逐步实现

**Q: AI不理解项目结构?**
A: 提供参考文件路径,说明项目架构

---

**文档版本**: v1.0.0
**最后更新**: 2026-01-09
**维护者**: OA系统开发团队
