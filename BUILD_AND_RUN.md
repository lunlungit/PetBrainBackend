# 后端运行环境配置指南

## 环境要求

- **Java**: 17+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Redis**: 6.0+ (可选)

## 安装 Java 17

### macOS (使用 Homebrew)

```bash
# 安装 Java 17
brew install openjdk@17

# 设置 Java 17 为默认版本
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk

# 设置环境变量（添加到 ~/.zshrc 或 ~/.bash_profile）
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH=$JAVA_HOME/bin:$PATH

# 验证安装
java -version
```

### macOS (手动安装)

如果 Homebrew 无法使用，可以从官网下载：
https://adoptium.net/zh-CN/temurin/releases/?version=17

## 安装 Maven

### macOS (使用 Homebrew)

```bash
brew install maven

# 验证安装
mvn --version
```

### 手动安装

```bash
# 下载 Maven
wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz

# 解压
tar -xzf apache-maven-3.9.6-bin.tar.gz
mv apache-maven-3.9.6 ~/maven

# 设置环境变量
export MAVEN_HOME=~/maven
export PATH=$MAVEN_HOME/bin:$PATH
```

## 数据库配置

数据库配置已更新到 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://10.233.211.23:5002/grocery_configuration?characterEncoding=UTF8&socketTimeout=60000&allowMultiQueries=true&connectTimeout=5000&useSSL=false&serverTimezone=GMT%2B8
    username: rds_pet
    password: m5iii6ueoF(2WL
```

**注意：请确保已在数据库中执行建表脚本 `src/main/resources/sql/init.sql`**

## 编译和运行

### 1. 编译项目

```bash
cd backend
mvn clean package -DskipTests
```

### 2. 运行项目

#### 方式一：使用 Maven 插件运行

```bash
cd backend
mvn spring-boot:run
```

#### 方式二：使用 JAR 包运行

```bash
cd backend
mvn clean package -DskipTests
java -jar target/aipetbrain-backend-1.0.0.jar
```

#### 方式三：在 IDE 中运行

直接运行 `AIPetBrainApplication.java` 的 `main` 方法

## 验证服务启动

启动成功后，会看到以下输出：

```
=======================================
  🐾 AIPetBrain 启动成功！
  🚀 服务器运行在: http://localhost:8080/api
  📱 宠物社区微信小程序后端服务已就绪
=======================================
```

测试接口：

```bash
curl http://localhost:8080/api
```

## 常见问题

### 1. Java 版本不对

```bash
# 查看所有 Java 版本
/usr/libexec/java_home -V

# 设置 Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### 2. Maven 找不到依赖

```bash
# 清理并重新下载依赖
mvn clean install -U
```

### 3. 数据库连接失败

- 确认数据库地址和端口正确
- 确认用户名和密码正确
- 确认网络可访问数据库
- 确认已执行建表脚本

### 4. 端口被占用

```bash
# 查看占用 8080 端口的进程
lsof -i :8080

# 杀掉进程
kill -9 <PID>
```

## 日志配置

日志级别可在 `application.yml` 中修改：

```yaml
logging:
  level:
    com.aipetbrain: debug
```

## 生产环境配置

生产环境使用 `application-prod.yml`，可通过以下参数指定：

```bash
java -jar target/aipetbrain-backend-1.0.0.jar --spring.profiles.active=prod

