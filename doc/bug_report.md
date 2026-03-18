# Community Mall 项目完整性与 Bug 分析报告

> 生成时间：2026-03-17  
> 审查范围：后端（Spring Boot + MyBatis-Plus + Sa-Token）+ 前端（Vue 3 + Vite）+ 数据库（MySQL schema.sql）

---

## 一、项目完整性总结

| 模块 | 状态 | 备注 |
|------|------|------|
| 数据库 schema | ✅ 基本完整 | 表结构清晰，初始化数据齐全 |
| 用户认证（注册/登录/登出） | ✅ 完整 | Sa-Token 集成正确 |
| 商品管理（CRUD + 上下架） | ✅ 完整 | 包含管理员接口 |
| 分类管理 | ✅ 完整 | 基础分类接口齐全 |
| 购物车 | ✅ 完整 | 添加/修改/删除/查询 |
| 收货地址 | ✅ 完整 | 含默认地址管理 |
| 普通订单 | ✅ 基本完整 | 存在若干 Bug（见下） |
| 团购活动 | ✅ 基本完整 | 存在若干 Bug（见下） |
| 团购订单 | ⚠️ 有 Bug | 状态码定义不一致、库存恢复逻辑错误 |
| AI 智能客服 | ✅ 完整 | LangChain4j 集成 |
| 权限控制 | ⚠️ 有 Bug | StpInterfaceImpl 权限列表硬编码 |
| 前端路由 | ✅ 完整 | 含路由守卫 |
| 前端管理后台 | ✅ 完整 | 商品/订单/用户/团购管理 |
| 文件上传 | ✅ 完整 | UploadResourceConfig 配置 |
| 定时任务 | ✅ 完整 | 每分钟更新团购活动状态 |

---

## 二、Bug 列表（按严重程度排序）

---

### 🔴 Bug 1：取消团购订单时普通商品库存被双重恢复（严重）

**位置：** `OrderService.java` → `cancelOrder()` 方法（第 198-209 行）

**问题描述：**  
当用户取消一个团购订单时，`cancelOrder` 方法会：
1. 先恢复 `group_activity` 表的活动库存（第 195 行，正确）
2. **然后又对 `order_item` 里的所有商品执行库存恢复**（第 198-209 行）

但是，团购订单在下单时（`GroupOrderService.joinGroupActivity()`）并没有扣减 `product` 表的 `stock` 字段，只扣减了 `group_activity.stock`。  
因此，取消团购订单时对 `product.stock` 做的 `+quantity` 操作会导致**商品库存凭空增加**。

**影响：** 团购商品被取消后，`product` 表中的库存数量会虚增，可能超过真实库存上限。

**复现路径：** 用户参与团购 → 创建团购订单 → 取消该订单 → 查看 `product.stock` 字段，会比取消前更多。

**修复建议：**  
在 `cancelOrder` 中恢复库存前，先判断该订单是否为团购订单（即 `groupOrder != null`）。如果是团购订单，跳过对 `product.stock` 的恢复；如果是普通订单，才执行 `product.stock` 的恢复。

```java
// 修复示例
if (groupOrder == null) {
    // 仅普通订单才恢复商品库存
    LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(OrderItem::getOrderId, orderId);
    List<OrderItem> items = orderItemMapper.selectList(wrapper);
    for (OrderItem item : items) {
        ...
    }
}
```

---

### 🔴 Bug 2：GroupOrder 状态码与 schema 定义不一致（严重）

**位置：**  
- `schema.sql` 第 191 行：`status` 注释为 `（1-待支付，2-已支付，3-已完成，4-已取消）`  
- `OrderService.java` 第 191 行：`groupOrder.setStatus(5)`（取消时设为 5）
- `GroupOrderService.java` 第 68 行：`.ne(GroupOrder::getStatus, 5)` （排除状态 5）

**问题描述：**  
数据库 schema 定义 `group_order.status` 取消状态为 **4**，但代码中统一使用 **5** 作为取消状态。两处定义完全矛盾，会导致：
- 查询"排除已取消订单"的逻辑失效（用 `ne(status, 5)` 但实际取消状态若是 4 则无法正确过滤）
- 前端根据状态码展示文字时出现错误

**修复建议：** 统一将 `group_order` 的取消状态定为 `4`（与 schema 一致），修改代码中所有 `status = 5` 的地方。或者修改 schema 注释并在项目中统一用 `5`。

---

### 🔴 Bug 3：库存扣减/恢复无并发保护，高并发下存在超卖风险（严重）

**位置：**  
- `OrderService.java` 第 64-71 行（创建订单时检查并扣减库存）
- `GroupOrderService.java` 第 59-61 行、第 156 行（团购库存检查与扣减）
- `GroupActivityService.java` 第 221-232 行（`decreaseStock`）

**问题描述：**  
所有库存操作的模式均为：
```
1. SELECT 查库存
2. 检查是否充足
3. UPDATE 扣减
```
这是典型的「读-检查-写」竞态条件。在高并发场景下，两个请求可能同时读到库存为 1，都通过检查，最终导致库存扣减为 -1（超卖）。

**修复建议：** 使用数据库乐观锁或条件更新：
```sql
UPDATE product SET stock = stock - ? WHERE id = ? AND stock >= ?
```
若更新行数为 0，说明库存不足，抛出异常并回滚事务。

---

### 🟡 Bug 4：两处订单号生成器存在并发重复风险（中等）

**位置：**  
- `OrderService.java` 第 122-129 行 `generateOrderNo()`
- `GroupOrderService.java` 第 166-168 行 `generateOrderNo()`

**问题描述：**  
两个类各自维护了一个订单号生成方法，逻辑也不同：
- `OrderService`：`yyyyMMddHHmmss + threadId % 10000 + random(10000)` — 同一秒内同一线程必然重复
- `GroupOrderService`：`"GO" + System.currentTimeMillis() + random(1000)` — 毫秒级冲突概率低但不为零

订单号字段 `order_no` 有唯一索引，重复时会抛出数据库异常，事务回滚，用户下单失败。

**修复建议：** 使用 Snowflake 算法或数据库序列生成全局唯一订单号，并抽取为公共工具类（避免两处分散实现）。

---

### 🟡 Bug 5：取消订单时，团购未调用管理员取消流程的库存恢复逻辑（中等）

**位置：** `OrderService.java` → `adminCancelOrder()` 方法（第 282-306 行）

**问题描述：**  
管理员取消订单 `adminCancelOrder()` 会恢复 `product.stock`，但没有检查是否为团购订单并恢复 `group_activity.stock`。与 `cancelOrder()` 相比，`adminCancelOrder()` 缺少对 `GroupOrder` 的状态更新和团购活动库存恢复逻辑，造成团购活动库存永久丢失。

**修复建议：** 参照 `cancelOrder()` 中的逻辑，在 `adminCancelOrder()` 里也加入对 `GroupOrder` 的查询和团购库存恢复。

---

### 🟡 Bug 6：`StpInterfaceImpl` 权限列表硬编码，所有用户权限相同（中等）

**位置：** `StpInterfaceImpl.java` 第 20-27 行

**问题描述：**  
```java
public List<String> getPermissionList(Object loginId, String loginType) {
    List<String> list = new ArrayList<>();
    list.add("user.read");
    list.add("user.write");
    return list;  // 所有用户权限完全一样！
}
```
`getPermissionList` 返回了硬编码的固定权限，没有从数据库 `role_permission` 表动态查询。这意味着所有用户（包括普通用户和管理员）都拥有完全相同的权限，数据库中的 `permission` 和 `role_permission` 表形同虚设。

**修复建议：** 注入 `UserMapper`，根据 `loginId` 查询用户角色，再关联查询 `role_permission` → `permission` 表，动态返回权限列表。

---

### 🟡 Bug 7：`GlobalExceptionHandler` 与 Controller 异常处理双重捕获（轻微但冗余）

**位置：** 各 Controller（如 `OrderController.java`）+ `GlobalExceptionHandler.java`

**问题描述：**  
Controller 中每个方法都有 `try-catch(Exception e)` 并手动返回 `Result.error()`，而项目同时存在 `GlobalExceptionHandler` 处理 `RuntimeException`。两层异常处理逻辑重复，且 Controller 层的 catch 会阻止 `GlobalExceptionHandler` 生效，导致统一异常处理机制失效（如 HTTP 状态码无法正确返回 400/401/403/500）。

**修复建议：** 删除各 Controller 方法中的 `try-catch` 块，让异常直接抛出由 `GlobalExceptionHandler` 统一处理。

---

### 🟢 Bug 8：`application.yml` 中 API Key 明文硬编码（安全风险）

**位置：** `application.yml` 第 72 行

```yaml
openai:
  api-key: sk-79dfe1754a694735862a0c8a995a7be4
```

**问题描述：** DeepSeek API Key 明文写入配置文件并提交到 Git，存在密钥泄露风险。

**修复建议：** 使用环境变量或 `.env` 文件配置，并将 `application.yml` 中的敏感信息替换为占位符（如 `${OPENAI_API_KEY}`），同时将真实值加入 `.gitignore`。

---

### 🟢 Bug 9：团购活动当日售出数量始终返回 0（功能缺失）

**位置：** `GroupActivityService.java` 第 300 行

```java
// 计算已售数量（这里简化处理，实际应该从订单表统计）
vo.setSoldCount(0);
```

**问题描述：** 前端显示的团购已售数量永远是 0，无法真实反映销售情况。

**修复建议：** 从 `group_order` 表统计非取消状态的已购数量：
```java
wrapper.eq(GroupOrder::getActivityId, activity.getId()).ne(GroupOrder::getStatus, 4/5);
vo.setSoldCount(groupOrderMapper.selectCount(wrapper).intValue());
```

---

### 🟢 Bug 10：`GroupActivityDetail` 页面参与团购未登录时体验差（前端）

**位置：** `frontend/src/views/GroupActivityDetail.vue`（路由无 `requiresAuth`）

**问题描述：**  
路由配置中 `group-activities/:id` 无需登录即可访问（因为是公开活动列表），但当未登录用户点击"立即参团"时会直接收到后端 401 错误，没有友好的跳转登录页提示逻辑（需核查 Vue 页面内的处理）。

---

## 三、总结

| 严重级别 | 数量 | 主要问题 |
|---------|------|---------|
| 🔴 严重 | 3 个 | 团购取消库存双重恢复、状态码不一致、超卖风险 |
| 🟡 中等 | 4 个 | 订单号重复风险、adminCancelOrder 缺失逻辑、权限硬编码、异常处理冲突 |
| 🟢 轻微 | 3 个 | API Key 泄露、售出数量为 0、参团未登录提示 |

最需要优先修复的是：**Bug 1（库存双重恢复）**、**Bug 2（状态码不一致）**、**Bug 3（超卖风险）**，这三个直接影响核心业务数据的准确性。
