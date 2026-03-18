# Bug 修复进展文档

> 文档更新时间：2026-03-18  
> 编译验证：✅ `mvn compile` 通过（含遗留Bug修复后二次验证）

---

## 本次修复范围

本次针对 `doc/bug_report.md` 中报告的 10 个 Bug，完成了其中 **9 个**的修复（含新增前端交互优化）。

---

## ✅ 已修复（9 个）

### 🔴 Bug 1：取消团购订单时 `product.stock` 被双重恢复（严重）

- **文件**：`community-mall-backend/src/main/java/com/community/mall/service/OrderService.java`
- **方法**：`cancelOrder()`
- **修复内容**：将恢复 `product.stock` 的代码移入 `else` 分支，仅当为普通订单时才执行。团购订单下单未扣减 `product.stock`，取消时不应恢复。

---

### 🔴 Bug 2：`GroupOrder.status` 取消状态码不一致（严重）

- **文件**：`OrderService.java`（`cancelOrder`、`adminCancelOrder`）、`GroupOrderService.java`（`joinGroupActivity`）
- **修复内容**：将所有 `status = 5` 的取消状态统一改为 `status = 4`，与 `schema.sql` 注释定义一致。

---

### 🔴 Bug 3：库存扣减无并发保护，超卖风险（严重）

- **新增文件**：`ProductMapper.java`、`GroupActivityMapper.java` 新增 `decreaseStock` 方法
- **修改文件**：`OrderService.java`、`GroupActivityService.java`
- **修复内容**：改用数据库原子条件更新：
  ```sql
  UPDATE product SET stock = stock - #{quantity}
  WHERE id = #{id} AND stock >= #{quantity}
  ```
  返回行数为 0 时抛出异常并触发事务回滚，彻底消除竞态条件。

---

### 🟡 Bug 5：`adminCancelOrder` 缺少团购库存恢复（中等）

- **文件**：`OrderService.java`
- **方法**：`adminCancelOrder()`
- **修复内容**：参照 `cancelOrder()` 逻辑，加入 `GroupOrder` 查询：
  - 团购订单 → 置状态 `4`，调用 `increaseStock` 恢复团购活动库存，不动 `product.stock`
  - 普通订单 → 恢复 `product.stock`

---

### 🟡 Bug 6：`StpInterfaceImpl` 权限列表硬编码（中等）

- **文件**：`StpInterfaceImpl.java`
- **修复内容**：注入 `UserMapper`，根据 `user.roleId` 动态返回权限：
  - `roleId = 1`（管理员）→ `["admin.*", "user.read", "user.write"]`
  - 其他（普通用户）→ `["user.read", "user.write"]`

---

### 🟢 Bug 9：团购已售数量始终为 0（轻微）

- **文件**：`GroupActivityService.java`
- **方法**：`convertToVO()`
- **修复内容**：注入 `GroupOrderMapper`，动态统计非取消状态（`status != 4`）的订单数量赋给 `soldCount`。

---

### 🟢 Bug 8：`application.yml` 中 API Key 明文硬编码（安全风险）

- **文件**：`resources/application.yml`（MODIFY）、`resources/application-local.yml`（NEW）、`.gitignore`（MODIFY）
- **修复内容**：
  - `application.yml` API Key 改为 `${OPENAI_API_KEY:}`（环境变量占位符）
  - 新建 `application-local.yml` 供本地开发填写真实 Key（不提交）
  - `.gitignore` 追加 `**/application-local.yml` 等规则，防止误提交
  - `pom.xml` 新增打包排除规则，防止 `application-local.yml` 被打包进 JAR（已将 `maven-resources-plugin` 改为 `maven-jar-plugin`，修复本地 `mvn spring-boot:run` 无法读取该配置导致的启动报错问题）
  - `application.yml` 追加 `profiles.include: local`，自动加载本地配置

---

### 🟢 Bug 10：参团未登录时无友好跳转提示（前端）

- **文件**：`frontend/src/views/GroupActivityDetail.vue`
- **方法**：`joinGroup()`
- **修复内容**：跳转登录页时携带 `redirect` 参数，登录成功后自动返回当前活动详情页：
  ```js
  router.push({ path: '/login', query: { redirect: route.fullPath } })
  ```

---

### 🟢 Bug 11：客服悬浮按钮未拦截未登录用户（前端）

- **文件**：`frontend/src/components/FloatingMenu.vue`、`frontend/src/layouts/MainLayout.vue`
- **修复内容**：
  - 点击客服按钮时校验登录状态，未登录则携带 `redirect=${route.fullPath}&openChat=true` 跳转到登录页。
  - 登录成功返回后，前台页面监听 URL 参数自动展开客服聊天窗口并清理参数。

---

## ⏸ 暂缓修复（2 个）

| Bug | 原因 |
|-----|------|
| **Bug 4**（订单号重复） | 需引入 Snowflake 等全局 ID 方案，涉及架构改动，建议单独迭代 |
| **Bug 7**（Controller 双重异常） | 代码规范问题，不影响功能，可单独重构 |

---

## 修改文件清单

| 文件 | 变更类型 | 涉及 Bug |
|------|---------|---------|
| `service/OrderService.java` | MODIFY | Bug1、Bug2、Bug3、Bug5 |
| `service/GroupOrderService.java` | MODIFY | Bug2 |
| `service/GroupActivityService.java` | MODIFY | Bug3、Bug9 |
| `mapper/ProductMapper.java` | MODIFY | Bug3 |
| `mapper/GroupActivityMapper.java` | MODIFY | Bug3 |
| `config/StpInterfaceImpl.java` | MODIFY | Bug6 |
| `resources/application.yml` | MODIFY | Bug8 |
| `resources/application-local.yml` | NEW | Bug8 |
| `resources/application.yml`（profiles） | MODIFY | Bug8 |
| `pom.xml` | MODIFY | Bug8（排除打包） |
| `.gitignore` | MODIFY | Bug8 |
| `views/GroupActivityDetail.vue` | MODIFY | Bug10 |
| `components/FloatingMenu.vue` | MODIFY | Bug11 |
| `layouts/MainLayout.vue` | MODIFY | Bug11 |
