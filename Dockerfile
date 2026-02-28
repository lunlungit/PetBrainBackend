# 多阶段构建：第一阶段编译
FROM maven:3.8-openjdk-17 as builder

WORKDIR /app

# 复制 pom.xml 和 src
COPY pom.xml .
COPY src ./src

# 编译项目
RUN mvn -DskipTests -q clean package

# 多阶段构建：第二阶段运行
FROM openjdk:17-jdk-slim

WORKDIR /app

# 从第一阶段复制编译好的 JAR 文件
COPY --from=builder /app/target/aipetbrain-backend-1.0.0.jar ./app.jar

# 创建上传文件夹
RUN mkdir -p /app/uploads

# 暴露端口
EXPOSE 8080

# 设置时区
ENV TZ=Asia/Shanghai

# 启动应用（使用生产环境配置）
CMD ["java", "-Dspring.profiles.active=prod", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]

