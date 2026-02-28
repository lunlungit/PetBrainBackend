#!/bin/bash

# 快速启动脚本

echo "========================================"
echo "  🐾 AIPetBrain 后端启动脚本"
echo "========================================"

# 进入后端目录
cd /Users/lunlundemac/repos-ai/AIPetBrainLun/backend

# 1. 清理旧的编译结果
echo ""
echo "[1/3] 清理旧的编译结果..."
mvn clean -q

# 2. 编译项目
echo "[2/3] 编译项目..."
mvn compile -q
if [ $? -ne 0 ]; then
    echo "❌ 编译失败!"
    echo "运行以下命令查看详细错误:"
    echo "mvn compile"
    exit 1
fi

echo "✓ 编译成功"

# 3. 启动应用
echo "[3/3] 启动应用..."
echo ""
java -jar target/aipetbrain-backend-1.0.0.jar

# 如果 jar 不存在，用 Maven 启动
if [ ! -f target/aipetbrain-backend-1.0.0.jar ]; then
    echo "JAR 文件不存在，使用 Maven 启动..."
    mvn spring-boot:run
fi

