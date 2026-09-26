# 全项目安全加固与订单/团购生命周期修复进展文档

> 文档更新时间：2026-09-26
> 修复范围：全项目 review（后端 / 前端 / 数据库与部署配置）发现的高危与中危问题
> 验证状态：✅ 后端 `mvn test`（16 个用例）、前端 `pnpm test`（37 个用例）、`pnpm lint`（0 error）、`pnpm build` 全部通过
> 迭代方式：实现 → 编译/测试 → 独立对抗式复查（三轮）→ 二次修复 → 再验证

---

## 一、本轮修复总览

| 类别 | 数量 | 说明 |
|------|------|------|
| 🔴 高危 | 10 | 越权、防爆破、存储型 XSS、XSS、401 链路、快捷登录泄漏、退款库存错账、订单竞态、密钥泄漏面、compose 抢跑 |
| 🟡 中危 | 约 20 | 团购状态机、超时取消、事务、原子库存、角色实时查库、TTS/聊天限制、前端中危清单、nginx/部署硬化 |
| 🟢 历史遗留 | Bug 7（双层异常捕获） | 引入 BusinessException + ExceptionSupport 统一收敛，全部 service/controller 完成 |
| ✅ 新增测试 | 后端 16 / 前端 37 | 覆盖本轮全部高危行为，形成回归保护网 |

---

## 二、后端修复明细

### 2.1 安全

1. **购物车越权下单封堵**（`OrderService.createOrder`）
   - 原查询 `selectBatchIds(cartIds)` 不校验属主，可枚举他人 cartId 越权下单并顺带删除他人购物车项。
   - 修复：按 `ID in + userId` 双重过滤查询与删除，数量不符时报"部分购物车商品不存在或无权操作"。

2. **验证码防暴力破解**（`VerificationCodeService`）
   - 原校验失败不计数、不锁定，6 位码可在有效期内无限枚举，配合未鉴权的 `/auth/reset-password` 可重置任意账号。
   - 修复：失败计数与验证码同生命周期，连续 5 次失败立即作废；新增每日每号 10 条发送上限；换发新验证码时清除旧失败计数；计数器 expire 兜底续期防"永久限流"。

3. **头像上传存储型 XSS**（新增 `common/ImageUploader`）
   - 原两处上传（`UserProfileController` / `AdminUserController`）扩展名直接取自客户端文件名，可上传 `.html`/`.svg` 经同域 `/api/uploads/**` 静态访问窃取 token。
   - 修复：白名单（jpg/jpeg/png/webp）+ 文件头魔数双重校验 + 5MB 上限 + 服务端生成随机文件名；上传目录统一走 `upload.path` 配置（与 `UploadResourceConfig` 映射一致）；两个上传入口收敛到该组件。

4. **CORS 收紧**（`CorsConfig`）：`allowedOriginPatterns("*") + allowCredentials(true)` 改为仅放行 `localhost/127.0.0.1`（生产前端经 nginx 反代为同源，不依赖 CORS）。

5. **全局异常不再泄漏内部细节**（`GlobalExceptionHandler`）：RuntimeException 的 message（可能含 SQL/NPE 细节）不再直接返回客户端，改为记录完整堆栈 + 通用文案；移除 `printStackTrace`。

6. **TTS 接口限流**（`TtsController`）：文本长度上限 500 字；用户级限流 20 次/分钟（Redis 计数 + 兜底续期）；云端返回 `choices` 为空时明确报错而非 NPE。

7. **聊天会话绑定用户**（`ChatController`）：sessionId 强制拼接 `userId-`，防止任意指定他人会话 ID 读写他人的 AI 对话记忆（RedisChatMemoryStore 历史上下文）；客户端部分截断至 64 字符防恶意超长 key。

### 2.2 订单 / 团购生命周期

8. **退款按团购/普通分流**（`OrderService.adminRefundOrder`，历史 Bug 1 同类问题在退款路径复发）
   - 原退款对所有订单一律恢复 `product.stock`、回退 `sales`，但团购下单从未扣减过 `product.stock` —— 团购退款导致库存虚增、销量错减。
   - 修复：团购订单 → 更新 `group_order` 状态并恢复活动库存；普通订单 → 原子恢复商品库存与销量。

9. **订单状态流转条件更新防竞态**（cancelOrder / payOrder / adminShipOrder / adminCompleteOrder / adminCancelOrder / adminRefundOrder）
   - 原全部为"读 → 内存判断 → 整对象写回"，并发下可出现已支付被取消、双重取消恢复两次库存。
   - 修复：统一改为 `WHERE order_status = x` 条件更新，返回 0 行即抛"订单状态已变更"。

10. **payOrder 联动团购状态机**：原 `group_order` 创建后永远停在待支付。修复：支付成功同步置为已支付；`soldCount` 统计口径改为仅计已支付/已完成（未支付不再刷高已售）。

11. **超时未支付订单自动取消**（`ScheduledTasks` 新增定时任务）：超过 30 分钟未支付的订单每分钟自动走标准取消流程，释放被占用的商品库存/活动库存（原未支付订单永不超时，库存被永久锁死）。

12. **库存操作全部原子化**：`ProductMapper` 新增 `increaseStock/increaseSales/decreaseSales`，`GroupActivityMapper` 新增 `increaseStock`，与既有 `decreaseStock` 对称，彻底消除"读-改-写"丢失更新；`decreaseSales` 带下限保护，0 行更新落 warn 日志。

13. **取消/退款补齐事务**：`cancelOrder/adminCancelOrder/adminRefundOrder` 增加 `@Transactional(rollbackFor)`，状态更新与库存恢复原子化。

14. **参团校验补全**（`GroupOrderService`）：参团前校验商品上架状态（与普通下单对齐）；活动状态值域校验（0-2）防任意整数落库。

### 2.3 认证与杂项

15. **角色实时查库**（`StpInterfaceImpl.getRoleList`）：原从登录 Session 读角色快照，管理员降权/删号后最长 24 小时仍可越权。修复：改为实时查询数据库。
16. **Session 不再存储用户实体**（`AuthService.login`）：移除 `role`/`userInfo`（含 BCrypt 密码哈希）写入，无任何读取方，消除泄漏面。
17. **购物车校验**（`CartService`）：数量必须为正且 ≤999、商品需上架、`updateCartQuantity` 非本人时显式报错（原静默无操作）。
18. **统一异常体系**（历史遗留 Bug 7）：新增 `exception/BusinessException`（用户级提示，message 可透传前端）与 `common/ExceptionSupport`（Controller catch 统一入口）；全后端 service 层 37 处用户级 RuntimeException 迁移、controller 层 46 处 catch 收敛。
19. **AI 工具返回库存**（`ProductTool`）：提示词要求模型报真实库存但工具不返回，导致模型编造。修复：输出增加库存字段。
20. `CreateOrderRequest.cartIds` 补 `@NotEmpty`；`getOrderDetail` 查不到时返回"订单不存在"而非 `Result.success(null)`；`isOnSale` 判空防 NPE。

---

## 三、前端修复明细

1. **AI 聊天 XSS**：marked 渲染接入 DOMPurify（`dompurify@^3.4.16`），渲染逻辑提取为 `utils/markdown.js`；`<img onerror>`/`<script>`/`javascript:` 链接均被剥离，表格/代码块等正常 Markdown 不受影响。
2. **401 处理链路修复**（后端未登录返回 HTTP 200 + body `{code:401}`，原拦截器按 HTTP 状态码判断导致整条链路失效）：
   - 过期登录：先同步 `localLogout()`（新增，清内存 + localStorage）再跳转登录页（带 redirect），修复"路由守卫在微任务中执行、token 未清导致被弹回 /admin 或 /"的时序问题；
   - 游客（无 token，如未登录使用智能客服）：仅展示后端提示，不登出不跳转，不中断浏览；
   - once 标志防并发重复弹窗。
3. **管理员快捷登录门控**：登录页内置的 admin/123456 快捷按钮改为 `import.meta.env.DEV` 门控，生产构建不渲染。
4. **购物车数量修改失败**：改为从服务端重拉购物车（原本地回滚在快速连点时与相邻请求交错会导致 UI 与服务端失同步）。
5. **商品编辑表单脏字段污染**：编辑回填改白名单字段、`resetForm` 用初始模板重建，"添加商品"不再携带上次编辑的 id/createdAt/sales。
6. **TTS blob 泄漏**（`ttsPlayer.js`）：`stop()` 先置空 `onended/onerror` 再 pause 并 `revokeObjectURL`，修复每次中断播报泄漏一个 objectURL。
7. **详情页路由参数 watch**：`ProductDetail` / `GroupActivityDetail` 同组件跳转时重新加载数据；后者同步重置参团表单与弹窗状态（防带旧数量/地址参团）。
8. **团购管理商品下拉分页参数**：`page` → `current`（原参数名错误导致下拉只拿到默认分页约 10 条）。
9. **编辑用户空密码不提交**、AI 客服双重报错去重并保留"团团开小差了"兜底回复、`ttsPlayer.listen(null)` 卸载监听、死代码与 console.log 清理。

---

## 四、部署与配置硬化

1. **后端 `.dockerignore`**（新增）：排除 `application-local.yml` 等，防止含真实 API Key 的本地配置进入镜像构建上下文与 build cache 层。⚠️ **配套要求：`application-local.yml` 中已落盘的两把 Key 必须在服务商侧轮换**。
2. **docker-compose**：
   - mysql/redis 增加 healthcheck，backend 改 `depends_on: service_healthy` —— 修复冷启动时 backend 抢在 MySQL 首次初始化（30-60 秒）完成前启动而必然崩溃重启的问题；
   - 宿主机端口 `3307/6380` 改绑 `127.0.0.1`（原 0.0.0.0 暴露无密码 Redis 会话存储）；
   - `MYSQL_ROOT_PASSWORD` / `MYSQL_USERNAME` 可经 `.env` 覆盖（application.yml 同步改为 `${MYSQL_USERNAME:root}` / `${MYSQL_PASSWORD:root}` 占位符）；
   - 移除已废弃的 `version: '3.8'`。
3. **nginx.conf**：`client_max_body_size 20m`（原默认 1m，头像上传必 413，与 Spring multipart 20MB 对齐）；gzip 修正（补 application/json、移除已压缩的图片类型）；`resolver + 变量 proxy_pass` 动态解析 backend（容器重启换 IP 后自动恢复）。
   - ⚠️ 复查发现的致命陷阱已规避：变量含 URI 时 nginx 会整体替换原始请求 URI，变量值必须不带 `/api` 路径，否则全站 API 404。
4. **后端 Dockerfile**：恢复 `mvn dependency:go-offline` 依赖缓存（源码变动不再全量重下依赖）；jar 拷贝改通配符（pom 版本变更不断链）。
5. **前端 Dockerfile**：保持 `npm install`（项目锁文件真源为 `pnpm-lock.yaml`，仓库中无 package-lock.json；容器内用 pnpm 存在 v10 built-dependencies 跨平台限制）。
6. **`.env.example`**：补 `MYSQL_ROOT_PASSWORD` 说明（含"改密码需删卷重建"提示）。

---

## 五、冒烟测试（真实运行验证，2026-09-26）

在本地 8081 端口以新代码启动真实实例（共用本地 MySQL/Redis，未干扰 8080 旧实例），API 级全链路冒烟：

| 验证项 | 结果 |
|--------|------|
| 管理员登录 → token 签发 | ✅ |
| 未登录访问受保护接口返回 body `{code:401}`（HTTP 200）——前端 401 契约 | ✅ |
| 购物车 quantity=0 / 不存在商品 被新校验拒绝 | ✅ |
| **非法编码请求体返回 400 友好提示**（冒烟中发现的新增 handler，此前落入通用 500） | ✅ |
| 收货地址创建 | ✅ |
| 加购 → 拆单下单 → 支付 → 状态=2 已支付 → **库存精确扣减 91→89** | ✅ |
| 第二单取消 → 状态=5 → **库存精确恢复 89→89** → 重复取消被条件更新拦截 | ✅ |
| 伪造他人 cartId 下单被属主校验拒绝 | ✅ |
| 团购参团 → 团购订单支付（group_order 状态联动） | ✅ |
| AI 智能导购 | ⚠️ 链路与降级行为正确（友好提示+堆栈落日志），**账户余额不足（insufficient_user_quota）**属外部问题，充值后即可用 |

结论：核心业务（订单状态机、库存原子操作、越权防护、团购生命周期、异常处理）在真实运行环境全部验证通过。冒烟临时脚本已删除；8081 测试实例已停止；开发库中留有少量冒烟数据（地址 SmokeTest、订单 79 等），无碍。

---

## 六、测试体系（本轮新增）
项目此前**前后端均无任何测试**。本轮补齐：

**后端（JUnit 5 + Mockito，`mvn test`，16 个用例）**
- `VerificationCodeServiceTest`：失败计数、5 次上限作废、每日上限、成功清理
- `ImageUploaderTest`：白名单、**"html 改名 .jpg"伪装场景**、WebP 魔数、超限、截断内容
- `ExceptionSupportTest`：业务异常透传、系统异常（SQL/NPE）message 不回传客户端

**前端（Vitest，`pnpm test`，37 个用例，4 个文件）**
- `markdown.test.js`：XSS 回归线（注入剥离 + 正常 Markdown 不误伤；因 happy-dom 对 `<template>` 支持不完整，单独使用 jsdom 环境）
- `request.test.js`：401 时序回归（localLogout 同步先于跳转）、游客分支、once 防重、token 注入
- `ttsPlayer.test.js`：objectURL 生命周期（泄漏回归）、播放竞态、降级路径、输入清洗
- `user.test.js`：localLogout 同步清态、logout 网络失败兜底、login 落库

---

## 七、对抗式复查发现并已修复的"修复引入的问题"（8 项）

这轮复查的价值证明：核心修复全部验证正确，但抓到了修复自身引入的新问题，全部已修——

1. 🔴 nginx 变量 proxy_pass 吞路径（部署后全站 API 404）→ 变量去掉 `/api` 路径
2. 🔴 前端 Dockerfile COPY 已删除的 package-lock.json（构建必失败）→ 回退 npm install
3. 🟠 401 跳转与路由守卫时序冲突（跳转被弹回）→ 新增同步 localLogout
4. 🟡 游客使用智能客服被误"登出+跳转" → 区分游客与过期登录
5. 🟡 购物车快速连点回滚竞态 → 失败改从服务端重拉
6. 🟡 AI 客服双重报错且无兜底 → 去重 + 恢复兜底消息
7. 🟢 活动切换残留参团表单 → watch 内重置
8. 🟢 compose 显式注入 MYSQL_USERNAME

后端复查另修复 4 处小问题：换码清失败计数、Redis 计数 expire 兜底、isOnSale 判空、静默 0 行更新补日志。

### 第三轮复查（针对 401 时序修复、markdown 提取与测试质量），发现并已修复 6 项

1. 🟠 **迟到的服务端 logout 会抹掉新登录会话**：401 路径 fire-and-forget 的 `logout()` 在网络往返期间若用户重新登录，其 `finally` 里的 `localLogout()` 会误清新 token → 删除该调用（其请求本就不带 satoken 头，服务端无法定位会话，属无效请求）
2. 🟠 **并发 401 多弹一次后端提示**：once 标志检查前移至游客分支之前（游客路径无 push，若被标志拦截会导致标志永不复位，故必须保持游客分支在标志作用域之外的前置判断已被注释说明）
3. 🟡 **测试假绿**：并发 401 用例的 mock `localLogout` 不清 token，断言在 mock 下成立而与真实行为相悖 → mock 复现真实行为（同步清 token），断言变为锁定正确行为
4. 🟡 **时序用例未锁顺序**：用 `invocationCallOrder` 断言 `localLogout` 严格先于 `router.push`；删除误导性的空 afterEach 注释
5. 🟢 **活动详情切换残留**：watch 补 `activity.value = null`（B 活动加载失败时不再展示 A 的旧价格/库存，模板 `v-if="activity"` 空态兜底）
6. 🟢 测试设施小修：onended 泄漏回归用例从空转改为真实驱动（登记 Audio 实例手动触发）；删除死代码；vitest include 收窄为 `*.test.js`（防 helper 文件被误当测试套件）

**复核确认无恙的**：markdown 提取（无残留引用、行为一致且为安全增强）、Cart 失败重拉（无死循环）、GroupActivityDetail 表单重置字段完整、游客分支不占用 once 标志的设计。
**记录在案（接受不改）**：购物车失败重拉与在途成功请求存在短暂竞态窗口（用户下次操作自愈，彻底解决需请求序列号，性价比低）。

---

## 八、记录在案、暂不处理的事项

| 事项 | 原因 |
|------|------|
| 限购校验 TOCTOU（并发参团绕过 limitPerUser） | 需分布式锁/唯一约束，属架构级改动 |
| 全库无外键、软删用户名/手机号唯一键冲突 | 需数据迁移脚本，建议单独迭代 |
| schema 内置弱口令 admin/123456 | 属产品设计（学习项目演示账号），README 已标注 |
| 真实短信接入后 generateCode 计数顺序调整 | 当前 Mock 实现不触发，接入真实 SMS 时处理 |
| ~~超时取消任务对"活动记录物理消失"极端场景的重试~~ | **已修复**（2026-09-26 冒烟后发现）：`GroupActivityService.increaseStock` 重构为 `restoreStock` 返回布尔值，活动被物理删除时跳过库存恢复并继续取消流程（用户取消/管理员取消/管理员退款三处调用点同步降级），孤儿团购订单不再无限重试。已实测：开发库孤儿订单 22 于 08:58:30 被定时任务成功降级取消 |
| group_order 无"已完成"闭环（soldCount 的 3 为死值） | 无害；重构团购状态机时一并处理 |
| `size:9999` 全量拉取做统计、Unsplash 外链、布局重复代码 | 低优先级，待后续优化 |
| Swagger 生产环境放行 | 建议生产 profile 关闭，随环境分离改造一并处理 |

---

## 九、修改文件清单（本轮合计）

- **后端**：新增 4（`.dockerignore`、`BusinessException`、`ExceptionSupport`、`ImageUploader`）；修改 32（service 10、controller 14、config 4、mapper 2、dto 1、exception 1、Dockerfile、application.yml）；新增测试 3 个文件
- **前端**：新增 5（`utils/markdown.js`、4 个测试文件、`vitest.config.js`）；修改 12（含 `package.json` 增 test script、`pnpm-lock.yaml`、`node_modules` 重建）
- **根目录**：`docker-compose.yml`、`.env.example`、`frontend/nginx.conf`、`frontend/Dockerfile`

> ⚠️ 部署提醒：`pnpm dev` 开发服务器若在依赖重建前启动，需重启；`.env` 中 AI Key 保持有效即可，**`application-local.yml` 的两把 Key 必须轮换**。
