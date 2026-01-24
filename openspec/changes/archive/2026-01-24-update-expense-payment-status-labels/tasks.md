# 实施任务

## 1. 后端修改
- [x] 1.1 修改 `ExpenseStatus.java` 枚举类
  - 将 `PAID("paid", "已打款")` 改为 `PAID("paid", "待打款")`
- [x] 1.2 验证后端编译通过
- [x] 1.3 添加 ExpensePayment 导入到 Controller

## 2. 前端修改
- [x] 2.1 修改 `utils/index.ts` 中的状态名称映射
  - 更新 `getExpenseStatusName()` 函数：`paid: '已打款'` → `paid: '待打款'`
- [x] 2.2 更新状态提示信息
  - 修改 `getStatusTip()` 函数中的 `paid` 提示文案
  - 从 "报销已完成并打款" 改为 "审批已完成，等待财务打款"
- [x] 2.3 修复类型导入问题
  - 修改导入路径为 `'../types/index'`
  - 添加 `InvoiceType.other` 映射
  - 修复 `formatDate` 类型定义
- [x] 2.4 更新 `types.ts` 中的状态选项
  - 更新 `EXPENSE_STATUS_OPTIONS` 中的 `paid` 标签为"待打款"

## 3. 验证
- [x] 3.1 后端编译验证（mvn compile）
- [x] 3.2 后端打包验证（mvn package）
- [x] 3.3 前端构建验证（vite build）
- [ ] 3.4 手动测试
  - [ ] 3.4.1 验证"我的报销"页面状态显示正确
  - [ ] 3.4.2 验证"打款管理"页面状态显示一致
  - [ ] 3.4.3 确认状态提示信息准确

## 4. 部署
- [ ] 4.1 后端部署
  - WAR包已生成: `target/oa-system-backend-0.0.1-SNAPSHOT.war`
  - 参考部署指南: `DEPLOYMENT.md`
- [ ] 4.2 前端部署
  - 构建目录: `oa-system-frontend/dist/`
  - 参考部署指南: `DEPLOYMENT.md`
- [ ] 4.3 生产环境验证
  - 验证清单见 `DEPLOYMENT.md`

## 5. 文档
- [x] 5.1 创建部署指南 `DEPLOYMENT.md`
- [ ] 5.2 更新 API 文档（如需要）
- [ ] 5.3 归档变更到 OpenSpec archive
  - 命令: `openspec archive update-expense-payment-status-labels`
