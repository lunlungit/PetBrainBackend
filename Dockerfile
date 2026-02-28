# 多阶段构建：第一阶段编译
FROM openjdk:17-jdk-slim as builder

WORKDIR /build

# 复制整个 backend 目录
COPY . .

# 进入 backend 目录并编译项目
RUN cd backend && apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests

# 多阶段构建：第二阶段运行
FROM openjdk:17-jdk-slim

WORKDIR /app

# 从第一阶段复制编译好的 JAR 文件
COPY --from=builder /build/backend/target/aipetbrain-backend-1.0.0.jar ./app.jar

# 创建上传文件夹
RUN mkdir -p /app/uploads

# 暴露端口
EXPOSE 8080

# 设置时区
ENV TZ=Asia/Shanghai

# 启动应用（使用生产环境配置）
CMD ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]

