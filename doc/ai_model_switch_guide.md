# AI 大模型快速切换指南

本文档用于指导开发者在当前社区团购项目中，快速于 **DeepSeek** 与 **阿里云通义千问 (DashScope)** 之间切换 AI 基座模型。

当前系统已通过 `langchain4j-open-ai` 依赖完全兼容了 OpenAI 标准接口协议。因此，不论是接入 DeepSeek 还是通义千问，都**无需修改任何后端 Java 逻辑代码**，只需调整 `application.yml` 和 `application-local.yml` 的配置项即可。

---

## 方案一：使用通义千问 (DashScope)

适用于需要极快响应速度和稳定国内网络环境的场景。

1. **修改 `application.yml` 基础配置**：
   将 `openai` 节点修改如下：
   ```yaml
   openai:
     api-key: ${OPENAI_API_KEY:}
     base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
     model-name: qwen-plus  # 也可选用 qwen-turbo 或 qwen-max
   ```

2. **配置私有 API-KEY**：
   在 `application-local.yml`（已忽略提交）中填入你从阿里云百炼控制台获取的密钥：
   ```yaml
   openai:
     api-key: "sk-xxxxxx..." # 通义千问的真实 key
   ```

---

## 方案二：使用 DeepSeek

适用于需要强逻辑推理或特定语境对话生成的场景。

1. **修改 `application.yml` 基础配置**：
   将 `openai` 节点修改如下：
   ```yaml
   openai:
     api-key: ${OPENAI_API_KEY:}
     base-url: https://api.deepseek.com
     model-name: deepseek-chat  # 大预言模型
   ```

2. **配置私有 API-KEY**：
   在 `application-local.yml` 中填入你的 DeepSeek 开放平台密钥：
   ```yaml
   openai:
     api-key: "sk-xxxxxx..." # DeepSeek的真实 key
   ```

---

## 💡 注意事项与排错指南

> [!WARNING]
> 大原则：**接口地址 (base-url) 必须与模型名称 (model-name) 完全匹配！**
> 例如，如果使用 `https://api.deepseek.com` 作为 base-url，但却填入了 `qwen-plus` 甚至 `glm-5`，请求将会被服务端因为“找不到模型”而直接拒绝，前端将抛出异常。

1. **超时设置**：
   无论是哪个基座模型，在执行如“商品查询并提取上下文对话”这种复杂工作流时，都有可能超过默认的 10 秒时间限制。
   前端的 `askAi` 接口超时已在 `frontend/src/api/ai.js` 放宽至 **60秒**（`timeout: 60000`）。如果将来引入更复杂的 AI 业务，记得保持对应的放大处理。

2. **本地聚合工具 (OneAPI / Grok2API 等)**：
   如果你使用了本地代理工具统管了多个大模型：
   - `base-url` 需配置为本地代理服务的地址（例如 `http://127.0.0.1:3000/v1`）。
   - `api-key` 需配置为你的本地代理令牌。
   - 此模式下，`model-name` 只需填本地代理服务所支持的任何模型名词即可。
