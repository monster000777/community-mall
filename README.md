# 社区团购系统

一个基于前后端分离架构的社区团购系统，提供完整的电商功能和管理后台。

---

## 🎯 项目简介

本项目是一个完整的社区团购系统，包含用户端和管理后台，支持商品浏览、购物车、订单管理、团购活动、以及内置的 **AI 智能导购与文案生成系统**。

### 技术栈

**后端**
- Spring Boot 2.7.14
- MyBatis-Plus 3.5.3.1
- MySQL 8.0
- Sa-Token 1.37.0 (权限认证)
- SpringDoc OpenAPI (Swagger UI)（接口文档）
- Redis 5.0+ (分布式会话与 AI 历史会话持久化)
- **LangChain4j 1.15.1** (LLM 开发框架，兼容 OpenAI 协议，支持全新 Tool Call 与自主 Agent)
- BCrypt (密码加密)
- Maven 3.6+

**前端**
- Vue 3.3.4 (Composition API)
- Vite 4.4.9
- Ant Design Vue 4.0.3
- Axios 1.5.0
- Pinia 2.1.6
- Vue Router 4.2.4
- **marked 18.0.4** (Markdown 富文本解析)

---

## 📦 项目结构

```
community-mall/
├── community-mall-backend/          # 后端项目
│   ├── src/main/java/com/community/mall/
│   │   ├── CommunityMallApplication.java   # 启动类
│   │   ├── common/                   # 公共类
│   │   │   └── Result.java           # 统一响应封装
│   │   ├── config/                   # 配置类
│   │   │   ├── CorsConfig.java       # 跨域配置
│   │   │   ├── AiConfig.java         # AI 服务装配与客户端注入配置
│   │   │   ├── RedisChatMemoryStore.java # 基于 Redis 的分布式 AI 记忆存储
│   │   │   ├── MybatisPlusConfig.java # MyBatis-Plus配置
│   │   │   └── SecurityConfig.java   # Spring Security配置
│   │   ├── controller/               # 控制器
│   │   │   ├── AuthController.java   # 认证控制器
│   │   │   ├── CartController.java   # 购物车控制器
│   │   │   ├── ChatController.java   # 智能导购客服控制器
│   │   │   ├── OrderController.java  # 订单控制器
│   │   │   ├── ProductController.java # 商品控制器
│   │   │   └── admin/                # 管理员控制器
│   │   │       ├── AdminProductController.java
│   │   │       └── AiController.java # 后台 AI 文案自动生成控制器
│   │   ├── dto/                      # 数据传输对象
│   │   ├── entity/                   # 实体类
│   │   ├── exception/                # 异常处理
│   │   ├── mapper/                   # MyBatis Mapper
│   │   ├── service/                  # 业务逻辑层
│   │   │   ├── CustomerAiService.java # 智能导购接口
│   │   │   ├── AiAssistant.java      # 后台文案生成接口
│   │   │   ├── ProductTool.java      # 大模型 Agent 可调用查库工具
│   │   │   └── ProductService.java   # 商品服务
│   │   └── vo/                       # 视图对象
│   ├── src/main/resources/
│   │   ├── application.yml           # 基础配置文件（含端口、AI占位符、数据库映射）
│   │   ├── application-local.yml     # 本地敏感配置（已加入 .gitignore，配置本地 API-KEY）
│   │   ├── banner.txt                # 启动Banner
│   │   └── success-banner.txt        # 成功启动Banner
│   └── pom.xml                       # Maven配置
├── frontend/                         # 前端项目
│   ├── public/                       # 静态资源目录
│   ├── src/
│   │   ├── api/                      # API接口封装
│   │   │   ├── ai.js                 # 智能导购及文案生成接口
│   │   │   └── request.js            # Axios封装
│   │   ├── components/               # 公共组件
│   │   │   ├── AppIcon.vue           # 图标组件
│   │   │   └── AiCustomerService.vue # 智能导购客服悬浮气泡组件（支持 markdown）
│   │   ├── layouts/                  # 布局组件
│   │   ├── router/                   # 路由配置
│   │   ├── stores/                   # Pinia状态管理
│   │   ├── styles/                   # 全局样式
│   │   └── views/                    # 页面组件
│   ├── index.html                    # 前端 HTML 模板
│   ├── package.json                  # 前端依赖配置
│   └── vite.config.js                # Vite 配置文件
└── database/
    └── schema.sql                    # 数据库初始化脚本
```

---

## 🚀 部署全流程与快速开始

### 1. 环境准备

在开始之前，请确保本地已安装并成功启动了以下基础设施：
- **JDK 17+**
- **Maven 3.6+**
- **MySQL 8.0+**
- **Redis 5.0+**
- **Node.js 16+**（及 pnpm/npm 包管理器）

---

### 2. 基础组件启动与配置

#### 2.1 启动 Redis 服务
系统依靠 Redis 来存储 Sa-Token 的用户登录 Session，并用于持久化 AI 智能导购的历史聊天记录（防止服务重启或集群部署时用户历史对话记忆丢失）。

* **Windows 环境启动**：
  在 Redis 解压目录下运行：
  ```bash
  redis-server.exe redis.windows.conf
  ```
  运行 `redis-cli ping` 验证，若返回 `PONG` 即表示成功运行。
* **Linux/Mac 环境启动**：
  ```bash
  sudo systemctl start redis-server   # Linux
  brew services start redis           # Mac (Homebrew)
  ```

#### 2.2 初始化 MySQL 数据库
1. 连接本地 MySQL 服务。
2. 创建名为 `community_mall` 的数据库。
3. 导入数据库初始脚本文件：
   ```bash
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS community_mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
   mysql -u root -p community_mall < database/schema.sql
   ```

---

### 3. AI 智能模块配置 (核心步骤)

后端 AI 模块默认将接口地址与模型名称抽象为了属性占位符。本地部署时，需要进行大模型接口的授权配置。

#### 3.1 方案 A：使用本地配置文件配置（推荐，开发环境首选）
在后端的配置资源目录下手动新建一个本地配置文件：
`community-mall-backend/src/main/resources/application-local.yml`
*(注：该文件已被 `.gitignore` 忽略，安全不会被提交)*。

根据你选用的兼容 OpenAI 协议的大模型服务商（例如 OpenAI、DeepSeek、阿里云通义千问等），在文件中填入真实的配置：

```yaml
# 本地开发私密配置
openai:
  api-key: "你的真实大模型API-KEY"               # 从云服务平台获取的密钥
  base-url: "https://api.deepseek.com"         # 接口地址（如: https://api.openai.com/v1 等）
  model-name: "deepseek-v4-pro"                  # 模型名称（如: gpt-4o 等）
```

#### 3.2 方案 B：使用系统环境变量配置（推荐，生产部署首选）
如果不创建 `application-local.yml` 文件，也可以直接在运行环境的操作系统中设置以下环境变量：
- `OPENAI_API_KEY`：填入你的 API Key。
- `OPENAI_BASE_URL`：填入兼容接口的 Base URL（默认为 `https://api.openai.com/v1`）。
- `OPENAI_MODEL_NAME`：填入对应的模型标识符（默认为 `gpt-4o`）。

---

### 4. 运行后端服务

1. 进入后端项目目录：
   ```bash
   cd community-mall-backend
   ```
2. 使用 Maven 编译并启动 Spring Boot 应用程序：
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   启动成功后，控制台会打印出成功 Banner，接口服务将在 `http://localhost:8080/api` 运行。
3. **验证启动状态**：
   访问 Swagger UI 在线接口文档，如果能打开表示接口连通正常：
   `http://localhost:8080/api/swagger-ui/index.html`

---

### 5. 运行前端服务

1. 进入前端项目目录：
   ```bash
   cd frontend
   ```
2. 安装项目依赖（推荐使用 pnpm，也可以使用 npm/yarn）：
   ```bash
   # 若未安装 pnpm 可执行：npm install -g pnpm
   pnpm install
   ```
3. 启动 Vite 本地开发服务器：
   ```bash
   pnpm dev
   ```
   前端控制台会输出运行端口，默认在 `http://localhost:3000` 启动，直接点击链接即可在浏览器中进入商城系统。

---

## 📝 功能模块

### 用户端功能
- ✅ 用户注册与登录（Sa-Token + Redis）
- ✅ 商品浏览、分类、搜索
- ✅ **智能导购客服“团团”**（右下角悬浮气泡，支持多轮对话记忆、基于本地库存的智能检索、Markdown 富文本渲染及语音播报）
- ✅ 个人中心（个人信息管理、头像上传及系统功能快捷入口）
- ✅ 购物车与地址管理
- ✅ 团购活动购买、拼团下单、订单列表及模拟支付

### 管理后台功能
- ✅ **AI 推广文案自动生成**（添加商品时，可基于商品名及关键词一键生成高吸引力推广文案）
- ✅ 商品管理与上下架控制
- ✅ 团购活动增删改查及订单统计
- ✅ 订单管理与发货状态维护
- ✅ 用户管理及权限配置

---

## 🔐 默认账号

- **管理员账号**：
  - 用户名：`admin`
  - 密码：`123456`
- **测试用户**：可在用户端自行注册。

---

## 📋 常见问题排错

#### 1. AI 聊天接口报错 `MalformedJsonException` 或 `AI服务暂时不可用`？
- **原因**：大模型服务未打通、密钥无效、或者本地网络无法直连大模型 API，导致代理返回了 HTML 拦截网关报错页。
- **解决方法**：
  1. 检查 `application-local.yml` 里的 `api-key`、`base-url` 是否配置正确。
  2. 确认网络可连通所配置的 Base URL，若是国内环境，推荐使用阿里云通义千问兼容接口（`https://dashscope.aliyuncs.com/compatible-mode/v1` 搭配 `qwen-plus`）以获得更好的网络连通表现。

#### 2. 后端提示 `Could not resolve placeholder` 无法启动？
- **原因**：找不到对应的配置参数。
- **解决方法**：检查 `application.yml` 末尾配置，确保包含属性占位符。开发环境确保创建了 `application-local.yml` 文件且配置没有拼写错误。

---

## 📄 版权与许可

> 本项目代码版权归作者 Monster(ZouXiaojie) 所有，仅供个人学习与学术交流使用。  
> 未经作者明确书面授权，禁止转载、搬运、复制、修改或用于任何形式的商业用途。
