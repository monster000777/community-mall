# ==========================================
# 阶段一: 编译构建
# ==========================================
FROM maven:3.8-openjdk-17-slim AS builder

WORKDIR /build

# 复制 pom.xml
COPY pom.xml .

# 预先下载依赖以缓存（源码变动时无需重新下载全部依赖）
RUN mvn dependency:go-offline -B

# 复制源码并打包
COPY src ./src
RUN mvn clean package -DskipTests=true

# ==========================================
# 阶段二: 运行镜像
# ==========================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 从构建阶段拷贝 JAR 包（通配符写法，pom 版本号变更后无需同步修改）
COPY --from=builder /build/target/*.jar app.jar

# 创建上传文件存放目录
RUN mkdir -p /app/uploads

# 暴露端口
EXPOSE 8080

# 设置默认上传目录为容器内目录
ENV UPLOAD_PATH=/app/uploads

# 启动命令 (显式指定 JVM 以 UTF-8 编码运行，防止 Alpine 容器内中文乱码)
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
