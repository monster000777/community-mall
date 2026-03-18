# Bug 修复进展文档

> 文档更新时间：2026-03-18  
> 编译验证：✅ `mvn compile` 通过（含遗留Bug修复后二次验证）

---

## 本次修复范围

本次针对 `doc/bug_report.md` 中报告的 10 个 Bug，完成了其中 **6 个**（含全部 3 个严重Bug）的修复。

---

## ✅ 已修复（6 个）

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

## ⏸ 暂缓修复（4 个）

| Bug | 原因 |
|-----|------|
| **Bug 4**（订单号重复） | 需引入 Snowflake 等全局 ID 方案，涉及架构改动，建议单独迭代 |
| **Bug 7**（Controller 双重异常） | 代码规范问题，不影响功能，可单独重构 |
| **Bug 8**（API Key 明文） | 运维配置问题，需结合部署环境处理 |
| **Bug 10**（前端未登录提示） | 前端交互体验问题，可单独迭代修复 |

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
