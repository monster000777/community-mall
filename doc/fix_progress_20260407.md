# 修复进展文档

> 文档更新时间：2026-04-07

---

## ✅ 已修复内容

### 🔴 Bug 1：后台管理更新时间(updatedAt)与创建时间始终一致的问题（后端）
- **文件**：`community-mall-backend/src/main/java/com/community/mall/config/MybatisPlusConfig.java`
- **问题描述**：后台订单详情等涉及到数据库实体更新的地方，记录的`updatedAt`与`createdAt`永远相同，没有随更新操作自动刷新时间。
- **修复内容**：MyBatis Plus 的 `strictUpdateFill` 处于严格模式下，如果实体类中被更新字段本身拥有非 null 值（在先查询后更新的业务流中，查询出来的实体 `updatedAt` 不为空），那么就不会进行自动覆盖填充。在此，将其替换为底层的 `this.setFieldValByName("updatedAt", LocalDateTime.now(), metaObject);` 即强制无条件赋最新时间，解决更新时间不刷新的问题。

### 🟢 Bug 2：订单备注无法在后台订单管理中显示的问题（前端）
- **文件**：`frontend/src/views/admin/OrderManage.vue`
- **问题描述**：用户下单或者参与团购时填写的商品备注（`remark`），已经成功存入后端的订单数据表中，但前端“后台订单管理”界面中无法查看该备注信息。
- **修复内容**：
  1. 在后台订单管理的面包屑表格 `columns` 列表中新加入了“订单备注”列，限定固定宽度(`width: 120`)避免挤占其他列，并支持超长文本以省略号显示(`ellipsis: true`)。
  2. 针对部分订单操作列（按钮较多时）显示超出单元格或者出现折行的问题，将“操作列”的固定宽度放宽至了 `220` 来保证多按钮横向布局不溢出。
  3. 在“订单详情”对话框(`AModal` > `ADescriptions`)中新增了“订单备注”描述项，即使为空也会显示“无”。

### 🟡 Bug 3：购物车多商品结算时被错误合并为单一订单的问题（后端）
- **文件**：`community-mall-backend/src/main/java/com/community/mall/service/OrderService.java`, `community-mall-backend/src/main/java/com/community/mall/controller/OrderController.java`
- **问题描述**：在购物车中选中多个不同商品进行结算时，系统未把它们作为独立订单进行处理，而是统一创建为一个订单主表记录（包裹了多个订单明细），由于前端订单列表仅展示 OrderMaster 层面信息，导致用户在前端无法查看具体包裹的商品明细，也无法对单个商品级订单进行独立操作。
- **修复内容**：
  1. 修改了 `OrderService` 的 `createOrder` 方法，移除原先的“先计算总额创建单条 OrderMaster，再循环插入相关 OrderItem”的做法。
  2. 重构为在循环迭代购物车列表时，对于每件商品单独创建一份 `OrderMaster` 以及其对应的唯一 `OrderItem`。
  3. 调整了 `OrderController` 的 `createOrder` API 和 Service 层的返回值类型，将其由单一 ID 变更为返回 `List<Long>` 类型的独立订单 ID 列表。
  4. **解决遗留/并发潜在 Bug**：由于拆分为多个独立订单之后，原本执行一次的 `generateOrderNo()` 会在紧凑的 `for` 循环中瞬间被执行多次。原有的 `时间戳 + 线程ID + 随机数(0~9999)` 机制在同毫秒多频次调用时具有万分之一的重复概率，极可能触发数据库 `order_no` 唯一索引冲突。所以我们将其改重构，引入了 `AtomicInteger` 静态序列保证同微秒的循环体内也不会出现相同的订单号，实现了订单生成的绝对安全机制。
