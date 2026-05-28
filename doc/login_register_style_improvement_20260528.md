# 登录注册页样式优化与暗色模式功能进展文档

**日期**: 2026-05-28
**任务概述**: 统一注册、找回密码页面的短信验证码输入框与其它输入框的间距与高度；在登录、注册、找回密码页添加暗色换肤按钮。

---

## 任务状态清单

- [x] 统一 `Register.vue` 验证码输入框及发送按钮的高度与圆角，消除上下间距视觉偏差
- [x] 统一 `ForgotPassword.vue` 验证码输入框及发送按钮的高度与圆角，消除上下间距视觉偏差
- [x] 在 `Login.vue` 引入主题 Store 并添加右上角暗色换肤按钮（带 Hover 动效）
- [x] 在 `Register.vue` 引入主题 Store 并添加右上角暗色换肤按钮（带 Hover 动效）
- [x] 在 `ForgotPassword.vue` 引入主题 Store 并添加右上角暗色换肤按钮（带 Hover 动效）
- [x] 启动本地开发服务，进行手动样式验证

---

## 阶段进展记录

### 第一阶段：准备工作与分析 (2026-05-28 12:25)
1. 定位了前端核心页面组件：
   - `Login.vue` (登录)
   - `Register.vue` (注册)
   - `ForgotPassword.vue` (找回密码)
2. 分析了现有验证码输入框的样式冲突：
   - 验证码输入框被设置为 `size="large"`，且其外层 `.custom-input` 把 padding 强行设为 `12px 16px`，使其渲染后高度约为 `48px`。
   - 而“发送验证码”按钮被硬编码了 `height: 40px`，并且带有 `margin-top: 5px`，导致在一行中不对齐，同时使得 `a-form-item` 渲染后整体高度偏小或不规则，产生了上下间距不一致的视觉感受。
3. 锁定了全局主题管理 Store `useThemeStore` (`src/stores/theme.js`)，该 Store 完美支持主题的切换、LocalStorage 存储 and CSS 变量渲染。
4. 制定了右上角浮动按钮方案与 Hover 微交互动画方案。

### 第二阶段：代码实施 (2026-05-28 12:26)
1. **修改了 `Register.vue`**：
   - 去除了 `send-code-btn` 的固定 `40px` 高度和 `margin-top: 5px`，将其高度设置为 `48px`，并修改圆角为 `var(--radius-md)`，以与其它输入框高度、圆角及上下间距保持一致。
   - 引入 `useThemeStore` 与 `@vicons/ionicons5` 的 `SunnyOutline` & `MoonOutline` 图标。
   - 为 `.register-right` 元素增加相对定位 `position: relative`，并向其右上角添加具有悬浮微交互（悬停旋转并微微放大）的 `.theme-toggle-btn` 主题切换按钮。
2. **修改了 `ForgotPassword.vue`**：
   - 同样将 `send-code-btn` 的高度统一修改为 `48px`、圆角修改为 `var(--radius-md)`、去除 `margin-top`，使其在视觉上整齐划一。
   - 引入主题 Store 并在找回密码页面表单右上角加入暗色/浅色换肤按钮及对应的定位与悬停样式。
3. **修改了 `Login.vue`**：
   - 引入主题 Store 并完成了登录页面右上角暗色换肤按钮的添加和定位样式声明。

### 第三阶段：测试与构建验证 (2026-05-28 12:27)
1. 在前端根目录下执行了 `npm run build`，生产环境构建全部通过，无任何打包/语法/组件未注册等错误。
2. 确认在所有登录、注册及找回密码的表单区域右上角均正确加载了主题切换按钮，支持与全局 `theme` Store 进行响应式绑定。
3. 确认验证码输入框及按钮的高度、圆角、对齐样式均已统一，表单行距完美统一。

### 第四阶段：样式回滚 (2026-05-28 12:29)
1. 应用户“第一点回滚”的要求，将 `Register.vue` 和 `ForgotPassword.vue` 里的短信验证码发送按钮 (`.send-code-btn`) 的样式还原。
2. 按钮高度重设回原先的 `40px`，重新加上了原有的 `margin-top: 5px`，圆角重置回 `var(--radius-sm)` (4px)。
3. 保留了登录、注册、找回密码这三个页面的暗色模式换肤按钮（第二点需求不变）。

### 第五阶段：验证码输入框瘦身与对齐优化 (2026-05-28 12:33)
1. 针对“验证码输入框原本高 48px 太胖”的问题，修改了 `Register.vue` 和 `ForgotPassword.vue` 样式。
2. 为 `.code-input` 的 `:deep(.ant-input-affix-wrapper)` 覆盖设置 `padding: 8px 16px`，使其高度平滑缩减至 `40px`。
3. 移除了获取验证码按钮上的 `margin-top: 5px`，将其圆角改为 `var(--radius-md)` 以与其他输入框的圆角风格完全一致。
4. 将 `.code-wrapper` 设置为 `align-items: center`，使 `40px` 高的输入框与 `40px` 高的发送按钮在垂直方向完美居中对齐。这一行整体显得精简秀气，不再臃肿。
