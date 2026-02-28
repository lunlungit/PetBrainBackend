#!/bin/bash

# 后端编译脚本

cd "$(dirname "$0")"

echo "========================================="
echo "开始编译后端代码..."
echo "========================================="
echo ""

# 清理并编译
mvn clean compile -U

COMPILE_STATUS=$?

echo ""
echo "========================================="
if [ $COMPILE_STATUS -eq 0 ]; then
    echo "✅ 编译成功"
else
    echo "❌ 编译失败"
fi
echo "========================================="

exit $COMPILE_STATUS

