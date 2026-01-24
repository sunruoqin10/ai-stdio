# 变更日志 - 打款凭证上传参数错误修复

## [1.0.0] - 2026-01-24

### Bug 修复
- 🐛 修复上传打款凭证时"报销单不存在"错误
- 🔧 修改参数传递：使用 `expenseId` 替代 `id`
- 📝 更新变量类型定义：`string | null` 替代 `number | null`

### 技术细节
- **修改文件**: `PaymentManagement.vue`
- **修改行数**: 2 处
- **变更类型**: 前端 Bug 修复
- **风险等级**: 🟢 低风险

### 验证状态
- ✅ 代码审查通过
- ✅ 构建成功
- ⏳ 功能测试待执行

---

## 问题详情

### 错误信息
```
BusinessException: 报销单不存在
Transaction synchronization closing SqlSession
```

### 根本原因
前端传递了支付记录ID (`row.id`)，但后端期望的是报销单号 (`row.expenseId`)

### 修复方案
在 `handleUploadProof` 函数中使用 `row.expenseId` 替代 `row.id`

---

**部署状态**: ⏳ 待测试和部署
**最后更新**: 2026-01-24
