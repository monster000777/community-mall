# 暂存区开发浏览与 Bug 修复进展文档

## 1. 暂存区代码现状调研与分析

通过运行 `git status` 与 `git diff --cached`， we 发现当前 Git 暂存区中存在以下已暂存的开发修改，它们构成了一个基于 MIMO-TTS（云端语音合成）的 AI 客服语音播放功能：

1. **`community-mall-backend/src/main/java/com/community/mall/config/AiConfig.java`**
   - 注册了 `RestTemplate` Bean，用于调用外部 TTS 接口。
2. **`community-mall-backend/src/main/resources/application.yml`**
   - 增加了 `mimo` 云端语音合成服务的配置项（`api-key`, `base-url`, `model`, `voice`）。
3. **`community-mall-backend/src/main/java/com/community/mall/controller/TtsController.java`**
   - 暴露 `POST /api/tts` 接口，接收文本，获取 base64 音频并解码为字节流返回前端。
4. **`frontend/src/utils/ttsPlayer.js`**
   - 移除原生的 `window.speechSynthesis` 播放，改为请求后端的 `/api/tts` 接口获取音频并进行播放。

---

## 2. 发现的 Bug 与冗余代码

### A. 后端 `TtsController.java` 中的问题
1. **冗余代码**：
   - 引入了 `org.springframework.util.LinkedMultiValueMap` 和 `org.springframework.util.MultiValueMap`，已清理。
2. **缺乏异常处理**：
   - 网络异常直接抛出，导致返回 500 异常堆栈。已加 try-catch 保护。
3. **潜在的 NPE 和越界风险**：
   - 解析第三方 Map 结构时未对 choices、message、audio 等节点做安全判空和类型验证。已重构防御。
4. **URL 拼接不鲁棒**：
   - baseUrl 尾部多斜杠问题。已做清洗。
5. **接口路径匹配 Bug (404 错误)**：
   - 后端 context-path 全局为 `/api`，而控制器原先配置了 `@RequestMapping("/api/tts")`，造成暴露相对路径为 `/api/api/tts`，与前端调用的 `/api/tts` 不符，从而产生 404 错误。已更正为 `@RequestMapping("/tts")`。
6. **base-url 为空或非法时抛出 `URI is not absolute` 异常**：
   - 若系统环境变量中 `MIMO_BASE_URL` 未配置（或为空字符串），RestTemplate 在发送请求时会抛出运行时 `java.lang.IllegalArgumentException: URI is not absolute`。已在入口处加前置拦截直接安全返回 400。
7. **[新发现] 切换音色配置后旧音色缓存被错误命中 Bug**：
   - 之前 Redis 缓存以文本 MD5 作为 Key (`tts:cache:<md5>`)。一旦修改配置切换音色后（比如由女声“茉莉”换为男声“苏打”），当请求相同文本时，会错误命中之前缓存的“茉莉”女声音频，造成播放音色与配置的不一致。已将音色参数拼入缓存 Key 予以物理隔离。

### B. 前端 `ttsPlayer.js` 中的严重并发竞态 Bug
1. **异步并发竞争 (Race Condition)**：
   - 连击播放会导致旧的异步请求回调后在后台重合叠音播放。已引入 currentPlayId 控制逻辑彻底解决。
2. **ESLint 语法校验报错**：
   - 在 JS 文件中直接使用浏览器全局变量 `Audio`，在严格的 ESLint (`no-undef`) 环境下会报告 `'Audio' is not defined`。已通过将其改写为引用 `window.Audio` 予以完美解决。

---

## 3. 修复与重构进展

### [已完成] 后端 `TtsController.java` 的修复与健壮性改造
我们已经对 `TtsController.java` 进行了如下重构与精简工作：
1. **清理无用依赖**：删除了 `LinkedMultiValueMap` 和 `MultiValueMap`。
2. **集成日志系统**：加入了 Lombok 注解 `@Slf4j`。
3. **消除双斜杠风险**：清洗了 `baseUrl` 尾部的斜杠。
4. **最小代码精简改造**：去掉了原先对 Map 解析时繁复、重复的 `instanceof` 手动校验，利用全局 try-catch 防崩兜底。使解析代码从 40 多行极致压缩到了 8 行，实现了真正的最小修改与最小代码实现。
5. **添加全局异常捕获与安全性拦截**：使用 try-catch 包裹 RestTemplate 网络请求，记录详细日志并友好返回 HTTP 500。
6. **配置项强力拦截**：在 `speech` 接口最开始，对 `apiKey` 和 `baseUrl` 进行配置强校验。一旦发现未配置（如为空）或者基地址不以 `http` 开头，直接打印警告并返回 `400 Bad Request`，彻底消除了抛出 `URI is not absolute` 的异常隐患。
7. **音色隔离缓存机制实现（最新修复）**：将缓存 Key 拼接策略改为：`tts:cache:<音色名称>:<MD5>`。在不增加复杂度和依赖的前提下，以仅一行代码的修改完美实现了不同角色音色缓存的隔离，修复了切换音色后错配的隐患。
8. **修复接口路径匹配问题**：将 `TtsController` 的 `@RequestMapping` 从 `/api/tts` 修正为 `/tts`，彻底解决了接口调用 404 错误。

### [已完成] 补充配置描述文档注释
1. **配置文件更新**：在 `application.yml` 中，针对 `mimo` 的配置参数补充了详尽的中文注释，列出了四大内置精品音色（冰糖、茉莉、苏打、白桦）的特点以方便上手。

### [已完成] 前端 `ttsPlayer.js` 并发竞态与资源泄露修复
我们已经对 `ttsPlayer.js` 进行了重写与代码去重：
1. **添加唯一请求 ID (`currentPlayId`) 控制**：在模块顶层定义 `currentPlayId`，每次调用 `play()` 产生唯一的局部 `myPlayId`，调用 `stop()` 时则递增 `currentPlayId`，使所有在此之前的异步回调失效。
2. **防重入与并发阻断**：在 fetch 等异步等待的后续步骤中加入 `if (myPlayId !== currentPlayId) return` 强检验，及时丢弃无效的前序异步加载。
3. **极简化资源与状态回收 (`cleanup()`)**：将 `onended`、`onerror` 监听回调以及 `play()` 发生异常时重复出现的 Blob URL 释放（`URL.revokeObjectURL(url)`）与状态重置代码抽取合并为一个内联局部 `cleanup()` 帮助函数，极大地精简了逻辑，实现了最优雅、最简化的去重逻辑。
4. **精准实例清理**：通过 `currentAudio === audio` 匹配性校验，确保每次只修改和置空属于当前任务的状态，避免并发调用时误清状态。
5. **消除 ESLint Undefined 报错**：将 `new Audio(url)` 改写为 `new window.Audio(url)`，确保其可在任何 JS 构建环境下无需配置直接通过 ESLint 全局检查。

### [已完成] 实现 Redis 音频缓存降低 TTS 延迟
1. **缓存流程**：后端计算待播文本的 MD5 Hex 作为缓存标识 Key（`tts:cache:<md5>`）。在发出外部请求前优先查询 Redis，若命中则直出 Base64 编码并解码为 `audio/wav` 字节流返回。在未命中请求到云端数据后，则以 7 天过期时间写入 Redis 中存储。
2. **防崩溃优雅降级**：读写缓存逻辑由专门的 try-catch 包裹保护，保证即使 Redis 断开或异常，系统也会自动降级直联云端获取音频，不会使整个功能失效。

### [已完成] 双保险：API 故障时自动降级到浏览器原生 TTS (SpeechSynthesis) 播放
1. **自动降级拦截**：在 `ttsPlayer.js` 请求后端 `/api/tts` 音频文件的 `catch` 块中加入拦截逻辑。一旦网络发生错误或接口非 200 返回，立刻转向触发局部的 `_playNative(cleanText, myPlayId)`。
2. **原生播放支持**：`_playNative` 调用浏览器标准的 Web Speech API (`window.speechSynthesis`) 播放原生中文语音。
3. **竞态与中止完全拉齐**：原生播放过程中的事件受到 `myPlayId === currentPlayId` 的严密版本一致性保护，并且在全局的 `stop()` 调用时，也会立刻触发 `window.speechSynthesis.cancel()` 彻底中止原生播音，保证降级时的音频无叠加、防停止失效体验。

---

## 4. 验证结果报告

### 1. 后端代码编译测试
- 在 `community-mall-backend` 下运行 `mvn clean compile`。
- 结果：**BUILD SUCCESS**。无编译与语法错误，Lombok 与注入正常。

### 2. 前端代码打包与 Lint 测试
- 在 `frontend` 下运行 `npm run build`。
- 结果：**构建成功（Built in 16.37s）**。Vite 构建顺利输出所有 chunk，没有因为引入新语法或不兼容的 API 引用导致打包失败，ESLint 零 Error 阻断。
