#!/bin/bash

# AIPetBrain 后端启动脚本

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}    🐾 AIPetBrain 后端启动脚本${NC}"
echo -e "${GREEN}======================================${NC}"

# 检查 Java 版本
echo -e "${YELLOW}[1/4] 检查 Java 版本...${NC}"
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}错误: 需要 Java 17+，当前版本: $JAVA_VERSION${NC}"
    echo -e "${YELLOW}请运行以下命令安装 Java 17:${NC}"
    echo -e "  brew install openjdk@17"
    exit 1
fi
echo -e "${GREEN}✓ Java 版本检查通过${NC}"

# 检查 Maven
echo -e "${YELLOW}[2/4] 检查 Maven...${NC}"
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: Maven 未安装${NC}"
    echo -e "${YELLOW}请运行以下命令安装 Maven:${NC}"
    echo -e "  brew install maven"
    exit 1
fi
echo -e "${GREEN}✓ Maven 检查通过${NC}"

# 检查端口占用
echo -e "${YELLOW}[3/4] 检查端口 8080...${NC}"
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo -e "${YELLOW}端口 8080 已被占用，尝试关闭...${NC}"
    PID=$(lsof -ti:8080)
    if kill -9 $PID 2>/dev/null; then
        echo -e "${GREEN}✓ 已关闭占用进程 $PID${NC}"
    else
        echo -e "${RED}无法关闭占用进程，请手动处理${NC}"
        exit 1
    fi
fi
echo -e "${GREEN}✓ 端口检查通过${NC}"

# 启动应用
echo -e "${YELLOW}[4/4] 启动 AIPetBrain...${NC}"
echo ""

mvn spring-boot:run

# 如果启动失败，显示帮助信息
if [ $? -ne 0 ]; then
    echo ""
    echo -e "${RED}启动失败！${NC}"
    echo ""
    echo "常见问题："
    echo "1. Java 版本不对 - 请安装 Java 17"
    echo "2. 数据库连接失败 - 检查配置文件 application.yml"
    echo "3. 依赖下载失败 - 运行 'mvn clean install -U' 重试"
    echo ""
    echo "详细配置请参考: BUILD_AND_RUN.md"
fi

