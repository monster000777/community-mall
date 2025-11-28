# 社区团购系统

一个基于前后端分离架构的社区团购系统，提供完整的电商功能和管理后台。

## 🎯 项目简介

本项目是一个完整的社区团购系统，包含用户端和管理后台，支持商品浏览、购物车、订单管理等功能。

### 技术栈

**后端**
- Spring Boot 2.7.14
- MyBatis-Plus 3.5.3.1
- MySQL 8.0
- Sa-Token 1.37.0 (权限认证)
- SpringDoc OpenAPI (Swagger UI)（接口文档）
- Redis (Session存储)
- BCrypt (密码加密)
- Maven

**前端**
- Vue 3.3.4 (Composition API)
- Vite 4.4.9
- Ant Design Vue 4.0.3
- Naive UI (图标组件)
- Axios 1.5.0
- Pinia 2.1.6
- Vue Router 4.2.4

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
│   │   │   ├── JwtProperties.java    # JWT配置
│   │   │   ├── MybatisPlusConfig.java # MyBatis-Plus配置
│   │   │   └── SecurityConfig.java   # Spring Security配置
│   │   ├── controller/               # 控制器
│   │   │   ├── AuthController.java   # 认证控制器
│   │   │   ├── CartController.java   # 购物车控制器
│   │   │   ├── CategoryController.java # 分类控制器
│   │   │   ├── OrderController.java  # 订单控制器
│   │   │   ├── ProductController.java # 商品控制器
│   │   │   └── admin/                # 管理员控制器
│   │   │       └── AdminProductController.java
│   │   ├── dto/                      # 数据传输对象
│   │   ├── entity/                   # 实体类
│   │   ├── exception/                # 异常处理
│   │   ├── mapper/                   # MyBatis Mapper
│   │   ├── service/                  # 业务逻辑层
│   │   ├── util/                     # 工具类
│   │   └── vo/                       # 视图对象
│   ├── src/main/resources/
│   │   ├── application.yml           # 配置文件
│   │   ├── banner.txt                # 启动Banner
│   │   └── success-banner.txt        # 成功启动Banner
│   └── pom.xml                       # Maven配置
├── frontend/                         # 前端项目
│   ├── public/                       # 静态资源目录（如 favicon 等）
│   │   └── favicon.svg               # 网站图标（浏览器标签 icon）
│   ├── src/
│   │   ├── api/                      # API接口封装
│   │   │   ├── address.js            # 地址接口
│   │   │   ├── auth.js               # 认证接口
│   │   │   ├── cart.js               # 购物车接口
│   │   │   ├── category.js           # 分类接口
│   │   │   ├── order.js              # 订单接口
│   │   │   ├── product.js            # 商品接口
│   │   │   ├── profile.js            # 个人中心接口
│   │   │   ├── user.js               # 用户管理接口
│   │   │   └── request.js            # Axios封装
│   │   ├── layouts/                  # 布局组件
│   │   │   ├── MainLayout.vue        # 主布局
│   │   │   └── AdminLayout.vue       # 管理后台布局
│   │   ├── router/                   # 路由配置
│   │   │   └── index.js
│   │   ├── stores/                   # Pinia状态管理
│   │   │   ├── user.js               # 用户状态
│   │   │   └── cart.js               # 购物车状态
│   │   ├── views/                    # 页面组件
│   │   │   ├── Home.vue              # 首页
│   │   │   ├── Login.vue             # 登录页
│   │   │   ├── Register.vue          # 注册页
│   │   │   ├── Products.vue          # 商品列表
│   │   │   ├── ProductDetail.vue     # 商品详情
│   │   │   ├── Cart.vue              # 购物车
│   │   │   ├── Orders.vue            # 订单列表
│   │   │   ├── Profile.vue           # 个人中心
│   │   │   ├── Address.vue           # 地址管理
│   │   │   └── admin/                # 管理后台页面
│   │   │       ├── ProductManage.vue # 商品管理
│   │   │       ├── OrderManage.vue   # 订单管理
│   │   │       └── UserManage.vue    # 用户管理
│   │   ├── App.vue                   # 根组件
│   │   └── main.js                   # 前端入口脚本
│   ├── index.html                    # 前端入口 HTML 模板
│   ├── package.json                  # 前端依赖与脚本配置
│   └── vite.config.js                # Vite 构建与开发服务器配置
└── database/
    └── schema.sql                    # 数据库脚本
```

## 🚀 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+（用于Session存储和缓存）
- Node.js 16+
- npm 或 yarn

### Redis 安装与配置

#### Windows 环境

1. **下载 Redis for Windows**
   - 访问 [Microsoft Archive Redis](https://github.com/microsoftarchive/redis/releases)
   - 下载最新版本的 `.msi` 或 `.zip` 文件（推荐 Redis-x64-5.0.14.1.msi）

2. **安装 Redis**
   ```bash
   # 使用 .msi 安装包会自动配置环境变量和服务
   # 或解压 .zip 后手动运行
   cd redis安装目录
   redis-server.exe redis.windows.conf
   ```

3. **验证 Redis 服务**
   ```bash
   # 打开新的命令行窗口
   redis-cli.exe
   ping
   # 应返回：PONG
   ```

4. **配置 Redis 为 Windows 服务（可选）**
   ```bash
   # 以管理员身份运行命令行
   redis-server.exe --service-install redis.windows.conf
   redis-server.exe --service-start
   
   # 查看服务状态
   redis-server.exe --service-status
   ```

#### Linux/Mac 环境

1. **Ubuntu/Debian 安装**
   ```bash
   sudo apt update
   sudo apt install redis-server
   sudo systemctl start redis-server
   sudo systemctl enable redis-server
   ```

2. **CentOS/RHEL 安装**
   ```bash
   sudo yum install redis
   sudo systemctl start redis
   sudo systemctl enable redis
   ```

3. **macOS 安装（使用 Homebrew）**
   ```bash
   brew install redis
   brew services start redis
   ```

4. **验证 Redis 服务**
   ```bash
   redis-cli ping
   # 应返回：PONG
   ```

#### Redis 配置说明

Redis 默认配置即可满足开发需求，如需修改配置：

```bash
# 编辑 Redis 配置文件
# Windows: redis.windows.conf
# Linux/Mac: /etc/redis/redis.conf 或 /usr/local/etc/redis.conf

# 常用配置项：
bind 127.0.0.1              # 绑定地址（生产环境注意安全）
port 6379                   # 端口号
requirepass your_password   # 设置密码（可选）
maxmemory 256mb            # 最大内存限制
maxmemory-policy allkeys-lru  # 内存淘汰策略
```

> **注意**：如果设置了 Redis 密码，需要在后端配置文件中同步修改。

### 数据库配置

#### 1. MySQL 数据库初始化

创建数据库并导入SQL脚本：

```bash
mysql -u root -p < database/schema.sql
```

#### 2. 配置后端连接

修改后端配置文件 `community-mall-backend/src/main/resources/application.yml`：

```yaml
server:
  port: 8080                 # 后端服务端口
  servlet:
    context-path: /api       # 应用上下文路径，所有接口前缀为 /api

spring:
  application:
    name: community-mall-backend  # 应用名称
  
  # MySQL 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/community_mall?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 你的MySQL密码    # 请修改为实际密码
  
  # Jackson JSON 序列化配置
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss  # 日期格式化
    time-zone: GMT+8                   # 时区设置
  
  # Redis 配置
  redis:
    host: localhost          # Redis 服务器地址
    port: 6379              # Redis 端口
    password:               # Redis 密码（如果设置了密码请填写）
    database: 0             # 使用的数据库编号（0-15）
    timeout: 10s            # 连接超时时间
    lettuce:
      pool:
        max-active: 8       # 连接池最大连接数
        max-wait: -1ms      # 连接池最大阻塞等待时间（-1 表示没有限制）
        max-idle: 8         # 连接池最大空闲连接数
        min-idle: 0         # 连接池最小空闲连接数

# MyBatis-Plus 配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true  # 开启驼峰命名转换（数据库字段 user_name -> Java 属性 userName）
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # SQL 日志输出（开发环境）
  global-config:
    db-config:
      id-type: auto                      # 主键类型：数据库自增
      logic-delete-field: deletedFlag    # 逻辑删除字段名
      logic-delete-value: 1              # 逻辑删除值（已删除）
      logic-not-delete-value: 0          # 逻辑未删除值（未删除）
  mapper-locations: classpath*:/mapper/**/*.xml  # Mapper XML 文件位置

# Sa-Token 配置
sa-token:
  token-name: satoken        # Token 名称（同时也是 cookie 名称）
  timeout: 86400             # Token 有效期（单位：秒，86400秒 = 1天，-1 代表永久有效）
  active-timeout: -1         # Token 最低活跃频率（单位：秒），超过此时间没有访问会被冻结，-1 表示不限制
  is-concurrent: true        # 是否允许同一账号多地同时登录（true 允许，false 新登录挤掉旧登录）
  is-share: false           # 是否共用一个 token（true 共用，false 每次登录新建）
  token-style: uuid         # Token 风格（可选：uuid、simple-uuid、random-32、random-64、random-128、tik）
  is-log: true              # 是否输出操作日志

# 日志配置
logging:
  level:
    com.community.mall: debug           # 应用日志级别
    com.baomidou.mybatisplus: debug     # MyBatis-Plus 日志级别
```

### 启动项目

#### 1. 确保服务运行

在启动项目前，请确保以下服务正常运行：

```bash
# 检查 MySQL 服务
mysql -u root -p -e "SELECT VERSION();"

# 检查 Redis 服务
redis-cli ping
# 应返回：PONG
```

#### 2. 启动后端

```bash
cd community-mall-backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080/api` 启动

> **启动检查**：
> - 查看控制台是否有错误信息
> - 确认 Redis 连接成功的日志
> - 访问 Swagger 文档：`http://localhost:8080/api/swagger-ui/index.html`

#### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端应用将在 `http://localhost:3000` 启动

### 常见问题排查

#### Redis 连接失败

如果后端启动时出现 Redis 连接错误：

1. **检查 Redis 服务是否启动**
   ```bash
   # Windows
   netstat -ano | findstr :6379
   
   # Linux/Mac
   ps aux | grep redis
   netstat -nltp | grep 6379
   ```

2. **检查 Redis 连接配置**
   - 确认 `application.yml` 中的 Redis host、port 配置正确
   - 如果设置了密码，确认密码配置正确

3. **测试 Redis 连接**
   ```bash
   redis-cli -h localhost -p 6379
   # 如果设置了密码
   redis-cli -h localhost -p 6379 -a your_password
   ```

#### MySQL 连接失败

如果出现 MySQL 连接错误：

1. 确认 MySQL 服务正在运行
2. 确认数据库 `community_mall` 已创建
3. 检查用户名和密码是否正确
4. 确认 MySQL 时区设置正确

#### 端口被占用

如果提示端口被占用：

```bash
# Windows - 查找占用端口的进程
netstat -ano | findstr :8080
taskkill /PID 进程号 /F

# Linux/Mac - 查找并终止进程
lsof -i :8080
kill -9 进程号
```

## 📝 功能模块

### 用户端功能

✅ 用户注册/登录（JWT认证）  
✅ 商品浏览、分类筛选、搜索  
✅ 商品详情查看  
✅ 加入购物车、修改数量、删除商品  
✅ 创建订单、查看订单  
✅ 订单支付（模拟）、取消订单  
✅ 个人中心

### 管理后台功能

✅ 商品管理（已实现）
- 支持商品新增/编辑/删除
- 商品上下架管理
- 商品库存维护
- 主图与分类管理

✅ 订单管理（已实现）
- 查看订单列表
- 查看订单详情
- 查看订单状态、支付信息
- 订单搜索与筛选

✅ 用户管理（已实现）
- 用户列表
- 用户信息查看
- 用户权限与状态管理

✅ 团购活动管理（已实现）
- 创建/编辑团购活动
- 团购库存管理
- 团购订单统计
- 活动状态管理（未开始/进行中/已结束）

⏳ 数据报表（待实现）
- 销售数据统计
- 用户增长数据
- 团购活动数据分析
- 趋势图表展示

## 🗄️ 数据库设计

主要数据表：
- `user` - 用户表
- `role` - 角色表
- `permission` - 权限表
- `category` - 商品分类表
- `product` - 商品表
- `cart` - 购物车表
- `address` - 收货地址表
- `order_master` - 订单主表
- `order_item` - 订单明细表
- `group_activity` - 团购活动表

## 🔐 默认账号

**管理员账号**
- 用户名: admin
- 密码: 123456

**测试用户**
- 可自行注册

## 📡 API接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 商品接口
- `GET /api/products` - 获取商品列表（分页、筛选、搜索）
- `GET /api/products/{id}` - 获取商品详情
- `GET /api/categories` - 获取所有分类

### 购物车接口
- `POST /api/cart/add` - 添加商品到购物车
- `GET /api/cart/list` - 获取购物车列表
- `PUT /api/cart/{cartId}` - 更新购物车商品数量
- `DELETE /api/cart/{cartId}` - 删除购物车商品

### 订单接口
- `POST /api/order/create` - 创建订单
- `GET /api/order/list` - 获取订单列表
- `GET /api/order/{orderId}` - 获取订单详情
- `PUT /api/order/{orderId}/cancel` - 取消订单
- `PUT /api/order/{orderId}/pay` - 支付订单（模拟）

### 管理员接口
- `GET /api/admin/products` - 获取商品列表
- `POST /api/admin/products` - 添加商品
- `PUT /api/admin/products/{id}` - 更新商品
- `DELETE /api/admin/products/{id}` - 删除商品
- `PUT /api/admin/products/{id}/status` - 更新商品状态

### 接口文档（Swagger）

- 后端已集成 SpringDoc OpenAPI（Swagger UI），启动后端服务后可通过以下地址查看在线接口文档：
  - `http://localhost:8080/api/swagger-ui/index.html`

## 🔒 安全机制

- Sa-Token + Redis 认证（已从JWT迁移）
- BCrypt 密码加密
- 路由拦截器权限控制
- 跨域配置（CORS）
- 请求拦截器自动携带Token
- 401自动跳转登录页
- Session会话管理
- 支持强制下线、踢人等高级功能

## 🎨 前端特性

- Vue3 Composition API
- Pinia 状态管理
- Vue Router 路由守卫
- Axios 请求/响应拦截器
- Ant Design Vue 组件库
- 响应式布局设计
- 现代化UI设计，使用真实网络图片
- 流畅的动画效果和交互体验
- 渐变色系，视觉效果出众

## 📋 待优化功能

- [X] 地址管理功能完善
- [X] 用户管理功能实现
- [X] 团购活动模块
- [ ] 数据报表和统计
- [X] 图片上传功能
- [ ] 支付宝/微信支付集成
- [ ] 订单物流跟踪
- [ ] 商品评价系统
- [ ] 优惠券/折扣功能

## 📄 License

> 本项目代码版权归作者 Monster(ZouXiaojie) 所有，仅供个人学习与学术交流使用。  
> 未经作者明确书面授权，禁止转载、搬运、复制、修改或用于任何形式的商业用途或学术不端行为。

## 📧 联系方式

- 作者：Monster(ZouXiaojie)
- 邮箱：1170844693@qq.com

 ---

 **声明**: 未经作者允许禁止抄袭本项目内容。
