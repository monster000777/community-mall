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
