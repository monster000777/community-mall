# ==========================================
# 阶段一: 编译构建
# ==========================================
FROM node:18-alpine AS builder

# 配置国内 npm 源以加速依赖下载
RUN npm config set registry https://registry.npmmirror.com

WORKDIR /build

# 复制依赖定义文件
COPY package.json ./

# 使用 npm 安装依赖，避免 pnpm v10 跨平台 built-dependencies 运行限制
RUN npm install

# 复制项目代码进行构建
COPY . .
RUN npm run build

# ==========================================
# 阶段二: 运行镜像
# ==========================================
FROM nginx:alpine

# 用自定义配置替换 Nginx 默认配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 拷贝第一阶段生成的 dist 静态文件到 Nginx 目录
COPY --from=builder /build/dist /usr/share/nginx/html

# 暴露容器 80 端口
EXPOSE 80

# 运行 Nginx
CMD ["nginx", "-g", "daemon off;"]
