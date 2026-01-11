# 费用报销模块 - 技术实现规范

> **文档类型**: 技术实现规范 (Technical Specification)
> **模块类型**: 审批流程
> **技术栈**: Spring Boot 3.x + Vue 3 + MySQL
> **创建日期**: 2026-01-09
> **版本**: v1.0.0

---

## 1. 技术架构

### 1.1 整体架构
采用经典三层架构模式:
- **表现层** (Presentation Layer): Vue 3 + Element Plus
- **业务逻辑层** (Business Logic Layer): Spring Boot Service
- **数据访问层** (Data Access Layer): Spring Data JPA + MyBatis

### 1.2 技术选型
- **后端框架**: Spring Boot 3.2
- **前端框架**: Vue 3.4 + TypeScript
- **数据库**: MySQL 8.0
- **ORM框架**: Spring Data JPA
- **文件存储**: 本地存储 / MinIO
- **图表库**: ECharts 5.x
- **OCR服务**: 百度OCR API / 腾讯云OCR

---

## 2. 数据库设计

### 2.1 主表: expense (报销单表)

```sql
CREATE TABLE expense (
    id VARCHAR(32) PRIMARY KEY COMMENT '报销单号: EXP+YYYYMMDD+序号',
    applicant_id VARCHAR(32) NOT NULL COMMENT '报销人ID',
    department_id VARCHAR(32) NOT NULL COMMENT '部门ID',
    type VARCHAR(50) NOT NULL COMMENT '报销类型: travel/hospitality/office/transport/other',
    amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    reason TEXT NOT NULL COMMENT '报销事由',
    apply_date DATE NOT NULL COMMENT '申请日期',
    expense_date DATE NOT NULL COMMENT '费用发生日期',

    -- 审批状态
    status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态: draft/dept_pending/finance_pending/paid/rejected',

    -- 部门审批
    dept_approver_id VARCHAR(32) COMMENT '部门审批人ID',
    dept_approver_name VARCHAR(100) COMMENT '部门审批人姓名',
    dept_approval_status VARCHAR(20) COMMENT '部门审批状态: pending/approved/rejected',
    dept_approval_opinion TEXT COMMENT '部门审批意见',
    dept_approval_time DATETIME COMMENT '部门审批时间',

    -- 财务审批
    finance_approver_id VARCHAR(32) COMMENT '财务审批人ID',
    finance_approver_name VARCHAR(100) COMMENT '财务审批人姓名',
    finance_approval_status VARCHAR(20) COMMENT '财务审批状态: pending/approved/rejected',
    finance_approval_opinion TEXT COMMENT '财务审批意见',
    finance_approval_time DATETIME COMMENT '财务审批时间',

    -- 打款信息
    payment_date DATE COMMENT '打款时间',
    payment_proof VARCHAR(500) COMMENT '打款凭证URL',

    -- 审计字段
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME COMMENT '删除时间',

    INDEX idx_applicant (applicant_id),
    INDEX idx_department (department_id),
    INDEX idx_status (status),
    INDEX idx_apply_date (apply_date),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用报销单表';
```

### 2.2 明细表: expense_item (费用明细表)

```sql
CREATE TABLE expense_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    expense_id VARCHAR(32) NOT NULL COMMENT '报销单号',
    description VARCHAR(500) NOT NULL COMMENT '费用说明',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    expense_date DATE NOT NULL COMMENT '发生日期',
    category VARCHAR(100) COMMENT '费用分类',
    sort_order INT DEFAULT 0 COMMENT '排序序号',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_expense (expense_id),
    FOREIGN KEY (expense_id) REFERENCES expense(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用明细表';
```

### 2.3 发票表: expense_invoice (发票表)

```sql
CREATE TABLE expense_invoice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    expense_id VARCHAR(32) NOT NULL COMMENT '报销单号',
    invoice_type VARCHAR(20) NOT NULL COMMENT '发票类型: vat_special/vat_common/electronic',
    invoice_number VARCHAR(50) NOT NULL COMMENT '发票号码',
    amount DECIMAL(10,2) NOT NULL COMMENT '发票金额',
    invoice_date DATE NOT NULL COMMENT '开票日期',
    image_url VARCHAR(500) COMMENT '发票图片URL',
    verified BOOLEAN DEFAULT FALSE COMMENT '是否已验真',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_invoice_number (invoice_number),
    INDEX idx_expense (expense_id),
    INDEX idx_invoice_number (invoice_number),
    FOREIGN KEY (expense_id) REFERENCES expense(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票表';
```

### 2.4 打款表: expense_payment (打款记录表)

```sql
CREATE TABLE expense_payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    expense_id VARCHAR(32) NOT NULL COMMENT '报销单号',
    amount DECIMAL(10,2) NOT NULL COMMENT '打款金额',
    payment_method VARCHAR(20) NOT NULL DEFAULT 'bank_transfer' COMMENT '打款方式: bank_transfer/cash/check',
    payment_date DATE NOT NULL COMMENT '打款日期',
    bank_account VARCHAR(100) COMMENT '收款账号',
    proof VARCHAR(500) COMMENT '打款凭证URL',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/completed/failed',
    remark TEXT COMMENT '备注',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_expense (expense_id),
    INDEX idx_status (status),
    INDEX idx_payment_date (payment_date),
    FOREIGN KEY (expense_id) REFERENCES expense(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打款记录表';
```

---

## 3. 核心业务逻辑

### 3.1 数据实体定义

```typescript
// 报销单实体
interface Expense {
  id: string
  applicantId: string
  departmentId: string
  type: 'travel' | 'hospitality' | 'office' | 'transport' | 'other'
  amount: number
  items: ExpenseItem[]
  invoices: Invoice[]
  reason: string
  applyDate: string
  expenseDate: string

  status: 'draft' | 'dept_pending' | 'finance_pending' | 'paid' | 'rejected'
  departmentApproval?: {
    approverId: string
    approverName: string
    status: 'pending' | 'approved' | 'rejected'
    opinion?: string
    timestamp?: string
  }
  financeApproval?: {
    approverId: string
    approverName: string
    status: 'pending' | 'approved' | 'rejected'
    opinion?: string
    timestamp?: string
  }

  paymentDate?: string
  paymentProof?: string

  createdAt: string
  updatedAt: string
}

// 费用明细
interface ExpenseItem {
  id?: number
  description: string
  amount: number
  date: string
  category: string
}

// 发票
interface Invoice {
  id?: number
  type: 'vat_special' | 'vat_common' | 'electronic'
  number: string
  amount: number
  date: string
  imageUrl: string
  verified: boolean
}

// 打款记录
interface PaymentRecord {
  id?: number
  expenseId: string
  amount: number
  paymentMethod: 'bank_transfer' | 'cash' | 'check'
  paymentDate: string
  bankAccount?: string
  proof?: string
  status: 'pending' | 'completed' | 'failed'
}
```

### 3.2 大额加签规则实现

```typescript
interface LargeAmountRule {
  amount: number
  additionalApprovers: string[]
}

// 加签规则配置
const largeAmountRules: LargeAmountRule[] = [
  { amount: 5000, additionalApprovers: ['general_manager'] },
  { amount: 10000, additionalApprovers: ['general_manager', 'special_approver'] }
]

// 月度累计规则
const monthlyAccumulationRules: LargeAmountRule[] = [
  { amount: 20000, additionalApprovers: ['general_manager', 'special_approver'] },
  { amount: 50000, additionalApprovers: ['general_manager', 'special_approver', 'chairman'] }
]

// 检查单笔金额加签
function checkSingleAmount(amount: number): string[] {
  const approvers = ['department_leader', 'finance']

  for (const rule of largeAmountRules) {
    if (amount > rule.amount) {
      approvers.push(...rule.additionalApprovers)
    }
  }

  return approvers
}

// 检查月度累计加签
async function checkMonthlyAccumulation(
  employeeId: string,
  currentAmount: number
): Promise<string[]> {
  const monthStart = new Date()
  monthStart.setDate(1)
  monthStart.setHours(0, 0, 0, 0)

  const monthlyExpenses = await getExpensesByEmployeeAndDateRange(
    employeeId,
    monthStart,
    new Date(),
    ['paid']
  )

  const totalAmount = monthlyExpenses.reduce((sum, e) => sum + e.amount, 0) + currentAmount

  const approvers = ['department_leader', 'finance']

  for (const rule of monthlyAccumulationRules) {
    if (totalAmount > rule.amount) {
      approvers.push(...rule.additionalApprovers)
    }
  }

  return approvers
}

// 获取完整审批流程
async function getApprovalFlow(employeeId: string, amount: number): Promise<string[]> {
  const singleApprovers = checkSingleAmount(amount)
  const accumulatedApprovers = await checkMonthlyAccumulation(employeeId, amount)

  // 合并去重
  return Array.from(new Set([...singleApprovers, ...accumulatedApprovers]))
}
```

### 3.3 发票验证逻辑

```typescript
// 发票唯一性验证
async function validateInvoiceUnique(invoiceNumber: string): Promise<boolean> {
  const existing = await getInvoiceByNumber(invoiceNumber)
  if (existing) {
    throw new Error(`发票号 ${invoiceNumber} 已被使用,不能重复报销`)
  }
  return true
}

// 发票金额一致性验证
function validateInvoiceAmount(invoices: Invoice[], totalAmount: number): boolean {
  const invoiceTotal = invoices.reduce((sum, inv) => sum + inv.amount, 0)

  if (Math.abs(invoiceTotal - totalAmount) > 0.01) {
    throw new Error(`发票总金额(${invoiceTotal})与申请金额(${totalAmount})不一致`)
  }

  return true
}

// 发票号码格式验证
function validateInvoiceNumberFormat(invoiceNumber: string): boolean {
  // 8位或20位数字
  const regex = /^\d{8}$|^\d{20}$/
  if (!regex.test(invoiceNumber)) {
    throw new Error('发票号码格式不正确,应为8位或20位数字')
  }
  return true
}

// 综合发票验证
async function validateInvoices(invoices: Invoice[], totalAmount: number): Promise<void> {
  for (const invoice of invoices) {
    validateInvoiceNumberFormat(invoice.number)
    await validateInvoiceUnique(invoice.number)
  }

  validateInvoiceAmount(invoices, totalAmount)
}
```

### 3.4 报销单生成逻辑

```typescript
// 生成报销单号
function generateExpenseId(): string {
  const now = new Date()
  const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
  const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
  return `EXP${dateStr}${random}`
}

// 创建报销单
async function createExpense(data: Partial<Expense>): Promise<Expense> {
  // 验证发票
  await validateInvoices(data.invoices!, data.amount!)

  // 生成报销单号
  const expenseId = generateExpenseId()

  // 计算总金额
  const totalAmount = data.items!.reduce((sum, item) => sum + item.amount, 0)

  // 构建报销单对象
  const expense: Expense = {
    id: expenseId,
    applicantId: data.applicantId!,
    departmentId: data.departmentId!,
    type: data.type!,
    amount: totalAmount,
    items: data.items!,
    invoices: data.invoices!,
    reason: data.reason!,
    applyDate: new Date().toISOString().split('T')[0],
    expenseDate: data.expenseDate!,
    status: 'draft',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }

  // 保存到数据库
  await saveExpense(expense)

  return expense
}
```

### 3.5 审批流程逻辑

```typescript
// 提交审批
async function submitExpense(expenseId: string): Promise<void> {
  const expense = await getExpense(expenseId)

  if (expense.status !== 'draft') {
    throw new Error('只有草稿状态的报销单可以提交')
  }

  // 获取审批流程
  const approvers = await getApprovalFlow(expense.applicantId, expense.amount)

  // 更新状态为部门审批
  await updateExpense(expenseId, {
    status: 'dept_pending',
    departmentApproval: {
      approverId: approvers[0],
      status: 'pending'
    }
  })

  // 通知审批人
  await notifyApprover(approvers[0], expenseId)
}

// 部门审批
async function departmentApprove(
  expenseId: string,
  approved: boolean,
  opinion?: string
): Promise<void> {
  const expense = await getExpense(expenseId)

  if (expense.status !== 'dept_pending') {
    throw new Error('当前状态不允许部门审批')
  }

  if (approved) {
    // 检查是否需要更多审批
    const approvers = await getApprovalFlow(expense.applicantId, expense.amount)
    const currentIndex = approvers.indexOf('department_leader')

    if (currentIndex < approvers.length - 2) {
      // 还有其他加签审批人
      const nextApprover = approvers[currentIndex + 1]
      await updateExpense(expenseId, {
        status: 'dept_pending',
        departmentApproval: {
          approverId: getCurrentUserId(),
          approverName: getCurrentUserName(),
          status: 'approved',
          opinion,
          timestamp: new Date().toISOString()
        }
      })
      await notifyApprover(nextApprover, expenseId)
    } else {
      // 进入财务审批
      await updateExpense(expenseId, {
        status: 'finance_pending',
        departmentApproval: {
          approverId: getCurrentUserId(),
          approverName: getCurrentUserName(),
          status: 'approved',
          opinion,
          timestamp: new Date().toISOString()
        },
        financeApproval: {
          approverId: approvers[currentIndex + 1],
          status: 'pending'
        }
      })
      await notifyApprover('finance', expenseId)
    }
  } else {
    // 驳回
    await updateExpense(expenseId, {
      status: 'rejected',
      departmentApproval: {
        approverId: getCurrentUserId(),
        approverName: getCurrentUserName(),
        status: 'rejected',
        opinion,
        timestamp: new Date().toISOString()
      }
    })

    // 通知申请人
    await notifyApplicant(expense.applicantId, expenseId, 'rejected')
  }
}

// 财务审批
async function financeApprove(
  expenseId: string,
  approved: boolean,
  opinion?: string
): Promise<void> {
  const expense = await getExpense(expenseId)

  if (expense.status !== 'finance_pending') {
    throw new Error('当前状态不允许财务审批')
  }

  if (approved) {
    // 创建打款记录
    await createPayment(expenseId)

    // 更新状态
    await updateExpense(expenseId, {
      status: 'paid',
      financeApproval: {
        approverId: getCurrentUserId(),
        approverName: getCurrentUserName(),
        status: 'approved',
        opinion,
        timestamp: new Date().toISOString()
      }
    })
  } else {
    // 驳回
    await updateExpense(expenseId, {
      status: 'rejected',
      financeApproval: {
        approverId: getCurrentUserId(),
        approverName: getCurrentUserName(),
        status: 'rejected',
        opinion,
        timestamp: new Date().toISOString()
      }
    })

    // 通知申请人
    await notifyApplicant(expense.applicantId, expenseId, 'rejected')
  }
}
```

### 3.6 打款管理逻辑

```typescript
// 创建打款记录
async function createPayment(expenseId: string): Promise<PaymentRecord> {
  const expense = await getExpense(expenseId)

  if (expense.status !== 'finance_pending') {
    throw new Error('报销单未通过财务审批')
  }

  // 获取员工银行账户
  const employee = await getEmployee(expense.applicantId)
  const bankAccount = employee.bankAccount

  const payment: PaymentRecord = {
    expenseId,
    amount: expense.amount,
    paymentMethod: 'bank_transfer',
    paymentDate: new Date().toISOString().split('T')[0],
    bankAccount,
    status: 'pending'
  }

  await savePayment(payment)

  return payment
}

// 上传打款凭证
async function uploadPaymentProof(
  paymentId: number,
  proofImage: string
): Promise<void> {
  await updatePayment(paymentId, {
    proof: proofImage,
    status: 'completed'
  })

  const payment = await getPaymentById(paymentId)

  // 更新报销单状态
  await updateExpense(payment.expenseId, {
    status: 'paid',
    paymentDate: payment.paymentDate,
    paymentProof: proofImage
  })

  // 通知报销人
  const expense = await getExpense(payment.expenseId)
  await notifyEmployee(expense.applicantId, '您的报销已打款')
}
```

### 3.7 OCR识别逻辑

```typescript
// OCR识别发票
async function recognizeInvoice(imageUrl: string): Promise<Partial<Invoice>> {
  try {
    // 调用OCR服务 (示例: 百度OCR)
    const result = await baiduOcr.generalBasic(imageUrl)

    // 解析识别结果
    const invoice: Partial<Invoice> = {
      number: extractInvoiceNumber(result.words_result),
      amount: extractAmount(result.words_result),
      date: extractDate(result.words_result),
      type: determineInvoiceType(result.words_result)
    }

    return invoice
  } catch (error) {
    console.error('OCR识别失败:', error)
    throw new Error('发票识别失败,请手动输入')
  }
}

// 提取发票号码
function extractInvoiceNumber(wordsResult: any[]): string {
  const numberField = wordsResult.find(item =>
    item.words.includes('发票号码') || item.words.includes('No.')
  )
  return numberField?.words.replace(/\D/g, '') || ''
}

// 提取金额
function extractAmount(wordsResult: any[]): number {
  const amountField = wordsResult.find(item =>
    item.words.includes('价税合计') || item.words.includes('合计')
  )
  const amountStr = amountField?.words.replace(/[^\d.]/g, '') || '0'
  return parseFloat(amountStr)
}

// 提取日期
function extractDate(wordsResult: any[]): string {
  const dateField = wordsResult.find(item =>
    item.words.includes('开票日期') || item.words.includes('日期')
  )
  return dateField?.words || ''
}

// 判断发票类型
function determineInvoiceType(wordsResult: any[]): 'vat_special' | 'vat_common' | 'electronic' {
  const typeField = wordsResult.find(item =>
    item.words.includes('增值税专用发票')
  )
  return typeField ? 'vat_special' : 'vat_common'
}
```

---

## 4. API接口设计

### 4.1 报销单管理接口

#### 创建报销单
```
POST /api/expenses
Request Body:
{
  "type": "travel",
  "items": [
    {
      "description": "北京往返机票",
      "amount": 1200.00,
      "date": "2026-01-05",
      "category": "交通费"
    }
  ],
  "invoices": [
    {
      "type": "vat_common",
      "number": "12345678",
      "amount": 1200.00,
      "date": "2026-01-05",
      "imageUrl": "/uploads/invoice/xxx.jpg"
    }
  ],
  "reason": "参加客户会议",
  "expenseDate": "2026-01-05"
}

Response:
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": "EXP202601090001",
    "status": "draft",
    ...
  }
}
```

#### 提交审批
```
POST /api/expenses/{id}/submit
Response:
{
  "code": 200,
  "message": "提交成功",
  "data": {
    "id": "EXP202601090001",
    "status": "dept_pending"
  }
}
```

#### 查询报销单列表
```
GET /api/expenses?page=1&size=10&status=draft&applicantId=xxx
Response:
{
  "code": 200,
  "data": {
    "total": 100,
    "list": [...]
  }
}
```

#### 查询报销单详情
```
GET /api/expenses/{id}
Response:
{
  "code": 200,
  "data": {
    "id": "EXP202601090001",
    "items": [...],
    "invoices": [...],
    ...
  }
}
```

### 4.2 审批接口

#### 部门审批
```
POST /api/expenses/{id}/department-approval
Request Body:
{
  "approved": true,
  "opinion": "同意报销"
}

Response:
{
  "code": 200,
  "message": "审批成功",
  "data": {
    "status": "finance_pending"
  }
}
```

#### 财务审批
```
POST /api/expenses/{id}/finance-approval
Request Body:
{
  "approved": true,
  "opinion": "发票合规,同意打款"
}

Response:
{
  "code": 200,
  "message": "审批成功",
  "data": {
    "status": "paid"
  }
}
```

### 4.3 发票接口

#### 发票OCR识别
```
POST /api/expenses/invoices/ocr
Request Body:
{
  "imageUrl": "/uploads/invoice/xxx.jpg"
}

Response:
{
  "code": 200,
  "data": {
    "number": "12345678",
    "amount": 1200.00,
    "date": "2026-01-05",
    "type": "vat_common"
  }
}
```

#### 验证发票唯一性
```
GET /api/expenses/invoices/validate?number=12345678
Response:
{
  "code": 200,
  "data": {
    "valid": true,
    "message": "发票号可用"
  }
}
```

### 4.4 打款接口

#### 创建打款记录
```
POST /api/expenses/{id}/payment
Response:
{
  "code": 200,
  "data": {
    "id": 1,
    "expenseId": "EXP202601090001",
    "amount": 1200.00,
    "status": "pending"
  }
}
```

#### 上传打款凭证
```
POST /api/expenses/payments/{id}/proof
Request Body:
{
  "proofUrl": "/uploads/payment/xxx.jpg"
}

Response:
{
  "code": 200,
  "message": "上传成功"
}
```

### 4.5 统计接口

#### 按部门统计
```
GET /api/expenses/stats/department?startDate=2026-01-01&endDate=2026-01-31
Response:
{
  "code": 200,
  "data": [
    {
      "departmentId": "dept001",
      "departmentName": "技术部",
      "totalAmount": 50000.00,
      "count": 20,
      "avgAmount": 2500.00
    }
  ]
}
```

#### 按类型统计
```
GET /api/expenses/stats/type?startDate=2026-01-01&endDate=2026-01-31
Response:
{
  "code": 200,
  "data": [
    {
      "type": "travel",
      "totalAmount": 30000.00,
      "count": 15,
      "percentage": 60.0
    }
  ]
}
```

#### 按月份统计
```
GET /api/expenses/stats/monthly?year=2026
Response:
{
  "code": 200,
  "data": [
    {
      "month": "2026-01",
      "totalAmount": 50000.00,
      "count": 25
    }
  ]
}
```

---

## 5. 文件上传处理

### 5.1 文件上传配置

```yaml
# application.yml
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 50MB

file:
  upload:
    path: /data/oa/uploads
    url-prefix: /uploads
```

### 5.2 文件上传工具类

```typescript
// 文件上传服务
class FileUploadService {
  // 上传发票图片
  async uploadInvoiceImage(file: File): Promise<string> {
    // 验证文件类型
    const allowedTypes = ['image/jpeg', 'image/png', 'application/pdf']
    if (!allowedTypes.includes(file.type)) {
      throw new Error('只支持JPG、PNG、PDF格式')
    }

    // 验证文件大小
    const maxSize = 5 * 1024 * 1024 // 5MB
    if (file.size > maxSize) {
      throw new Error('文件大小不能超过5MB')
    }

    // 生成文件名
    const ext = file.name.split('.').pop()
    const filename = `invoice_${Date.now()}_${Math.random().toString(36).substr(2, 9)}.${ext}`

    // 保存文件
    const filepath = `/invoices/${filename}`
    await saveFile(file, filepath)

    // 返回URL
    return `/uploads${filepath}`
  }

  // 上传打款凭证
  async uploadPaymentProof(file: File): Promise<string> {
    const ext = file.name.split('.').pop()
    const filename = `payment_${Date.now()}_${Math.random().toString(36).substr(2, 9)}.${ext}`

    const filepath = `/payments/${filename}`
    await saveFile(file, filepath)

    return `/uploads${filepath}`
  }
}
```

---

## 6. 数据验证与安全

### 6.1 数据验证规则

```typescript
// 报销单验证
const expenseValidationRules = {
  type: {
    required: true,
    enum: ['travel', 'hospitality', 'office', 'transport', 'other']
  },
  items: {
    required: true,
    minLength: 1,
    validate: (items: ExpenseItem[]) => {
      return items.every(item => {
        return item.description &&
               item.amount > 0 &&
               item.date &&
               item.category
      })
    }
  },
  invoices: {
    required: true,
    minLength: 1,
    validate: async (invoices: Invoice[]) => {
      for (const invoice of invoices) {
        await validateInvoiceUnique(invoice.number)
      }
      return true
    }
  },
  reason: {
    required: true,
    minLength: 10,
    maxLength: 500
  },
  expenseDate: {
    required: true,
    validate: (date: string) => {
      return new Date(date) <= new Date()
    }
  }
}
```

### 6.2 权限控制

```typescript
// 权限定义
const expensePermissions = {
  // 创建报销单
  create: ['employee', 'admin'],

  // 查看自己的报销单
  viewOwn: ['employee', 'admin'],

  // 查看部门报销单
  viewDepartment: ['department_leader', 'admin'],

  // 查看所有报销单
  viewAll: ['finance', 'admin'],

  // 部门审批
  departmentApprove: ['department_leader', 'admin'],

  // 财务审批
  financeApprove: ['finance', 'admin'],

  // 打款操作
  payment: ['finance', 'admin'],

  // 删除报销单
  delete: ['admin']
}

// 权限检查中间件
function checkPermission(permission: string) {
  return (req, res, next) => {
    const userRole = req.user.role
    const allowedRoles = expensePermissions[permission]

    if (!allowedRoles.includes(userRole)) {
      return res.status(403).json({
        code: 403,
        message: '无权限访问'
      })
    }

    next()
  }
}
```

---

## 7. 性能优化

### 7.1 数据库优化
- 合理创建索引 (applicant_id, department_id, status, apply_date)
- 使用分页查询避免大数据量加载
- 统计查询使用缓存

### 7.2 缓存策略
```typescript
// 缓存配置
const cacheConfig = {
  // 审批流程缓存 (1小时)
  approvalFlow: {
    ttl: 3600,
    key: (employeeId: string, amount: number) =>
      `approval:flow:${employeeId}:${amount}`
  },

  // 统计数据缓存 (30分钟)
  stats: {
    ttl: 1800,
    key: (type: string, startDate: string, endDate: string) =>
      `stats:${type}:${startDate}:${endDate}`
  }
}
```

### 7.3 异步处理
```typescript
// OCR识别异步处理
async function recognizeInvoiceAsync(imageUrl: string): Promise<string> {
  const taskId = `ocr_${Date.now()}`

  // 提交异步任务
  await queue.add('invoice-ocr', {
    taskId,
    imageUrl
  })

  return taskId
}

// 查询OCR结果
async function getOCRResult(taskId: string): Promise<Invoice | null> {
  const result = await cache.get(`ocr:result:${taskId}`)
  return result ? JSON.parse(result) : null
}
```

---

## 8. 测试策略

### 8.1 单元测试
- 业务逻辑层测试 (Service层)
- 数据验证测试
- 工具类测试

### 8.2 集成测试
- API接口测试
- 数据库操作测试
- 文件上传测试

### 8.3 性能测试
- 并发提交测试
- 大数据量查询测试
- OCR识别性能测试

---

## 8. 数据字典集成实现

### 8.1 数据字典API封装

```typescript
// src/modules/dict/api/index.ts

import { http } from '@/utils/request'

/**
 * 字典数据项接口
 */
export interface DictItem {
  label: string
  value: string
  dictCode: string
  sort?: number
  remark?: string
}

/**
 * 获取指定类型的字典列表
 * @param dictCode 字典编码
 * @returns 字典项列表
 */
export async function getDictList(dictCode: string): Promise<DictItem[]> {
  return http.get<DictItem[]>(`/api/dict/${dictCode}`)
}

/**
 * 批量获取多个字典类型
 * @param dictCodes 字典编码数组
 * @returns 字典数据映射
 */
export async function getDictBatch(dictCodes: string[]): Promise<Record<string, DictItem[]>> {
  return http.post<Record<string, DictItem[]>>('/api/dict/batch', { dictCodes })
}

/**
 * 获取字典标签文本
 * @param dictCode 字典编码
 * @param value 字典值
 * @returns 字典标签
 */
export async function getDictLabel(dictCode: string, value: string): Promise<string> {
  return http.get<string>(`/api/dict/${dictCode}/label/${value}`)
}
```

### 8.2 Pinia字典Store

```typescript
// src/modules/dict/store/index.ts

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getDictBatch, getDictList, type DictItem } from '../api'

export const useDictStore = defineStore('dict', () => {
  // 字典数据缓存
  const dictData = ref<Record<string, DictItem[]>>({})

  // 缓存时间戳
  const cacheTime = ref<Record<string, number>>({})

  // 缓存过期时间(30分钟)
  const CACHE_EXPIRE = 30 * 60 * 1000

  /**
   * 批量加载字典数据
   */
  async function loadDicts(dictCodes: string[]) {
    const now = Date.now()
    const needLoad: string[] = []

    // 检查哪些字典需要重新加载
    for (const code of dictCodes) {
      const cached = cacheTime.value[code]
      if (!cached || now - cached > CACHE_EXPIRE) {
        needLoad.push(code)
      }
    }

    if (needLoad.length === 0) return

    // 批量加载
    const data = await getDictBatch(needLoad)

    for (const [code, items] of Object.entries(data)) {
      dictData.value[code] = items
      cacheTime.value[code] = now
    }
  }

  /**
   * 获取字典列表
   */
  function getDictList(dictCode: string): DictItem[] {
    return dictData.value[dictCode] || []
  }

  /**
   * 获取字典标签
   */
  function getLabel(dictCode: string, value: string): string {
    const items = getDictList(dictCode)
    const item = items.find(i => i.value === value)
    return item?.label || value
  }

  /**
   * 刷新指定字典
   */
  async function refreshDict(dictCode: string) {
    delete cacheTime.value[dictCode]
    await loadDicts([dictCode])
  }

  /**
   * 清空所有缓存
   */
  function clearCache() {
    dictData.value = {}
    cacheTime.value = {}
  }

  return {
    dictData,
    loadDicts,
    getDictList,
    getLabel,
    refreshDict,
    clearCache
  }
})
```

### 8.3 费用报销模块中使用字典

```typescript
// src/modules/expense/composables/useExpenseDict.ts

import { computed, onMounted } from 'vue'
import { useDictStore } from '@/modules/dict/store'

/**
 * 费用报销模块字典组合函数
 */
export function useExpenseDict() {
  const dictStore = useDictStore()

  // 组件挂载时预加载常用字典
  onMounted(async () => {
    await dictStore.loadDicts(['expense_type', 'expense_status', 'invoice_type'])
  })

  // 报销类型选项
  const expenseTypeOptions = computed(() =>
    dictStore.getDictList('expense_type')
  )

  // 报销状态选项
  const statusOptions = computed(() =>
    dictStore.getDictList('expense_status')
  )

  // 发票类型选项
  const invoiceTypeOptions = computed(() =>
    dictStore.getDictList('invoice_type')
  )

  // 动态加载费用分类字典
  async function loadCategoryOptions() {
    await dictStore.loadDicts(['expense_category'])
    return dictStore.getDictList('expense_category')
  }

  // 获取状态标签文本
  function getStatusLabel(value: string): string {
    return dictStore.getLabel('expense_status', value)
  }

  // 获取报销类型标签文本
  function getTypeLabel(value: string): string {
    return dictStore.getLabel('expense_type', value)
  }

  // 获取发票类型标签文本
  function getInvoiceTypeLabel(value: string): string {
    return dictStore.getLabel('invoice_type', value)
  }

  return {
    expenseTypeOptions,
    statusOptions,
    invoiceTypeOptions,
    loadCategoryOptions,
    getStatusLabel,
    getTypeLabel,
    getInvoiceTypeLabel
  }
}
```

### 8.4 筛选面板中使用字典

```vue
<!-- src/modules/expense/components/ExpenseFilter.vue -->
<template>
  <el-form :inline="true" :model="filterForm">
    <!-- 报销类型筛选 -->
    <el-form-item label="报销类型">
      <el-select v-model="filterForm.type" placeholder="请选择" clearable>
        <el-option
          v-for="item in expenseTypeOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>

    <!-- 状态筛选 -->
    <el-form-item label="状态">
      <el-select v-model="filterForm.status" placeholder="请选择" clearable>
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>

    <!-- 发票类型筛选 -->
    <el-form-item label="发票类型">
      <el-select v-model="filterForm.invoiceType" placeholder="请选择" clearable>
        <el-option
          v-for="item in invoiceTypeOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="handleFilter">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useExpenseDict } from '../composables/useExpenseDict'

const {
  expenseTypeOptions,
  statusOptions,
  invoiceTypeOptions
} = useExpenseDict()

const filterForm = ref({
  type: '',
  status: '',
  invoiceType: ''
})

const emit = defineEmits(['filter'])

function handleFilter() {
  emit('filter', filterForm.value)
}

function handleReset() {
  filterForm.value = {
    type: '',
    status: '',
    invoiceType: ''
  }
  emit('filter', filterForm.value)
}
</script>
```

### 8.5 列表页中使用字典标签

```vue
<!-- src/modules/expense/components/ExpenseList.vue -->
<template>
  <el-table :data="expenseList">
    <el-table-column prop="id" label="报销单号" width="180" />

    <el-table-column prop="type" label="报销类型" width="120">
      <template #default="{ row }">
        <el-tag>{{ getTypeLabel(row.type) }}</el-tag>
      </template>
    </el-table-column>

    <el-table-column prop="amount" label="金额" width="120">
      <template #default="{ row }">
        ¥{{ row.amount.toFixed(2) }}
      </template>
    </el-table-column>

    <el-table-column prop="status" label="状态" width="120">
      <template #default="{ row }">
        <el-tag :type="getStatusTagType(row.status)">
          {{ getStatusLabel(row.status) }}
        </el-tag>
      </template>
    </el-table-column>

    <el-table-column prop="applyDate" label="申请日期" width="120" />

    <el-table-column label="操作" fixed="right" width="200">
      <template #default="{ row }">
        <el-button
          v-if="hasPermission('expense:edit_own') && row.status === 'draft'"
          size="small"
          @click="handleEdit(row)"
        >
          编辑
        </el-button>
        <el-button
          v-if="hasPermission('expense:dept_approve') && row.status === 'dept_pending'"
          type="primary"
          size="small"
          @click="handleDeptApprove(row)"
        >
          审批
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useExpenseDict } from '../composables/useExpenseDict'
import { useExpensePermission } from '../composables/useExpensePermission'

const {
  getStatusLabel,
  getTypeLabel
} = useExpenseDict()

const { hasPermission } = useExpensePermission()

const expenseList = ref([])

function getStatusTagType(status: string) {
  const typeMap: Record<string, string> = {
    draft: 'info',
    dept_pending: 'warning',
    finance_pending: 'warning',
    rejected: 'danger',
    paid: 'success'
  }
  return typeMap[status] || 'info'
}

onMounted(async () => {
  // 加载数据
})
</script>
```

---

## 9. 权限管理集成实现

### 9.1 权限Store扩展

```typescript
// src/modules/expense/composables/useExpensePermission.ts

import { computed } from 'vue'
import { useAuthStore } from '@/modules/auth/store'

/**
 * 费用报销权限组合函数
 */
export function useExpensePermission() {
  const authStore = useAuthStore()

  /**
   * 检查权限
   */
  function hasPermission(permission: string): boolean {
    return authStore.hasPermission(permission)
  }

  /**
   * 检查是否可以编辑报销单
   */
  function canEditExpense(expense: any): boolean {
    // 草稿状态且是自己的报销单
    return (
      expense.status === 'draft' &&
      expense.applicantId === authStore.currentUser?.id &&
      hasPermission('expense:edit_own')
    )
  }

  /**
   * 检查是否可以查看报销单
   */
  function canViewExpense(expense: any): boolean {
    // 自己的报销单
    if (expense.applicantId === authStore.currentUser?.id) {
      return hasPermission('expense:view_own')
    }

    // 本部门的报销单
    if (expense.departmentId === authStore.currentUser?.departmentId) {
      return hasPermission('expense:view_department')
    }

    // 所有报销单
    return hasPermission('expense:view_all')
  }

  /**
   * 过滤报销单列表(数据权限)
   */
  function filterExpenseList(expenses: any[]): any[] {
    if (hasPermission('expense:view_all')) {
      return expenses // 返回所有报销单
    }

    if (hasPermission('expense:view_department')) {
      // 只返回本部门的报销单
      return expenses.filter(
        e => e.departmentId === authStore.currentUser?.departmentId
      )
    }

    // 只返回自己的报销单
    return expenses.filter(
      e => e.applicantId === authStore.currentUser?.id
    )
  }

  /**
   * 检查是否可以审批报销单
   */
  function canApproveExpense(expense: any): boolean {
    if (expense.status === 'dept_pending') {
      return hasPermission('expense:dept_approve')
    }

    if (expense.status === 'finance_pending') {
      return hasPermission('expense:finance_approve')
    }

    return false
  }

  /**
   * 检查是否可以执行打款
   */
  function canPayment(expense: any): boolean {
    return (
      expense.status === 'pending' &&
      hasPermission('expense:payment')
    )
  }

  return {
    hasPermission,
    canEditExpense,
    canViewExpense,
    filterExpenseList,
    canApproveExpense,
    canPayment
  }
}
```

### 9.2 列表页权限控制

```vue
<!-- src/modules/expense/views/ExpenseListView.vue -->
<template>
  <div class="expense-list">
    <!-- 工具栏 -->
    <el-toolbar>
      <el-button
        v-if="hasPermission('expense:create')"
        type="primary"
        @click="handleCreate"
      >
        新增报销单
      </el-button>

      <el-button
        v-if="hasPermission('expense:export')"
        @click="handleExport"
      >
        导出数据
      </el-button>
    </el-toolbar>

    <!-- 筛选面板 -->
    <ExpenseFilter @filter="handleFilter" />

    <!-- 数据表格 -->
    <el-table :data="displayExpenses">
      <el-table-column prop="id" label="报销单号" width="180" />

      <el-table-column prop="type" label="报销类型" width="120">
        <template #default="{ row }">
          <el-tag>{{ getTypeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="amount" label="金额" width="120">
        <template #default="{ row }">
          ¥{{ row.amount.toFixed(2) }}
        </template>
      </el-table-column>

      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" fixed="right" width="250">
        <template #default="{ row }">
          <el-button
            v-if="canEditExpense(row)"
            size="small"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>

          <el-button
            v-if="canApproveExpense(row)"
            type="primary"
            size="small"
            @click="handleApprove(row)"
          >
            审批
          </el-button>

          <el-button
            v-if="canPayment(row)"
            type="warning"
            size="small"
            @click="handlePayment(row)"
          >
            打款
          </el-button>

          <el-button
            v-if="hasPermission('expense:delete_own') && row.status === 'draft'"
            type="danger"
            size="small"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useExpenseDict } from '../composables/useExpenseDict'
import { useExpensePermission } from '../composables/useExpensePermission'
import ExpenseFilter from '../components/ExpenseFilter.vue'

const {
  getStatusLabel,
  getTypeLabel
} = useExpenseDict()

const {
  hasPermission,
  canEditExpense,
  canApproveExpense,
  canPayment,
  filterExpenseList
} = useExpensePermission()

// 原始数据
const allExpenses = ref<any[]>([])

// 应用数据权限过滤
const displayExpenses = computed(() => {
  return filterExpenseList(allExpenses.value)
})

function getStatusTagType(status: string) {
  const typeMap: Record<string, string> = {
    draft: 'info',
    dept_pending: 'warning',
    finance_pending: 'warning',
    rejected: 'danger',
    paid: 'success'
  }
  return typeMap[status] || 'info'
}

function handleCreate() {
  // 新增报销单
}

function handleEdit(row: any) {
  // 编辑报销单
}

function handleApprove(row: any) {
  // 审批报销单
}

function handlePayment(row: any) {
  // 打款
}

function handleDelete(row: any) {
  // 删除报销单
}

function handleExport() {
  // 导出数据
}

function handleFilter(filters: any) {
  // 应用筛选
}

onMounted(async () => {
  // 加载报销单数据
  allExpenses.value = []
})
</script>
```

### 9.3 详情页权限控制

```vue
<!-- src/modules/expense/views/ExpenseDetailView.vue -->
<template>
  <el-descriptions :column="2" border>
    <el-descriptions-item label="报销单号">
      {{ expense.id }}
    </el-descriptions-item>

    <el-descriptions-item label="报销类型">
      {{ getTypeLabel(expense.type) }}
    </el-descriptions-item>

    <el-descriptions-item label="报销金额">
      ¥{{ expense.amount.toFixed(2) }}
    </el-descriptions-item>

    <el-descriptions-item label="报销状态">
      <el-tag :type="getStatusTagType(expense.status)">
        {{ getStatusLabel(expense.status) }}
      </el-tag>
    </el-descriptions-item>

    <el-descriptions-item label="报销事由" :span="2">
      {{ expense.reason }}
    </el-descriptions-item>

    <!-- 银行账户信息 - 仅财务和本人可见 -->
    <el-descriptions-item
      v-if="showBankAccount"
      label="银行账户"
      :span="2"
    >
      {{ expense.bankAccount }}
    </el-descriptions-item>

    <!-- 打款凭证 - 仅财务可见 -->
    <el-descriptions-item
      v-if="hasPermission('expense:view_all') && expense.paymentProof"
      label="打款凭证"
      :span="2"
    >
      <el-image
        :src="expense.paymentProof"
        :preview-src-list="[expense.paymentProof]"
        style="width: 200px"
      />
    </el-descriptions-item>

    <!-- 审批意见 -->
    <el-descriptions-item v-if="expense.deptApproval" label="部门审批意见" :span="2">
      {{ expense.deptApproval.opinion || '无' }}
    </el-descriptions-item>

    <el-descriptions-item v-if="expense.financeApproval" label="财务审批意见" :span="2">
      {{ expense.financeApproval.opinion || '无' }}
    </el-descriptions-item>
  </el-descriptions>

  <!-- 操作按钮 -->
  <div class="actions">
    <el-button
      v-if="canEditExpense(expense)"
      type="primary"
      @click="handleEdit"
    >
      编辑
    </el-button>

    <el-button
      v-if="canApproveExpense(expense)"
      type="success"
      @click="handleApprove"
    >
      审批
    </el-button>

    <el-button
      v-if="canPayment(expense)"
      type="warning"
      @click="handlePayment"
    >
      执行打款
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useExpenseDict } from '../composables/useExpenseDict'
import { useExpensePermission } from '../composables/useExpensePermission'

const props = defineProps<{
  expense: any
}>()

const {
  getStatusLabel,
  getTypeLabel
} = useExpenseDict()

const {
  hasPermission,
  canEditExpense,
  canApproveExpense,
  canPayment
} = useExpensePermission()

// 银行账户显示控制
const showBankAccount = computed(() => {
  return hasPermission('expense:view_all') ||
         (hasPermission('expense:view_own') &&
          props.expense.applicantId === authStore.currentUser?.id)
})

function getStatusTagType(status: string) {
  const typeMap: Record<string, string> = {
    draft: 'info',
    dept_pending: 'warning',
    finance_pending: 'warning',
    rejected: 'danger',
    paid: 'success'
  }
  return typeMap[status] || 'info'
}

function handleEdit() {
  // 编辑
}

function handleApprove() {
  // 审批
}

function handlePayment() {
  // 打款
}
</script>
```

### 9.4 表单权限控制

```vue
<!-- src/modules/expense/components/ExpenseForm.vue -->
<template>
  <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
    <!-- 基本信息 -->
    <el-form-item label="报销类型" prop="type">
      <el-select v-model="form.type" placeholder="请选择报销类型">
        <el-option
          v-for="item in expenseTypeOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>

    <el-form-item label="报销事由" prop="reason">
      <el-input
        v-model="form.reason"
        type="textarea"
        :rows="3"
        placeholder="请输入报销事由"
        :disabled="formDisabled"
      />
    </el-form-item>

    <!-- 费用明细 -->
    <el-form-item label="费用明细">
      <div v-for="(item, index) in form.items" :key="index" class="expense-item">
        <el-input v-model="item.description" placeholder="费用说明" :disabled="formDisabled" />
        <el-input-number v-model="item.amount" :precision="2" :disabled="formDisabled" />
        <el-date-picker v-model="item.date" :disabled="formDisabled" />
      </div>
    </el-form-item>

    <!-- 发票信息 - 仅财务可编辑 -->
    <el-form-item label="发票信息">
      <div v-for="(invoice, index) in form.invoices" :key="index" class="invoice-item">
        <el-select v-model="invoice.type" :disabled="!canEditInvoice">
          <el-option
            v-for="type in invoiceTypeOptions"
            :key="type.value"
            :label="type.label"
            :value="type.value"
          />
        </el-select>
        <el-input v-model="invoice.number" placeholder="发票号码" :disabled="!canEditInvoice" />
        <el-input-number v-model="invoice.amount" :precision="2" :disabled="!canEditInvoice" />
      </div>
    </el-form-item>

    <!-- 打款凭证 - 仅财务可上传 -->
    <el-form-item v-if="hasPermission('expense:payment')" label="打款凭证">
      <el-upload
        :action="uploadUrl"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
      >
        <el-button type="primary">上传打款凭证</el-button>
      </el-upload>
    </el-form-item>

    <el-form-item>
      <el-button
        v-if="!formDisabled"
        type="primary"
        @click="handleSubmit"
      >
        提交
      </el-button>
      <el-button @click="handleCancel">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useExpenseDict } from '../composables/useExpenseDict'
import { useExpensePermission } from '../composables/useExpensePermission'

const props = defineProps<{
  expense?: any
}>()

const emit = defineEmits(['submit', 'cancel'])

const {
  expenseTypeOptions,
  invoiceTypeOptions
} = useExpenseDict()

const { hasPermission } = useExpensePermission()

const form = ref({
  type: '',
  reason: '',
  items: [],
  invoices: []
})

const formRef = ref()

// 表单是否禁用(草稿可编辑,其他状态不可编辑)
const formDisabled = computed(() => {
  return props.expense && props.expense.status !== 'draft'
})

// 是否可以编辑发票信息
const canEditInvoice = computed(() => {
  return !formDisabled.value || hasPermission('expense:manage_invoice')
})

function handleSubmit() {
  formRef.value?.validate((valid: boolean) => {
    if (valid) {
      emit('submit', form.value)
    }
  })
}

function handleCancel() {
  emit('cancel')
}

function handleUploadSuccess(response: any) {
  console.log('上传成功', response)
}
</script>
```

### 9.5 API请求权限拦截

```typescript
// src/utils/request.ts

import axios from 'axios'
import { useAuthStore } from '@/modules/auth/store'

// 创建axios实例
const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
http.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()

    // 添加Token
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }

    // 记录审计日志
    if (config.method === 'post' || config.method === 'put' || config.method === 'delete') {
      console.log(`[API Audit] ${config.method?.toUpperCase()} ${config.url}`, {
        user: authStore.currentUser?.id,
        timestamp: new Date().toISOString()
      })
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
http.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response?.status === 403) {
      console.error('[Permission Error] 无权限访问:', error.config.url)
    }
    return Promise.reject(error)
  }
)

export { http }
```

---

## 10. Mock数据实现

费用报销模块提供了完整的Mock数据实现,支持前端独立开发和测试。

### 10.1 Mock数据结构

**报销单数据** (`mockExpenses`):
```typescript
export const mockExpenses: Expense[] = [
  {
    id: generateExpenseId(),
    applicantId: 'U001',
    applicantName: '张三',
    departmentId: 'D001',
    departmentName: '销售部',
    type: 'travel',
    amount: 3280.00,
    reason: '上海客户拜访差旅费用',
    applyDate: '2025-12-15T09:30:00.000Z',
    expenseDate: '2025-12-10T00:00:00.000Z',
    status: 'paid',
    departmentApproval: {
      approverId: 'U010',
      approverName: '李经理',
      status: 'approved',
      opinion: '差旅费用合理,同意报销',
      timestamp: '2025-12-15T14:20:00.000Z'
    },
    financeApproval: {
      approverId: 'U020',
      approverName: '王财务',
      status: 'approved',
      opinion: '发票齐全,金额核对无误',
      timestamp: '2025-12-16T10:15:00.000Z'
    },
    paymentDate: '2025-12-17T14:30:00.000Z',
    paymentProof: '/uploads/payments/EXP202512150001_proof.pdf',
    items: [...],
    invoices: [...]
  },
  // ... 更多报销单数据
]
```

**打款记录数据** (`mockPayments`):
```typescript
export const mockPayments: PaymentRecord[] = [
  {
    id: 1,
    expenseId: mockExpenses[0].id,
    amount: 3280.00,
    paymentMethod: 'bank_transfer',
    paymentDate: '2025-12-17T14:30:00.000Z',
    bankAccount: '6222021234567890123',
    proof: '/uploads/payments/EXP202512150001_proof.pdf',
    status: 'completed',
    remark: '工商银行转账,流水号: TX202512171430001'
  },
  // ... 更多打款记录
]
```

### 10.2 Mock API实现

**创建报销单**:
```typescript
export async function createExpense(data: ExpenseForm): Promise<Expense> {
  await mockDelay()

  // 计算总金额
  const totalAmount = data.items.reduce((sum, item) => sum + item.amount, 0)

  // 生成报销单号
  const expenseId = generateExpenseId()

  const now = new Date().toISOString()

  const newExpense: Expense = {
    id: expenseId,
    applicantId: 'U001',
    applicantName: '张三',
    departmentId: 'D001',
    departmentName: '销售部',
    type: data.type,
    amount: totalAmount,
    items: data.items,
    invoices: data.invoices,
    reason: data.reason,
    applyDate: now,
    expenseDate: data.expenseDate,
    status: 'draft',
    createdAt: now,
    updatedAt: now
  }

  mockExpenses.unshift(newExpense)
  return newExpense
}
```

**提交审批**:
```typescript
export async function submitExpense(id: string): Promise<Expense> {
  await mockDelay()

  const expense = mockExpenses.find(e => e.id === id)
  if (!expense) {
    throw new Error(`报销单 ${id} 不存在`)
  }

  if (expense.status !== 'draft') {
    throw new Error('只能提交草稿状态的报销单')
  }

  // 验证发票
  if (!expense.invoices || expense.invoices.length === 0) {
    throw new Error('请至少上传一张发票')
  }

  // 验证金额一致性
  const invoiceTotal = expense.invoices.reduce((sum, inv) => sum + inv.amount, 0)
  if (Math.abs(invoiceTotal - expense.amount) > 0.01) {
    throw new Error(`发票总金额(${invoiceTotal})与报销金额(${expense.amount})不一致`)
  }

  // 更新状态为部门审批中
  expense.status = 'dept_pending'
  expense.departmentApproval = {
    status: 'pending'
  }
  expense.updatedAt = new Date().toISOString()

  return expense
}
```

**部门审批**:
```typescript
export async function departmentApprove(
  id: string,
  approval: ApprovalForm
): Promise<Expense> {
  await mockDelay()

  const expense = mockExpenses.find(e => e.id === id)
  if (!expense) {
    throw new Error(`报销单 ${id} 不存在`)
  }

  if (expense.status !== 'dept_pending') {
    throw new Error('当前状态不允许部门审批')
  }

  const now = new Date().toISOString()

  if (approval.status === 'approved') {
    // 审批通过,进入财务审批
    expense.status = 'finance_pending'
    expense.departmentApproval = {
      approverId: 'U010',
      approverName: '李经理',
      status: 'approved',
      opinion: approval.opinion,
      timestamp: now
    }
    expense.financeApproval = {
      status: 'pending'
    }
  } else {
    // 驳回
    expense.status = 'rejected'
    expense.departmentApproval = {
      approverId: 'U010',
      approverName: '李经理',
      status: 'rejected',
      opinion: approval.opinion,
      timestamp: now
    }
  }

  expense.updatedAt = now
  return expense
}
```

**财务审批**:
```typescript
export async function financeApprove(
  id: string,
  approval: ApprovalForm
): Promise<Expense> {
  await mockDelay()

  const expense = mockExpenses.find(e => e.id === id)
  if (!expense) {
    throw new Error(`报销单 ${id} 不存在`)
  }

  if (expense.status !== 'finance_pending') {
    throw new Error('当前状态不允许财务审批')
  }

  const now = new Date().toISOString()

  if (approval.status === 'approved') {
    // 审批通过,自动创建打款记录
    expense.status = 'paid'
    expense.financeApproval = {
      approverId: 'U020',
      approverName: '王财务',
      status: 'approved',
      opinion: approval.opinion,
      timestamp: now
    }

    // 创建打款记录
    const payment: PaymentRecord = {
      id: mockPayments.length + 1,
      expenseId: expense.id,
      amount: expense.amount,
      paymentMethod: 'bank_transfer',
      paymentDate: now,
      bankAccount: '6222021234567890123',
      status: 'pending',
      remark: '财务审批通过,等待打款'
    }
    mockPayments.push(payment)
  } else {
    // 驳回
    expense.status = 'rejected'
    expense.financeApproval = {
      approverId: 'U020',
      approverName: '王财务',
      status: 'rejected',
      opinion: approval.opinion,
      timestamp: now
    }
  }

  expense.updatedAt = now
  return expense
}
```

**发票验证**:
```typescript
export async function validateInvoice(invoiceNumber: string): Promise<{
  valid: boolean
  message: string
}> {
  await mockDelay()

  // 检查发票号码格式
  const regex = /^\d{8}$|^\d{20}$/
  if (!regex.test(invoiceNumber)) {
    return {
      valid: false,
      message: '发票号码格式不正确,应为8位或20位数字'
    }
  }

  // 检查是否已被使用
  for (const expense of mockExpenses) {
    for (const invoice of expense.invoices) {
      if (invoice.number === invoiceNumber) {
        return {
          valid: false,
          message: `发票号 ${invoiceNumber} 已被使用,不能重复报销`
        }
      }
    }
  }

  return {
    valid: true,
    message: '发票号可用'
  }
}
```

**OCR识别发票(模拟)**:
```typescript
export async function ocrInvoice(imageUrl: string): Promise<Partial<Invoice>> {
  await delay(500) // OCR识别延迟500ms

  // 模拟OCR识别结果
  const mockInvoice: Partial<Invoice> = {
    type: 'vat_common',
    number: Math.floor(Math.random() * 90000000 + 10000000).toString(),
    amount: Math.floor(Math.random() * 5000 * 100) / 100,
    date: new Date().toISOString().split('T')[0],
    imageUrl: imageUrl,
    verified: false
  }

  return mockInvoice
}
```

### 10.3 统计API实现

**按部门统计**:
```typescript
export async function getDepartmentStats(params: StatsQueryParams): Promise<DepartmentStats[]> {
  await mockDelay()

  // 筛选日期范围内的报销单
  let filtered = mockExpenses.filter(expense => {
    const applyDate = expense.applyDate.split('T')[0]
    return applyDate >= params.startDate && applyDate <= params.endDate
  })

  // 按部门分组统计
  const deptMap = new Map<string, DepartmentStats>()

  filtered.forEach(expense => {
    const existing = deptMap.get(expense.departmentId)
    if (existing) {
      existing.totalAmount += expense.amount
      existing.count += 1
    } else {
      deptMap.set(expense.departmentId, {
        departmentId: expense.departmentId,
        departmentName: expense.departmentName,
        totalAmount: expense.amount,
        count: 1,
        avgAmount: expense.amount
      })
    }
  })

  // 计算平均金额
  const result = Array.from(deptMap.values())
  result.forEach(stat => {
    stat.avgAmount = stat.totalAmount / stat.count
  })

  // 按总金额降序排序
  result.sort((a, b) => b.totalAmount - a.totalAmount)

  return result
}
```

**按类型统计**:
```typescript
export async function getTypeStats(params: StatsQueryParams): Promise<TypeStats[]> {
  await mockDelay()

  // 筛选日期范围内的报销单
  let filtered = mockExpenses.filter(expense => {
    const applyDate = expense.applyDate.split('T')[0]
    return applyDate >= params.startDate && applyDate <= params.endDate
  })

  // 类型筛选
  if (params.type) {
    filtered = filtered.filter(expense => expense.type === params.type)
  }

  // 按类型分组统计
  const typeMap = new Map<string, TypeStats>()

  filtered.forEach(expense => {
    const existing = typeMap.get(expense.type)
    if (existing) {
      existing.totalAmount += expense.amount
      existing.count += 1
    } else {
      typeMap.set(expense.type, {
        type: expense.type,
        totalAmount: expense.amount,
        count: 1,
        percentage: 0
      })
    }
  })

  // 计算总金额
  const totalAmount = Array.from(typeMap.values()).reduce((sum, stat) => sum + stat.totalAmount, 0)

  // 计算百分比
  const result = Array.from(typeMap.values())
  result.forEach(stat => {
    stat.percentage = totalAmount > 0 ? (stat.totalAmount / totalAmount) * 100 : 0
  })

  // 按总金额降序排序
  result.sort((a, b) => b.totalAmount - a.totalAmount)

  return result
}
```

### 10.4 工具函数实现

**报销单号生成**:
```typescript
export function generateExpenseId(): string {
  const now = new Date()
  const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
  const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
  return `EXP${dateStr}${random}`
}
```

**状态判断函数**:
```typescript
export function canEdit(status: ExpenseStatus): boolean {
  return status === 'draft'
}

export function canDelete(status: ExpenseStatus): boolean {
  return status === 'draft'
}

export function canSubmit(status: ExpenseStatus): boolean {
  return status === 'draft' || status === 'rejected'
}

export function canCancel(status: ExpenseStatus): boolean {
  return status === 'dept_pending' || status === 'finance_pending'
}
```

**大额加签规则**:
```typescript
export function checkSingleAmountApproval(amount: number): number {
  if (amount > 10000) return 3 // 总经理+特别审批人
  if (amount > 5000) return 2 // 总经理
  return 1 // 默认审批层级
}
```

**格式化函数**:
```typescript
export function formatDate(date: string): string {
  if (!date) return ''
  return date.split('T')[0]
}

export function formatAmount(amount: number): string {
  return `¥${amount.toFixed(2)}`
}
```

---

**文档版本**: v1.0.0
**最后更新**: 2026-01-11
