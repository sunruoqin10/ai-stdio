# 费用报销模块 - UI/UX设计规范

> **文档类型**: UI/UX设计规范 (Design Specification)
> **模块类型**: 审批流程
> **设计风格**: 企业级B端设计
> **设计工具**: Element Plus + ECharts
> **创建日期**: 2026-01-09
> **版本**: v1.0.0

---

## 1. 设计原则

### 1.1 核心设计理念
- **效率优先**: 减少操作步骤,提高报销效率
- **清晰明了**: 审批流程透明,状态展示清晰
- **容错性强**: 提供完善的验证和提示
- **专业规范**: 符合财务审批的专业要求

### 1.2 设计目标
- 降低员工报销操作门槛
- 提高审批人员工作效率
- 确保数据录入准确性
- 提供良好的视觉体验

---

## 2. 整体布局

### 2.1 页面结构

```
┌─────────────────────────────────────────────────────────┐
│  面包屑导航: 首页 > 审批管理 > 费用报销                  │
├─────────────────────────────────────────────────────────┤
│  Tab栏: [我的报销] [待我审批] [报销统计] [打款管理]      │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  主内容区                                                │
│  - 表格/表单/图表                                       │
│                                                           │
└─────────────────────────────────────────────────────────┘
```

### 2.2 响应式布局
- **桌面端**: 左侧导航 + 右侧内容
- **平板端**: 顶部导航 + 内容区
- **移动端**: 汉堡菜单 + 单列布局

---

## 3. 页面设计

### 3.1 我的报销列表页

#### 3.1.1 页面布局

```
┌────────────────────────────────────────────────────────────┐
│  操作栏:                                                   │
│  [新建报销] [刷新]  状态筛选:[全部▼]  日期: [2026-01] ~ [01] │
├────────────────────────────────────────────────────────────┤
│  报销单列表:                                               │
│  ┌──────┬────────┬──────────┬────────┬───────┬────────┐  │
│  │报销单号│  类型  │   金额   │  状态  │申请日期│  操作  │  │
│  ├──────┼────────┼──────────┼────────┼───────┼────────┤  │
│  │EXP... │差旅费  │¥1,200.00 │已打款  │01-09  │查看详情│  │
│  │EXP... │招待费  │¥800.00   │审批中  │01-08  │查看详情│  │
│  └──────┴────────┴──────────┴────────┴───────┴────────┘  │
│  分页: < 1 2 3 4 5 ... 20 >  共200条                      │
└────────────────────────────────────────────────────────────┘
```

#### 3.1.2 状态标签样式

```typescript
// 状态标签配置
const statusConfig = {
  draft: {
    text: '草稿',
    color: '#909399',
    bgColor: '#F4F4F5',
    icon: 'Edit'
  },
  dept_pending: {
    text: '部门审批',
    color: '#E6A23C',
    bgColor: '#FDF6EC',
    icon: 'Clock'
  },
  finance_pending: {
    text: '财务审批',
    color: '#409EFF',
    bgColor: '#ECF5FF',
    icon: 'Money'
  },
  paid: {
    text: '已打款',
    color: '#67C23A',
    bgColor: '#F0F9FF',
    icon: 'Check'
  },
  rejected: {
    text: '已驳回',
    color: '#F56C6C',
    bgColor: '#FEF0F0',
    icon: 'Close'
  }
}
```

#### 3.1.3 操作按钮
- **查看详情**: 主按钮,蓝色
- **编辑**: 草稿状态显示
- **提交**: 草稿状态显示
- **撤回**: 审批中状态显示
- **删除**: 仅草稿状态显示

---

### 3.2 新建报销单页

#### 3.2.1 页面布局

```
┌────────────────────────────────────────────────────────────┐
│  新建报销单                              [保存草稿] [提交]  │
├────────────────────────────────────────────────────────────┤
│  第一步: 基本信息                                          │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  报销类型: [差旅费 ▼] *                              │ │
│  │  费用发生日期: [2026-01-05] *                        │ │
│  │  报销事由: [参加客户会议，前往北京洽谈合作] *         │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  第二步: 费用明细                          [+ 添加明细]   │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  序号  费用说明      金额      日期      分类      操作│ │
│  │   1    北京往返机票  ¥1,200   01-05    交通费   删除 │ │
│  │   2    住宿费        ¥400     01-05    住宿费   删除 │ │
│  └──────────────────────────────────────────────────────┘ │
│  小计: ¥1,600.00                                          │
│                                                            │
│  第三步: 发票上传                           [+ 上传发票]  │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  发票1:                                               │ │
│  │  ┌────────┐  发票类型:[增值税普通发票 ▼]            │ │
│  │  │        │  发票号码: [12345678]                    │ │
│  │  │ 缩略图 │  发票金额: [¥1,200.00]                   │ │
│  │  │        │  开票日期: [2026-01-05]                  │ │
│  │  └────────┘  [OCR识别] [删除]                        │ │
│  └──────────────────────────────────────────────────────┘ │
│  发票总金额: ¥1,600.00                                    │
│                                                            │
│  总计: ¥1,600.00                                          │
└────────────────────────────────────────────────────────────┘
```

#### 3.2.2 表单验证提示
- 实时验证: 失焦时验证
- 错误提示: 红色边框 + 底部文字
- 成功提示: 绿色对勾图标
- 警告提示: 橙色感叹号

```scss
// 表单验证样式
.form-item {
  .el-input.is-error {
    border-color: #F56C6C;

    & + .error-message {
      color: #F56C6C;
      font-size: 12px;
      margin-top: 4px;
    }
  }

  .el-input.is-success {
    border-color: #67C23A;

    &::after {
      content: '✓';
      position: absolute;
      right: 10px;
      color: #67C23A;
    }
  }
}
```

---

### 3.3 报销单详情页

#### 3.3.1 页面布局

```
┌────────────────────────────────────────────────────────────┐
│  报销单详情                                  [编辑] [打印]  │
├────────────────────────────────────────────────────────────┤
│  基本信息                                                  │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  报销单号: EXP202601090001                            │ │
│  │  报销人: 张三 (技术部)                                 │ │
│  │  报销类型: 差旅费                                      │ │
│  │  申请日期: 2026-01-09                                  │ │
│  │  费用发生日期: 2026-01-05                              │ │
│  │  报销事由: 参加客户会议，前往北京洽谈合作              │ │
│  │  总金额: ¥1,600.00                                    │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  费用明细                                                  │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  北京往返机票  ¥1,200.00  2026-01-05  交通费         │ │
│  │  住宿费        ¥400.00    2026-01-05  住宿费         │ │
│  │  ─────────────────────────────────────────────────    │ │
│  │  合计          ¥1,600.00                              │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  发票信息                                                  │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  [发票图片缩略图]                                      │ │
│  │  类型: 增值税普通发票                                 │ │
│  │  号码: 12345678                                       │ │
│  │  金额: ¥1,200.00                                      │ │
│  │  日期: 2026-01-05                                     │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  审批流程                                                  │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  ● 提交申请          张三           2026-01-09 10:00  │ │
│  │  ✓ 部门审批通过      李主管         2026-01-09 14:30  │ │
│  │    审批意见: 同意报销                               │ │
│  │  ○ 财务审批          王财务                        │ │
│  │  ○ 打款完成                                         │ │
│  └──────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────┘
```

#### 3.3.2 审批流程时间轴

```typescript
// 时间轴配置
const timelineConfig = {
  submit: {
    icon: 'EditDocument',
    color: '#409EFF',
    label: '提交申请'
  },
  dept_approved: {
    icon: 'CircleCheck',
    color: '#67C23A',
    label: '部门审批通过'
  },
  dept_rejected: {
    icon: 'CircleClose',
    color: '#F56C6C',
    label: '部门审批驳回'
  },
  finance_approved: {
    icon: 'CircleCheck',
    color: '#67C23A',
    label: '财务审批通过'
  },
  finance_rejected: {
    icon: 'CircleClose',
    color: '#F56C6C',
    label: '财务审批驳回'
  },
  paid: {
    icon: 'Wallet',
    color: '#67C23A',
    label: '打款完成'
  }
}
```

---

### 3.4 待我审批页

#### 3.4.1 页面布局

```
┌────────────────────────────────────────────────────────────┐
│  待我审批 (15)                                             │
│  筛选: [全部 ▼] [部门审批 ▼] [财务审批 ▼]                 │
├────────────────────────────────────────────────────────────┤
│  审批列表:                                                 │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  [ ] EXP202601090009                                  │ │
│  │     报销人: 张三 (技术部)    类型: 差旅费             │ │
│  │     金额: ¥1,200.00         申请时间: 2026-01-09     │ │
│  │     报销事由: 参加客户会议，前往北京                  │ │
│  │     [通过] [驳回]                                     │ │
│  ├──────────────────────────────────────────────────────┤ │
│  │  [ ] EXP202601090008                                  │ │
│  │     报销人: 李四 (市场部)    类型: 招待费             │ │
│  │     金额: ¥800.00           申请时间: 2026-01-09     │ │
│  │     报销事由: 招待重要客户                            │ │
│  │     [通过] [驳回]                                     │ │
│  └──────────────────────────────────────────────────────┘ │
│  批量操作: [批量通过] [批量驳回]                          │
└────────────────────────────────────────────────────────────┘
```

#### 3.4.2 审批操作对话框

```
┌─────────────────────────────────────┐
│  审批确认                    [×]    │
├─────────────────────────────────────┤
│  报销单信息:                        │
│    报销人: 张三                     │
│    类型: 差旅费                     │
│    金额: ¥1,200.00                  │
│    报销事由: 参加客户会议            │
│                                     │
│  审批操作:                          │
│  ○ 通过  ● 驳回                     │
│                                     │
│  审批意见:                          │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│           [取消]  [确认]            │
└─────────────────────────────────────┘
```

---

### 3.5 报销统计页

#### 3.5.1 页面布局

```
┌────────────────────────────────────────────────────────────┐
│  报销统计                                  [导出Excel]     │
│  时间范围: [2026-01-01] ~ [2026-01-31]  [查询]            │
├────────────────────────────────────────────────────────────┤
│  统计概标:                                                 │
│  ┌────────┬────────┬────────┬────────┬────────┐          │
│  │总金额  │总笔数  │平均金额│最高金额│最低金额│          │
│  │¥50,000 │  25    │¥2,000  │¥5,000  │¥200   │          │
│  └────────┴────────┴────────┴────────┴────────┘          │
│                                                            │
│  图表展示:                                                 │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  Tab: [按部门] [按类型] [按月份]                    │  │
│  │                                                     │  │
│  │  [柱状图/饼图/折线图]                               │  │
│  │                                                     │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                            │
│  详细数据:                                                 │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  部门    金额(元)  笔数  占比   操作                │ │
│  │  技术部  20,000    10    40%    [查看详情]          │ │
│  │  市场部  15,000    8     30%    [查看详情]          │ │
│  │  销售部  10,000    5     20%    [查看详情]          │ │
│  │  行政部  5,000     2     10%    [查看详情]          │ │
│  └──────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────┘
```

#### 3.5.2 图表配色方案

```typescript
// ECharts主题配色
const chartColors = {
  primary: '#1890FF',
  success: '#52C41A',
  warning: '#FAAD14',
  danger: '#F5222D',
  info: '#13C2C2',

  // 渐变色
  gradient: {
    blue: ['#1890FF', '#096DD9'],
    green: ['#52C41A', '#389E0D'],
    orange: ['#FAAD14', '#D46B08'],
    red: ['#F5222D', '#CF1322']
  },

  // 部门颜色
  departments: [
    '#1890FF', '#52C41A', '#FAAD14', '#F5222D',
    '#13C2C2', '#722ED1', '#EB2F96', '#FA8C16'
  ]
}

// 柱状图配置
const barChartOption = {
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: ['技术部', '市场部', '销售部', '行政部'],
    axisLabel: {
      color: '#666',
      fontSize: 12
    }
  },
  yAxis: {
    type: 'value',
    name: '金额(元)',
    axisLabel: {
      color: '#666',
      fontSize: 12
    },
    splitLine: {
      lineStyle: {
        type: 'dashed',
        color: '#E8E8E8'
      }
    }
  },
  series: [{
    type: 'bar',
    data: [20000, 15000, 10000, 5000],
    itemStyle: {
      color: {
        type: 'linear',
        x: 0,
        y: 0,
        x2: 0,
        y2: 1,
        colorStops: [{
          offset: 0,
          color: '#1890FF'
        }, {
          offset: 1,
          color: '#096DD9'
        }]
      },
      borderRadius: [4, 4, 0, 0]
    },
    barWidth: '40%',
    label: {
      show: true,
      position: 'top',
      formatter: '¥{c}'
    }
  }]
}

// 饼图配置
const pieChartOption = {
  tooltip: {
    trigger: 'item',
    formatter: '{b}: ¥{c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    right: '10%',
    top: 'center'
  },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    center: ['40%', '50%'],
    data: [
      { value: 20000, name: '差旅费' },
      { value: 15000, name: '招待费' },
      { value: 10000, name: '办公用品' },
      { value: 5000, name: '交通费' }
    ],
    itemStyle: {
      borderRadius: 6,
      borderColor: '#fff',
      borderWidth: 2
    },
    label: {
      show: true,
      formatter: '{b}\n¥{c}'
    },
    emphasis: {
      itemStyle: {
        shadowBlur: 10,
        shadowOffsetX: 0,
        shadowColor: 'rgba(0, 0, 0, 0.5)'
      }
    }
  }]
}

// 折线图配置
const lineChartOption = {
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: ['1月', '2月', '3月', '4月', '5月', '6月'],
    boundaryGap: false
  },
  yAxis: {
    type: 'value',
    name: '金额(元)'
  },
  series: [{
    type: 'line',
    data: [12000, 15000, 18000, 14000, 20000, 25000],
    smooth: true,
    symbol: 'circle',
    symbolSize: 8,
    lineStyle: {
      width: 3,
      color: '#1890FF'
    },
    itemStyle: {
      color: '#1890FF',
      borderColor: '#fff',
      borderWidth: 2
    },
    areaStyle: {
      color: {
        type: 'linear',
        x: 0,
        y: 0,
        x2: 0,
        y2: 1,
        colorStops: [{
          offset: 0,
          color: 'rgba(24, 144, 255, 0.3)'
        }, {
          offset: 1,
          color: 'rgba(24, 144, 255, 0.05)'
        }]
      }
    }
  }]
}
```

---

### 3.6 打款管理页

#### 3.6.1 页面布局

```
┌────────────────────────────────────────────────────────────┐
│  打款管理                                  [新建打款]       │
│  筛选: [全部 ▼] [待打款 ▼] [已完成 ▼] [失败 ▼]            │
├────────────────────────────────────────────────────────────┤
│  打款列表:                                                 │
│  ┌──────┬────────┬──────────┬────────┬───────┬────────┐  │
│  │打款ID│报销单号│  金额    │收款账号│  状态  │  操作  │  │
│  ├──────┼────────┼──────────┼────────┼───────┼────────┤  │
│  │  1   │EXP...  │¥1,200.00 │***1234 │待打款 │[打款] │  │
│  │  2   │EXP...  │¥800.00   │***5678 │已完成 │查看凭证│  │
│  └──────┴────────┴──────────┴────────┴───────┴────────┘  │
└────────────────────────────────────────────────────────────┘
```

#### 3.6.2 打款操作对话框

```
┌─────────────────────────────────────┐
│  打款操作                    [×]    │
├─────────────────────────────────────┤
│  报销单号: EXP202601090001          │
│  报销人: 张三                       │
│  打款金额: ¥1,200.00                │
│  收款账号: 6222 **** **** 1234      │
│  收款人: 张三                       │
│  开户行: 工商银行北京分行           │
│                                     │
│  打款方式:                          │
│  ● 银行转账  ○ 现金  ○ 支票        │
│                                     │
│  打款日期: [2026-01-10]             │
│                                     │
│  打款凭证上传:                      │
│  ┌────────────┐  [选择文件]         │
│  │            │                     │
│  │            │                     │
│  └────────────┘                     │
│                                     │
│  备注:                              │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│           [取消]  [确认打款]        │
└─────────────────────────────────────┘
```

---

## 4. 组件设计

### 4.1 发票上传组件

```vue
<template>
  <div class="invoice-uploader">
    <el-upload
      v-model:file-list="fileList"
      :action="uploadUrl"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      list-type="picture-card"
      :limit="10"
      accept=".jpg,.jpeg,.png,.pdf"
    >
      <el-icon><Plus /></el-icon>
      <template #tip>
        <div class="upload-tip">
          支持JPG/PNG/PDF格式,单文件不超过5MB
        </div>
      </template>
    </el-upload>

    <!-- 发票信息表单 -->
    <div v-for="invoice in invoices" :key="invoice.id" class="invoice-form">
      <el-form :model="invoice" label-width="100px">
        <el-form-item label="发票类型">
          <el-select v-model="invoice.type">
            <el-option label="增值税专用发票" value="vat_special" />
            <el-option label="增值税普通发票" value="vat_common" />
            <el-option label="电子发票" value="electronic" />
          </el-select>
        </el-form-item>
        <el-form-item label="发票号码">
          <el-input v-model="invoice.number" />
          <el-button @click="ocrRecognize(invoice)" :loading="ocrLoading">
            OCR识别
          </el-button>
        </el-form-item>
        <el-form-item label="发票金额">
          <el-input-number v-model="invoice.amount" :precision="2" />
        </el-form-item>
        <el-form-item label="开票日期">
          <el-date-picker v-model="invoice.date" type="date" />
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.invoice-uploader {
  :deep(.el-upload-list__item) {
    width: 148px;
    height: 148px;
    border-radius: 8px;
    overflow: hidden;
  }

  .upload-tip {
    font-size: 12px;
    color: #999;
    margin-top: 8px;
  }

  .invoice-form {
    margin-top: 16px;
    padding: 16px;
    background: #F5F7FA;
    border-radius: 8px;
  }
}
</style>
```

### 4.2 费用明细组件

```vue
<template>
  <div class="expense-items">
    <el-table :data="items" border>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="description" label="费用说明" min-width="200">
        <template #default="{ row }">
          <el-input v-model="row.description" placeholder="请输入费用说明" />
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" width="150">
        <template #default="{ row }">
          <el-input-number
            v-model="row.amount"
            :precision="2"
            :min="0"
            :controls="false"
            style="width: 100%"
          />
        </template>
      </el-table-column>
      <el-table-column prop="date" label="日期" width="180">
        <template #default="{ row }">
          <el-date-picker
            v-model="row.date"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
          />
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="150">
        <template #default="{ row }">
          <el-select v-model="row.category" placeholder="选择分类">
            <el-option label="交通费" value="transport" />
            <el-option label="住宿费" value="hotel" />
            <el-option label="餐费" value="meal" />
            <el-option label="其他" value="other" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ $index }">
          <el-button
            type="danger"
            link
            @click="removeItem($index)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="total-amount">
      <span>小计: </span>
      <span class="amount">¥{{ totalAmount.toFixed(2) }}</span>
    </div>

    <el-button type="primary" @click="addItem" style="margin-top: 16px">
      + 添加明细
    </el-button>
  </div>
</template>

<style lang="scss" scoped>
.expense-items {
  .total-amount {
    margin-top: 16px;
    text-align: right;
    font-size: 16px;
    font-weight: bold;

    .amount {
      color: #F56C6C;
      font-size: 20px;
    }
  }
}
</style>
```

### 4.3 审批流程组件

```vue
<template>
  <div class="approval-timeline">
    <el-timeline>
      <el-timeline-item
        v-for="(step, index) in steps"
        :key="index"
        :timestamp="step.timestamp"
        :type="step.type"
        :icon="step.icon"
        :color="step.color"
      >
        <div class="timeline-content">
          <div class="title">{{ step.title }}</div>
          <div v-if="step.approver" class="approver">
            审批人: {{ step.approver }}
          </div>
          <div v-if="step.opinion" class="opinion">
            审批意见: {{ step.opinion }}
          </div>
        </div>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<style lang="scss" scoped>
.approval-timeline {
  padding: 20px;

  .timeline-content {
    .title {
      font-size: 14px;
      font-weight: bold;
      color: #333;
      margin-bottom: 8px;
    }

    .approver {
      font-size: 13px;
      color: #666;
      margin-bottom: 4px;
    }

    .opinion {
      font-size: 13px;
      color: #999;
      padding: 8px;
      background: #F5F7FA;
      border-radius: 4px;
      margin-top: 8px;
    }
  }

  :deep(.el-timeline-item__timestamp) {
    color: #999;
    font-size: 12px;
  }
}
</style>
```

---

## 5. 交互设计

### 5.1 加载状态

#### 全局加载
```typescript
// 请求时显示loading
async function submitExpense() {
  const loading = ElLoading.service({
    lock: true,
    text: '提交中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    await api.submitExpense()
    ElMessage.success('提交成功')
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    loading.close()
  }
}
```

#### 按钮加载状态
```vue
<el-button :loading="submitting" @click="submitExpense">
  提交审批
</el-button>
```

### 5.2 消息提示

```typescript
// 成功提示
ElMessage.success('提交成功')

// 错误提示
ElMessage.error('发票号码已存在')

// 警告提示
ElMessage.warning('发票金额与申请金额不一致')

// 信息提示
ElMessage.info('正在处理中...')

// 确认对话框
ElMessageBox.confirm(
  '确定要提交审批吗?',
  '提示',
  {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }
)
```

### 5.3 表单验证

```typescript
// 实时验证
const rules = {
  type: [
    { required: true, message: '请选择报销类型', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入报销事由', trigger: 'blur' },
    { min: 10, max: 500, message: '长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  'invoice.number': [
    { required: true, message: '请输入发票号码', trigger: 'blur' },
    { pattern: /^\d{8}$|^\d{20}$/, message: '发票号码格式不正确', trigger: 'blur' }
  ]
}

// 自定义验证
const validateInvoiceAmount = (rule, value, callback) => {
  const invoiceTotal = invoices.value.reduce((sum, inv) => sum + inv.amount, 0)
  if (Math.abs(invoiceTotal - totalAmount.value) > 0.01) {
    callback(new Error('发票总金额与申请金额不一致'))
  } else {
    callback()
  }
}
```

---

## 6. 响应式设计

### 6.1 断点设置

```scss
// 断点定义
$breakpoints: (
  'mobile': 768px,
  'tablet': 1024px,
  'desktop': 1280px
);

// 响应式混合
@mixin respond-to($breakpoint) {
  @if map-has-key($breakpoints, $breakpoint) {
    @media (max-width: map-get($breakpoints, $breakpoint)) {
      @content;
    }
  }
}

// 使用示例
.container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;

  @include respond-to('tablet') {
    grid-template-columns: repeat(2, 1fr);
  }

  @include respond-to('mobile') {
    grid-template-columns: 1fr;
  }
}
```

### 6.2 移动端适配

```vue
<template>
  <div class="expense-list">
    <!-- 桌面端 -->
    <el-table
      v-if="!isMobile"
      :data="list"
      border
    >
      <!-- 表格列 -->
    </el-table>

    <!-- 移动端卡片列表 -->
    <div v-else class="mobile-list">
      <div
        v-for="item in list"
        :key="item.id"
        class="mobile-card"
        @click="viewDetail(item.id)"
      >
        <div class="card-header">
          <span class="id">{{ item.id }}</span>
          <el-tag :type="getStatusType(item.status)">
            {{ getStatusText(item.status) }}
          </el-tag>
        </div>
        <div class="card-body">
          <div class="row">
            <span class="label">类型:</span>
            <span class="value">{{ item.type }}</span>
          </div>
          <div class="row">
            <span class="label">金额:</span>
            <span class="value amount">¥{{ item.amount }}</span>
          </div>
          <div class="row">
            <span class="label">日期:</span>
            <span class="value">{{ item.applyDate }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.mobile-list {
  .mobile-card {
    margin-bottom: 12px;
    padding: 16px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      padding-bottom: 12px;
      border-bottom: 1px solid #EBEEF5;

      .id {
        font-weight: bold;
        color: #333;
      }
    }

    .card-body {
      .row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;
        font-size: 14px;

        .label {
          color: #999;
        }

        .value {
          color: #333;

          &.amount {
            color: #F56C6C;
            font-weight: bold;
          }
        }
      }
    }
  }
}
</style>
```

---

## 7. 无障碍设计

### 7.1 键盘导航
- Tab键: 表单元素间切换
- Enter键: 提交表单
- Esc键: 关闭对话框
- 方向键: 表格行导航

### 7.2 语义化HTML
```vue
<!-- 使用语义化标签 -->
<header>页面标题</header>
<nav>导航菜单</nav>
<main>主要内容</main>
<aside>侧边栏</aside>
<footer>页脚</footer>

<!-- 表单标签关联 -->
<label for="expense-type">报销类型</label>
<select id="expense-type">
  <option>差旅费</option>
</select>
```

### 7.3 ARIA属性
```vue
<button
  aria-label="删除发票"
  @click="removeInvoice"
>
  <el-icon><Delete /></el-icon>
</button>

<div
  role="alert"
  aria-live="polite"
  v-if="errorMessage"
>
  {{ errorMessage }}
</div>
```

---

## 8. 性能优化

### 8.1 图片懒加载
```vue
<el-image
  :src="invoice.imageUrl"
  :preview-src-list="[invoice.imageUrl]"
  lazy
  fit="cover"
/>
```

### 8.2 虚拟滚动
```vue
<el-table-v2
  :columns="columns"
  :data="data"
  :width="700"
  :height="400"
  fixed
/>
```

### 8.3 防抖节流
```typescript
import { debounce, throttle } from 'lodash-es'

// 搜索防抖
const search = debounce((keyword) => {
  loadData(keyword)
}, 300)

// 滚动节流
const handleScroll = throttle(() => {
  loadMore()
}, 200)
```

---

## 9. 设计资源

### 9.1 图标库
- Element Plus Icons
- 自定义SVG图标

### 9.2 配色方案
```scss
// 主色
$primary-color: #409EFF;

// 功能色
$success-color: #67C23A;
$warning-color: #E6A23C;
$danger-color: #F56C6C;
$info-color: #909399;

// 中性色
$text-primary: #303133;
$text-regular: #606266;
$text-secondary: #909399;
$text-placeholder: #C0C4CC;

// 边框色
$border-base: #DCDFE6;
$border-light: #E4E7ED;
$border-lighter: #EBEEF5;
$border-extra-light: #F2F6FC;

// 背景色
$bg-color: #F5F7FA;
```

### 9.3 字体规范
```scss
// 字体大小
$font-size-large: 18px;
$font-size-medium: 16px;
$font-size-base: 14px;
$font-size-small: 13px;
$font-size-extra-small: 12px;

// 行高
$line-height-base: 1.5;
$line-height-small: 1.2;

// 字重
$font-weight-normal: 400;
$font-weight-medium: 500;
$font-weight-bold: 600;
```

---

**文档版本**: v1.0.0
**最后更新**: 2026-01-09
