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
│   │   └── main.js                   # 入口文件
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
└── database/
    └── schema.sql                    # 数据库脚本
```

## 🚀 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+
- npm 或 yarn

### 数据库配置

1. 创建数据库并导入SQL脚本：

```bash
mysql -u root -p < database/schema.sql
```

2. 修改后端配置文件 `community-mall-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/community_mall?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
```

### 启动后端

```bash
cd community-mall-backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080/api` 启动

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端应用将在 `http://localhost:3000` 启动

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

✅ 商品管理（增删改查、上下架）  
✅ 订单管理（查看订单列表）  
⏳ 用户管理（待实现）  
⏳ 团购活动管理（待实现）  
⏳ 数据报表（待实现）

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
- [ ] 团购活动模块
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
