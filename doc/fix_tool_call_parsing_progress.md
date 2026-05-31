# AI 模块架构升级 — LangChain4j 版本升级

## 问题根因

AI 导购的 Function Calling 不生效，根因是 **LangChain4j 版本过旧**：
- 0.24.0 使用旧版 OpenAI `functions` 参数格式
- MiMo 等新模型只支持新版 `tools` 参数格式
- 模型收不到工具定义 → 在文本里乱输出

## 解决方案

升级 Java 8 → 17，LangChain4j 0.24.0 → 1.15.1。

## 修改清单

| 文件 | 改动 |
|------|------|
| `pom.xml` | Java 1.8→17，新增 `langchain4j.version` 属性，版本 1.15.1 |
| `AiConfig.java` | `ChatLanguageModel`→`ChatModel`，`.chatLanguageModel()`→`.chatModel()` |
| `AiController.java` | `ChatLanguageModel`→`ChatModel` |
| `RedisChatMemoryStore.java` | `getMessages()` 增加 try-catch 容错，旧格式数据自动清除 |
| `ChatController.java` | 回退到最简洁版本（55行），零兜底逻辑 |
| `CustomerAiService.java` | 恢复 Function Calling 工具调用指令 |
| `ProductTool.java` | 恢复 `@Tool` 注解，新增 `@P` 参数描述 |

## 删除的文件

- `IntentAnalysisService.java`（确定性架构废弃）
- `IntentResult.java`（确定性架构废弃）

## 验证结果

- `mvn clean compile` → BUILD SUCCESS ✅
- 实测 "可乐有没有" → Function Calling 自动调用 `searchProducts` → 返回真实价格 ✅

---

## 现在的 AI 模块实现链路

当前 AI 模块分为两个核心功能链路：**智能导购（多轮对话+工具调用）** 与 **推广文案生成（单次生成）**。

### 1. 智能导购链路（Function Calling + 记忆持久化）

```mermaid
sequenceDiagram
    autonumber
    actor User as 用户
    participant FE as 前端 (AiCustomerService.vue)
    participant Ctrl as 后端控制器 (ChatController)
    participant AISvc as LangChain4j AI代理 (CustomerAiService)
    participant Redis as 会话存储 (RedisChatMemoryStore)
    participant LLM as 大语言模型 (GPT-4o-mini)
    participant Tool as 本地商品工具 (ProductTool)
    participant DB as 数据库 (MySQL)

    User->>FE: 输入“有草莓吗？多少钱？”
    FE->>Ctrl: 发送 POST /api/chat/ask (question, sessionId)
    Ctrl->>AISvc: 调用 chat(sessionId, question)
    
    Note over AISvc,Redis: [步骤1：加载历史记忆]
    AISvc->>Redis: getMessages(sessionId)
    Redis-->>AISvc: 返回当前用户的多轮聊天历史记录

    Note over AISvc,LLM: [步骤2：大模型智能判断]
    AISvc->>LLM: 发送 (历史聊天+本次提问+角色Prompt+ProductTool描述)
    
    alt 需要调用工具
        LLM-->>AISvc: 返回 tool_calls 结构 (方法: searchProducts, 参数: {keyword:"草莓"})
        Note over AISvc,Tool: [步骤3：本地工具执行]
        AISvc->>Tool: 反射调用 searchProducts("草莓")
        Tool->>DB: MyBatis查询 (商品名含“草莓”且上架)
        DB-->>Tool: 返回草莓的真实库存价格
        Tool-->>AISvc: 返回字符串 ("草莓（价格：25.00元）")
        
        Note over AISvc,LLM: [步骤4：二次请求大模型]
        AISvc->>LLM: 发送 (历史聊天+原问题+工具执行结果)
        LLM-->>AISvc: 返回润色后的最终中文导购话术 ("咱家草莓有货哦，现在只要25元...")
    else 无需调用工具 (如闲聊)
        LLM-->>AISvc: 直接返回普通回答 ("你好呀，我是团团...")
    end

    Note over AISvc,Redis: [步骤5：持久化记忆]
    AISvc->>Redis: updateMessages(sessionId, 新消息历史)
    
    AISvc-->>Ctrl: 返回最终文本回答
    Ctrl-->>FE: 返回封装结果 Result.success(answer)
    FE->>User: 渲染聊天气泡
```

### 2. 商品推广文案生成链路（单次文本生成）

1. **前端触发**：商家在商品管理界面（`ProductManage.vue`）填写商品名称与卖点关键词，点击“AI生成文案”。
2. **后端接收**：`AiController` 拦截 `/api/admin/ai/generate` 请求，提取参数。
3. **无记忆调用**：`AiController` 调用 `AiAssistant.generateProductCopy(prompt)`。
4. **单次模型交互**：`AiAssistant`（LangChain4j 动态代理）直接将带有格式限定 Prompt 的请求发送给 `ChatModel`，大模型返回精简文案（50字内）。
5. **前端填充**：前端收到成功响应后，将文案自动填充并覆盖到“商品描述”文本框中。

### 3. README.md 说明更新
同步更新了项目根目录下的 [README.md](file:///e:/community-mall/community-mall-master/README.md)：
* 后端 JDK 版本依赖要求从 `1.8+` 提升至 `17+`。
* LangChain4j 的版本说明从 `0.24.0` 升级为 `1.15.1`，并新增说明“全新工具调用及自主 Agent 决策”。


