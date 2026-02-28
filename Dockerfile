# 多阶段构建：第一阶段编译
FROM openjdk:17-jdk-slim as builder

# 设置工作目录
WORKDIR /build

# 更新包管理器并安装 Maven
RUN apt-get update && \
    apt-get install -y --no-install-recommends maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 复制整个项目
COPY . .

# 进入 backend 目录并编译项目
RUN cd backend && mvn -DskipTests -q clean package

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

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD java -version || exit 1

# 启动应用（使用生产环境配置）
CMD ["java", "-Dspring.profiles.active=prod", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]

