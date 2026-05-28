# 项目完整性检查与深度分析报告

> **检查时间**：2026-05-26  
> **审查范围**：后端核心 Service 与 Controller、Sa-Token 鉴权拦截、前端响应拦截器、数据库设计  
> **当前编译状态**：后端 `mvn clean compile` 已成功通过  

---

## 一、 历史修复审计与确认

根据我们对当前源码的浏览，确认前两期修复工作已正确落地：
1. **取消团购订单时库存虚增（已解决）**：`OrderService.cancelOrder` 中已将商品库存的恢复限制在普通订单逻辑分支，团购订单取消时仅恢复团购活动本身的库存（`group_activity.stock`）。
2. **团购取消状态码不一致（已解决）**：团购取消状态在 Service 中已由 `5` 统一更正为 `4`，与数据库 Schema 定义完全一致。
3. **库存扣减超卖（已解决）**：已使用 `decreaseStock` 的原子 SQL（`SET stock = stock - #{quantity} WHERE stock >= #{quantity}`）并配合受影响行数校验，具备高并发下的超卖保护。
4. **购物车拆单后订单号碰撞（已解决）**：在普通订单生成中引入了 `ORDER_SEQ` (AtomicInteger) 静态序列，保证了高并发时同微秒产生的订单号的绝对唯一性。
5. **团购暗黑模式不适配（已解决）**：前端立即参团确认界面已适配全局 CSS 自适应变量。

---

## 二、 深度完整性检查发现的隐患与缺陷

在对项目业务流进行深度代码审计后，发现了以下几个核心层面的缺陷，它们将直接影响高并发数据一致性、前后端用户体验和动态权限系统的可用性：

### 1. Controller 冗余 try-catch 架空全局异常，导致前端 401 自动跳转失效（严重）
* **涉及文件**：几乎所有的 Controller 类（如 [OrderController.java](file:///E:/community-mall/community-mall-master/community-mall-backend/src/main/java/com/community/mall/controller/OrderController.java)）
* **缺陷描述**：
  在各 Controller 接口方法中，几乎全部硬编码了 `try-catch(Exception e)` 块，并手动返回 `Result.error(e.getMessage())`。而在项目中又同时注册了全局异常拦截器 [GlobalExceptionHandler.java](file:///E:/community-mall/community-mall-master/community-mall-backend/src/main/java/com/community/mall/exception/GlobalExceptionHandler.java)。
* **严重影响**：
  当用户 token 过期或未登录时，Sa-Token 会在 Controller 运行方法前/中抛出 `NotLoginException` 异常。由于该异常被 Controller 的 `catch` 吞掉，接口最终向前端返回的是一个 **HTTP 200 OK** 响应，伴随数据体 `{"code":500, "message":"未提供Token", "data":null}`。
  这导致前端的 Axios 响应拦截器 [request.js](file:///E:/community-mall/community-mall-master/frontend/src/api/request.js#L41-L67) 无法拦截到正常的 `HTTP 401` 状态码，从而**无法清除过期状态、无法自动重定向到登录页面 `/login`**，用户未登录时的重定向逻辑彻底崩溃，用户将看到无数个“请求失败：未提供Token”的业务提示框。
* **重构方案**：
  清理 Controller 中的冗余 `try-catch` 代码，让异常自然抛出，交由全局 `GlobalExceptionHandler` 处理并返回正确的 HTTP 状态码。

---

### 2. 库存恢复缺乏并发保护，存在更新丢失（Lost Update）风险（中等）
* **涉及文件**：
  - [OrderService.java](file:///E:/community-mall/community-mall-master/community-mall-backend/src/main/java/com/community/mall/service/OrderService.java) (`cancelOrder` 与 `adminCancelOrder` 中的普通商品库存恢复)
  - [GroupActivityService.java](file:///E:/community-mall/community-mall-master/community-mall-backend/src/main/java/com/community/mall/service/GroupActivityService.java) (`increaseStock` 团购库存恢复)
* **缺陷描述**：
  在订单取消或退款时，对于库存的加回（恢复），系统目前的做法是：从数据库读取实体 `productMapper.selectById()`，在 Java 内存中做 `stock = stock + quantity` 操作，最后执行 `updateById(product)` 将实体全部更新回数据库。
* **严重影响**：
  这是一个经典的并发更新丢失问题。在多人同时退款、取消订单或发生高并发扣减更新的瞬间，先读取的线程可能会覆盖后更新的线程写回的数据，导致最终的物理库存数加回错误（与真实货品数量不符）。
* **重构方案**：
  在 Mapper 中增加原子加库存方法（如 `increaseStock`），使用类似于 `UPDATE product SET stock = stock + #{quantity} WHERE id = #{id}` 的自增 SQL 来防止 Lost Update。

---

### 3. 团购订单号生成机制存在极微弱并发碰撞概率（轻微）
* **涉及文件**：[GroupOrderService.java](file:///E:/community-mall/community-mall-master/community-mall-backend/src/main/java/com/community/mall/service/GroupOrderService.java#L166-L168) (`generateOrderNo()`)
* **缺陷描述**：
  普通订单已经通过 `ORDER_SEQ` 解决了同微秒拆单产生的唯一性冲突，但团购订单的订单号目前仍然由以下逻辑生成：
  ```java
  private String generateOrderNo() {
      return "GO" + System.currentTimeMillis() + (int) (Math.random() * 1000);
  }
  ```
  该逻辑仅依靠 `System.currentTimeMillis()` 加上 `0-999` 的随机数。在万人秒杀抢团购的高并发场景下，同一个毫秒内如果有很多请求，那么有极高几率在 `Math.random() * 1000` 时发生随机数碰撞。
* **严重影响**：
  由于数据库中 `order_no` 字段具有唯一索引，订单号碰撞会引发数据库 Unique Key 冲突，导致该事务回滚，用户抢购下单直接报错失败。
* **重构方案**：
  团购订单的订单号也应使用 `AtomicInteger` 静态序列，或者统一抽取公共的 Snowflake (雪花算法) 订单号生成工具类。

---

### 4. 数据库权限关联表闲置，权限系统无法动态维护（中等）
* **涉及文件**：[StpInterfaceImpl.java](file:///E:/community-mall/community-mall-master/community-mall-backend/src/main/java/com/community/mall/config/StpInterfaceImpl.java#L28-L49) (`getPermissionList()`)
* **缺陷描述**：
  数据库中虽有 `permission` (权限表) 和 `role_permission` (角色权限关联表)，但 Sa-Token 的权限验证实现 `StpInterfaceImpl` 中依然使用了写死的逻辑：若 `roleId = 1` 赋予 `admin.*`、`user.read`、`user.write`，若为普通用户则只赋予 `user.read`、`user.write`。
* **严重影响**：
  这使数据库中的权限与关联表完全闲置，管理员无法通过在数据库中新增或修改角色权限关系来动态控制系统行为，这不符合完整权限系统设计标准。
* **重构方案**：
  利用 MyBatis-Plus 关联查询数据库的 `role_permission` 与 `permission` 表，实现动态且可伸缩的权限鉴权。

---

## 三、 下一步行动计划

为了使这个项目在架构完整性上真正达到生产级别的标准，建议在下一个迭代中展开如下重构工作：
1. **阶段 1：全局异常通道打通** — 清理 Controller 层中所有的 try-catch，并测试未登录时前端是否能正确且流畅地重定向到登录页面。
2. **阶段 2：高并发库存数据一致性优化** — 编写 Product 和 GroupActivity 的 `increaseStock` 数据库原子操作，消灭 Lost Update 并发 Bug。
3. **阶段 3：雪花算法 ID 提取与团购防重** — 引入一个轻量的 Snowflake 生成器或静态原子序列，使团购与普通订单的订单号生成均绝对安全。
4. **阶段 4：动态数据库权限改造** — 编写 SQL 关联查询获取用户的实际权限列表，移除 `StpInterfaceImpl` 中的硬编码分支。
