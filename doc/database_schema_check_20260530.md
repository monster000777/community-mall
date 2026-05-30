# 数据库文件与项目代码匹配性检查报告 - 2026-05-30

本报告旨在检查项目中的 `database/schema.sql` 定义是否与当前 Java 后端项目中的实体类 (Entity) 及数据库操作匹配。

## 检查结论
**检查结果：完全匹配（100% Match）**。
项目目前使用的是 **MyBatis-Plus** 框架，数据库字段与 Java 属性映射采用标准的**驼峰-下划线自动转换**机制。通过对 `schema.sql` 中的 13 张表与后端项目的 Entity、Mapper 以及自定义 SQL 进行逐一比对，没有发现任何不一致的情况。

---

## 详细比对情况说明

### 1. 业务表与 Entity 完美匹配表清单
以下表结构中的字段，与后端 `com.community.mall.entity` 包下的同名 Java 类属性一一对应，数据类型（Java 的 `Long`/`Integer`/`BigDecimal`/`LocalDateTime`/`String` 对应 MySQL 的 `BIGINT`/`TINYINT`/`DECIMAL`/`DATETIME`/`VARCHAR`/`TEXT`）与命名完全一致：
* **`user` (用户表)**  ↔  `User.java`
* **`address` (收货地址表)**  ↔  `Address.java`
* **`cart` (购物车表)**  ↔  `Cart.java`
* **`category` (商品分类表)**  ↔  `Category.java`
* **`product` (商品表)**  ↔  `Product.java`
* **`group_activity` (团购活动表)**  ↔  `GroupActivity.java`
* **`group_order` (团购订单表)**  ↔  `GroupOrder.java`
* **`group_participant` (团购参与者表)**  ↔  `GroupParticipant.java`
* **`order_master` (订单主表)**  ↔  `OrderMaster.java`
* **`order_item` (订单明细表)**  ↔  `OrderItem.java`

### 2. 权限相关表的特殊说明
在 `schema.sql` 中定义了角色权限相关的三张表：
* `role` (角色表)
* `permission` (权限表)
* `role_permission` (角色权限关联表)

**后端无对应 Java 实体类的原因**：
在 Java 代码中，权限验证使用 `Sa-Token` 框架。当前 `StpInterfaceImpl.java` 的实现是通过用户的 `role_id` 字段来硬编码动态返回角色和权限列表的（例如：`roleId = 1` 自动赋予 `admin.*` 全权限，普通用户则赋予 `user.read` / `user.write`）。因此，代码目前没有通过 MyBatis-Plus 去直接映射和操作这三张表。这是正常的业务设计，即使它们没有 Entity 类，也完全不影响现有系统的权限判断。

### 3. 自定义 SQL 注解匹配情况
经检索，整个后端项目仅在以下两个类中声明了原生 SQL `@Update` 注解：
* **`ProductMapper.decreaseStock`**: 
  `UPDATE product SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity} AND deleted_flag = 0`
* **`GroupActivityMapper.decreaseStock`**: 
  `UPDATE group_activity SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity}`

**比对结果**：
上述 SQL 中用到的表名（`product`、`group_activity`）和字段名（`stock`、`id`、`deleted_flag`）与 `schema.sql` 中表结构定义以及初始数据 100% 对应。

---

## 建议与后续注意事项
1. **测试数据同步**：`schema.sql` 的最后部分包含测试分类、测试商品和团购活动数据，这些数据使用的外键关联均基于子查询（例如 `(SELECT id FROM product WHERE product_name = 'xxx')`），这可以有效避免 ID 错乱导致的插入失败。
2. **逻辑删除**：`application.yml` 配置了 `logic-delete-field: deletedFlag`，项目表结构中的 `deleted_flag` 字段设置与代码完美对齐。
