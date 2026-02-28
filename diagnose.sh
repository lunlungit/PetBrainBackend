#!/bin/bash

# 诊断脚本 - 检查后端启动问题

echo "========================================"
echo "  🔍 AIPetBrain 后端诊断脚本"
echo "========================================"
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 1. 检查 Java 版本
echo -e "${YELLOW}[1/5] 检查 Java 版本...${NC}"
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_VERSION" -ge 17 ]; then
    echo -e "${GREEN}✓ Java 版本 OK (版本 $JAVA_VERSION)${NC}"
else
    echo -e "${RED}✗ Java 版本不符 (需要 17+，当前: $JAVA_VERSION)${NC}"
fi
echo ""

# 2. 检查 Maven
echo -e "${YELLOW}[2/5] 检查 Maven...${NC}"
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | grep "Maven" | head -1)
    echo -e "${GREEN}✓ Maven 已安装${NC}"
    echo "  $MVN_VERSION"
else
    echo -e "${RED}✗ Maven 未安装${NC}"
fi
echo ""

# 3. 检查端口占用
echo -e "${YELLOW}[3/5] 检查端口 8080...${NC}"
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo -e "${RED}✗ 端口 8080 被占用${NC}"
    echo "  运行以下命令查看占用进程:"
    echo "  lsof -i :8080"
else
    echo -e "${GREEN}✓ 端口 8080 可用${NC}"
fi
echo ""

# 4. 检查数据库连接
echo -e "${YELLOW}[4/5] 检查数据库连接...${NC}"
if command -v mysql &> /dev/null; then
    if mysql -h 10.233.211.23 -P 5002 -u rds_pet -p'm5iii6ueoF(2WL)' -e "SELECT 1" 2>/dev/null; then
        echo -e "${GREEN}✓ 数据库连接 OK${NC}"
    else
        echo -e "${YELLOW}⚠ 数据库连接失败${NC}"
        echo "  检查网络和数据库配置"
    fi
else
    echo -e "${YELLOW}⚠ MySQL 客户端未安装，跳过检查${NC}"
fi
echo ""

# 5. 检查 Redis
echo -e "${YELLOW}[5/5] 检查 Redis 连接...${NC}"
if command -v redis-cli &> /dev/null; then
    if redis-cli -h localhost -p 6379 PING 2>/dev/null | grep -q PONG; then
        echo -e "${GREEN}✓ Redis 连接 OK${NC}"
    else
        echo -e "${YELLOW}⚠ Redis 未运行或连接失败${NC}"
        echo "  运行以下命令启动 Redis:"
        echo "  redis-server"
    fi
else
    echo -e "${YELLOW}⚠ Redis 客户端未安装${NC}"
fi
echo ""

# 6. 尝试编译
echo -e "${YELLOW}[6/6] 编译项目...${NC}"
cd /Users/lunlundemac/repos-ai/AIPetBrainLun/backend
if mvn clean compile -q 2>/dev/null; then
    echo -e "${GREEN}✓ 编译成功${NC}"
else
    echo -e "${RED}✗ 编译失败${NC}"
    echo "  运行以下命令查看详细错误:"
    echo "  mvn clean compile"
fi
echo ""

echo "========================================"
echo "  诊断完成"
echo "========================================"
echo ""
echo "常见解决方案:"
echo "1. 端口被占用: killall java 或 lsof -i :8080 | grep LISTEN | awk '{print \$2}' | xargs kill -9"
echo "2. 数据库连接失败: 检查 application.yml 中的数据库配置"
echo "3. Redis 连接失败: brew install redis && redis-server"
echo "4. 编译失败: mvn clean install -U && mvn package -DskipTests"
echo ""
echo "启动应用:"
echo "  cd /Users/lunlundemac/repos-ai/AIPetBrainLun/backend"
echo "  mvn spring-boot:run"
echo ""

