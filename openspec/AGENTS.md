# OpenSpec 指南

供使用 OpenSpec 进行规范驱动开发的 AI 编程助手的说明。

## 快速检查清单 (TL;DR)

- 搜索现有工作: `openspec spec list --long`, `openspec list` (全文搜索仅使用 `rg`)
- 确定范围: 新增功能 vs 修改现有功能
- 选择唯一的 `change-id`: kebab-case，动词开头 (`add-`, `update-`, `remove-`, `refactor-`)
- 搭建框架: `proposal.md`, `tasks.md`, `design.md` (仅必要时)，以及每个受影响功能的增量规范
- 编写增量: 使用 `## ADDED|MODIFIED|REMOVED|RENAMED Requirements`；每个需求至少包含一个 `#### Scenario:`
- 验证: `openspec validate [change-id] --strict --no-interactive` 并修复问题
- 请求批准: 在批准之前不要开始实施

## 三阶段工作流

### 阶段 1: 创建变更
在以下情况下创建提案：
- 添加功能或能力
- 进行破坏性变更 (API、架构)
- 更改架构或模式
- 优化性能 (改变行为)
- 更新安全模式

触发示例：
- "Help me create a change proposal" - "帮我创建变更提案"
- "Help me plan a change" - "帮我规划变更"
- "Help me create a proposal" - "帮我创建提案"
- "I want to create a spec proposal" - "我想创建规范提案"
- "I want to create a spec" - "我想创建规范"

模糊匹配指引：
- 包含以下之一: `proposal`, `change`, `spec`
- 同时包含以下之一: `create`, `plan`, `make`, `start`, `help`

以下情况跳过提案：
- Bug 修复 (恢复预期行为)
- 拼写错误、格式化、注释
- 依赖项更新 (非破坏性)
- 配置更改
- 针对现有行为的测试

**工作流程**
1. 查看 `openspec/project.md`、`openspec list` 和 `openspec list --specs` 了解当前上下文
2. 选择唯一的动词开头的 `change-id`，在 `openspec/changes/<id>/` 下搭建 `proposal.md`、`tasks.md`、可选的 `design.md` 和规范增量
3. 使用 `## ADDED|MODIFIED|REMOVED Requirements` 起草规范增量，每个需求至少包含一个 `#### Scenario:`
4. 运行 `openspec validate <id> --strict --no-interactive` 并在共享提案前解决所有问题

### 阶段 2: 实施变更
将这些步骤跟踪为 TODO 并逐一完成。
1. **阅读 proposal.md** - 了解要构建的内容
2. **阅读 design.md** (如果存在) - 审查技术决策
3. **阅读 tasks.md** - 获取实施检查清单
4. **按顺序实施任务** - 按顺序完成
5. **确认完成** - 确保在更新状态前完成 `tasks.md` 中的每一项
6. **更新检查清单** - 所有工作完成后，将每个任务设置为 `- [x]` 以反映实际情况
7. **批准门槛** - 在提案被审查和批准之前不要开始实施

### 阶段 3: 归档变更
部署后，创建单独的 PR 来：
- 移动 `changes/[name]/` → `changes/archive/YYYY-MM-DD-[name]/`
- 如果功能发生更改，更新 `specs/`
- 对于仅工具的更改使用 `openspec archive <change-id> --skip-specs --yes` (始终明确传递变更 ID)
- 运行 `openspec validate --strict --no-interactive` 确认归档的变更通过检查

## 任何任务之前

**上下文检查清单:**
- [ ] 阅读 `specs/[capability]/spec.md` 中的相关规范
- [ ] 检查 `changes/` 中的待处理变更是否存在冲突
- [ ] 阅读 `openspec/project.md` 了解约定
- [ ] 运行 `openspec list` 查看活动变更
- [ ] 运行 `openspec list --specs` 查看现有功能

**创建规范之前:**
- 始终检查功能是否已存在
- 优先修改现有规范而非创建重复规范
- 使用 `openspec show [spec]` 查看当前状态
- 如果请求不明确，在搭建框架前询问 1-2 个澄清问题

## 搜索指引
- 枚举规范: `openspec spec list --long` (或 `--json` 用于脚本)
- 枚举变更: `openspec list` (或 `openspec change list --json` - 已弃用但可用)
- 显示详细信息:
  - 规范: `openspec show <spec-id> --type spec` (使用 `--json` 进行过滤)
  - 变更: `openspec show <change-id> --json --deltas-only`
- 全文搜索 (使用 ripgrep): `rg -n "Requirement:|Scenario:" openspec/specs`

## 快速开始

### CLI 命令

```bash
# 基本命令
openspec list                  # 列出活动变更
openspec list --specs          # 列出规范
openspec show [item]           # 显示变更或规范
openspec validate [item]       # 验证变更或规范
openspec archive <change-id> [--yes|-y]   # 部署后归档 (添加 --yes 用于非交互运行)

# 项目管理
openspec init [path]           # 初始化 OpenSpec
openspec update [path]         # 更新说明文件

# 交互模式
openspec show                  # 提示进行选择
openspec validate              # 批量验证模式

# 调试
openspec show [change] --json --deltas-only
openspec validate [change] --strict --no-interactive
```

### 命令选项

- `--json` - 机器可读输出
- `--type change|spec` - 消除项目歧义
- `--strict` - 全面验证
- `--no-interactive` - 禁用提示
- `--skip-specs` - 归档时不更新规范
- `--yes`/`-y` - 跳过确认提示 (非交互式归档)

## 目录结构

```
openspec/
├── project.md              # 项目约定
├── specs/                  # 当前事实 - 已构建的内容
│   └── [capability]/       # 单一专注的功能
│       ├── spec.md         # 需求和场景
│       └── design.md       # 技术模式
├── changes/                # 提案 - 应该更改的内容
│   ├── [change-name]/
│   │   ├── proposal.md     # 为什么、什么、影响
│   │   ├── tasks.md        # 实施检查清单
│   │   ├── design.md       # 技术决策 (可选；见标准)
│   │   └── specs/          # 增量更改
│   │       └── [capability]/
│   │           └── spec.md # ADDED/MODIFIED/REMOVED
│   └── archive/            # 已完成的变更
```

## 创建变更提案

### 决策树

```
新请求？
├─ 恢复规范行为的 bug 修复？ → 直接修复
├─ 拼写错误/格式/注释？ → 直接修复
├─ 新功能/能力？ → 创建提案
├─ 破坏性变更？ → 创建提案
├─ 架构变更？ → 创建提案
└─ 不确定？ → 创建提案 (更安全)
```

### 提案结构

1. **创建目录:** `changes/[change-id]/` (kebab-case，动词开头，唯一)

2. **编写 proposal.md:**
```markdown
# Change: [变更简要描述]

## Why
[1-2 句话说明问题/机会]

## What Changes
- [变更列表]
- [用 **BREAKING** 标记破坏性变更]

## Impact
- 受影响的规范: [功能列表]
- 受影响的代码: [关键文件/系统]
```

3. **创建规范增量:** `specs/[capability]/spec.md`
```markdown
## ADDED Requirements
### Requirement: 新功能
系统应该提供...

#### Scenario: 成功案例
- **WHEN** 用户执行操作
- **THEN** 预期结果

## MODIFIED Requirements
### Requirement: 现有功能
[完整的修改后的需求]

## REMOVED Requirements
### Requirement: 旧功能
**原因**: [删除原因]
**迁移**: [处理方式]
```
如果多个功能受影响，在 `changes/[change-id]/specs/<capability>/spec.md` 下创建多个增量文件——每个功能一个。

4. **创建 tasks.md:**
```markdown
## 1. 实施
- [ ] 1.1 创建数据库架构
- [ ] 1.2 实施 API 端点
- [ ] 1.3 添加前端组件
- [ ] 1.4 编写测试
```

5. **需要时创建 design.md:**
如果适用以下任何情况，创建 `design.md`；否则省略：
- 跨领域变更 (多个服务/模块) 或新的架构模式
- 新的外部依赖或重要的数据模型变更
- 安全性、性能或迁移复杂性
- 从编码前的技术决策中受益的歧义

最小化 `design.md` 骨架：
```markdown
## Context
[背景、约束、利益相关者]

## Goals / Non-Goals
- Goals: [...]
- Non-Goals: [...]

## Decisions
- Decision: [什么和为什么]
- 替代方案考虑: [选项 + 理由]

## Risks / Trade-offs
- [风险] → 缓解措施

## Migration Plan
[步骤、回滚]

## Open Questions
- [...]
```

## 规范文件格式

### 关键: 场景格式

**正确** (使用 #### 标题):
```markdown
#### Scenario: 用户登录成功
- **WHEN** 提供有效凭据
- **THEN** 返回 JWT 令牌
```

**错误** (不要使用项目符号或粗体):
```markdown
- **Scenario: 用户登录**  ❌
**Scenario**: 用户登录     ❌
### Scenario: 用户登录      ❌
```

每个需求必须至少有一个场景。

### 需求措辞
- 使用 SHALL/MUST 表示规范性要求 (避免 should/may，除非有意非规范性)

### 增量操作

- `## ADDED Requirements` - 新能力
- `## MODIFIED Requirements` - 更改行为
- `## REMOVED Requirements` - 弃用功能
- `## RENAMED Requirements` - 名称更改

使用 `trim(header)` 匹配标题 - 忽略空格。

#### 何时使用 ADDED vs MODIFIED
- ADDED: 引入新的能力或子能力，可以独立作为需求存在。当更改是正交的 (例如，添加"Slash Command Configuration") 而不是更改现有需求的语义时，优先使用 ADDED。
- MODIFIED: 更改现有需求的行为、范围或验收标准。始终粘贴完整的更新需求内容 (标题 + 所有场景)。归档器将用您在此处提供的内容替换整个需求；部分增量将丢失先前的细节。
- RENAMED: 仅在名称更改时使用。如果您也更改行为，请使用 RENAMED (名称) + MODIFIED (内容) 引用新名称。

常见陷阱: 使用 MODIFIED 添加新关注点而不包含先前的文本。这会在归档时导致细节丢失。如果您没有明确更改现有需求，请在 ADDED 下添加新需求。

正确编写 MODIFIED 需求：
1) 在 `openspec/specs/<capability>/spec.md` 中定位现有需求
2) 复制整个需求块 (从 `### Requirement: ...` 到其场景)
3) 将其粘贴到 `## MODIFIED Requirements` 下并编辑以反映新行为
4) 确保标题文本完全匹配 (空格不敏感) 并保留至少一个 `#### Scenario:`

RENAMED 示例：
```markdown
## RENAMED Requirements
- FROM: `### Requirement: Login`
- TO: `### Requirement: User Authentication`
```

## 故障排除

### 常见错误

**"Change must have at least one delta"**
- 检查 `changes/[name]/specs/` 是否存在 .md 文件
- 验证文件具有操作前缀 (## ADDED Requirements)

**"Requirement must have at least one scenario"**
- 检查场景使用 `#### Scenario:` 格式 (4 个井号)
- 不要使用项目符号或粗体作为场景标题

**静默场景解析失败**
- 需要精确格式: `#### Scenario: Name`
- 调试使用: `openspec show [change] --json --deltas-only`

### 验证提示

```bash
# 始终使用严格模式进行全面检查
openspec validate [change] --strict --no-interactive

# 调试增量解析
openspec show [change] --json | jq '.deltas'

# 检查特定需求
openspec show [spec] --json -r 1
```

## 快速路径脚本

```bash
# 1) 探索当前状态
openspec spec list --long
openspec list
# 可选全文搜索:
# rg -n "Requirement:|Scenario:" openspec/specs
# rg -n "^#|Requirement:" openspec/changes

# 2) 选择变更 id 并搭建框架
CHANGE=add-two-factor-auth
mkdir -p openspec/changes/$CHANGE/{specs/auth}
printf "## Why\n...\n\n## What Changes\n- ...\n\n## Impact\n- ...\n" > openspec/changes/$CHANGE/proposal.md
printf "## 1. Implementation\n- [ ] 1.1 ...\n" > openspec/changes/$CHANGE/tasks.md

# 3) 添加增量 (示例)
cat > openspec/changes/$CHANGE/specs/auth/spec.md << 'EOF'
## ADDED Requirements
### Requirement: Two-Factor Authentication
Users MUST provide a second factor during login.

#### Scenario: OTP required
- **WHEN** valid credentials are provided
- **THEN** an OTP challenge is required
EOF

# 4) 验证
openspec validate $CHANGE --strict --no-interactive
```

## 多功能示例

```
openspec/changes/add-2fa-notify/
├── proposal.md
├── tasks.md
└── specs/
    ├── auth/
    │   └── spec.md   # ADDED: Two-Factor Authentication
    └── notifications/
        └── spec.md   # ADDED: OTP email notification
```

auth/spec.md
```markdown
## ADDED Requirements
### Requirement: Two-Factor Authentication
...
```

notifications/spec.md
```markdown
## ADDED Requirements
### Requirement: OTP Email Notification
...
```

## 最佳实践

### 简单优先
- 默认少于 100 行新代码
- 单文件实现，直到证明不足
- 在没有明确理由的情况下避免使用框架
- 选择无聊、经过验证的模式

### 复杂性触发器
仅在以下情况下添加复杂性：
- 性能数据显示当前解决方案太慢
- 具体的规模要求 (>1000 用户，>100MB 数据)
- 多个经过验证的用例需要抽象

### 清晰引用
- 使用 `file.ts:42` 格式表示代码位置
- 引用规范为 `specs/auth/spec.md`
- 链接相关的变更和 PR

### 功能命名
- 使用动词-名词: `user-auth`, `payment-capture`
- 每个功能单一目的
- 10 分钟可理解性规则
- 如果描述需要 "AND"，则拆分

### 变更 ID 命名
- 使用 kebab-case，简短描述: `add-two-factor-auth`
- 优先使用动词开头的前缀: `add-`, `update-`, `remove-`, `refactor-`
- 确保唯一性；如果被占用，附加 `-2`、`-3` 等

## 工具选择指南

| 任务 | 工具 | 原因 |
|------|------|-----|
| 按模式查找文件 | Glob | 快速模式匹配 |
| 搜索代码内容 | Grep | 优化的正则搜索 |
| 读取特定文件 | Read | 直接文件访问 |
| 探索未知范围 | Task | 多步骤调查 |

## 错误恢复

### 变更冲突
1. 运行 `openspec list` 查看活动变更
2. 检查重叠的规范
3. 与变更所有者协调
4. 考虑合并提案

### 验证失败
1. 使用 `--strict` 标志运行
2. 检查 JSON 输出获取详细信息
3. 验证规范文件格式
4. 确保场景格式正确

### 缺少上下文
1. 首先阅读 project.md
2. 检查相关规范
3. 查看最近的归档
4. 请求澄清

## 快速参考

### 阶段指示器
- `changes/` - 已提议，尚未构建
- `specs/` - 已构建和部署
- `archive/` - 已完成的变更

### 文件用途
- `proposal.md` - 为什么和什么
- `tasks.md` - 实施步骤
- `design.md` - 技术决策
- `spec.md` - 需求和行为

### CLI 要点
```bash
openspec list              # 什么正在进行中？
openspec show [item]       # 查看详细信息
openspec validate --strict --no-interactive  # 是否正确？
openspec archive <change-id> [--yes|-y]  # 标记完成 (添加 --yes 用于自动化)
```

记住: 规范是事实。变更是提案。保持它们同步。
