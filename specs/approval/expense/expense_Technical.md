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

**文档版本**: v1.0.0
**最后更新**: 2026-01-09
