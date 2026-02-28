#!/bin/bash

# AIPetBrain 数据库初始化脚本

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}  🐾 AIPetBrain 数据库初始化脚本${NC}"
echo -e "${GREEN}======================================${NC}"

# 数据库配置
DB_HOST="10.233.211.23"
DB_PORT="5002"
DB_NAME="grocery_configuration"
DB_USER="rds_pet"
DB_PASS="m5iii6ueoF(2WL"

echo -e "${YELLOW}数据库配置:${NC}"
echo "  主机: $DB_HOST:$DB_PORT"
echo "  数据库: $DB_NAME"
echo "  用户: $DB_USER"
echo ""

# 检查 mysql 命令
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}错误: mysql 客户端未安装${NC}"
    echo -e "${YELLOW}请运行: brew install mysql-client${NC}"
    exit 1
fi

# 执行建表脚本
echo -e "${YELLOW}正在执行建表脚本...${NC}"
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p"$DB_PASS" $DB_NAME < src/main/resources/sql/init.sql

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 数据库初始化成功！${NC}"
    echo ""
    echo "已创建的表："
    echo "  - user_info (用户表)"
    echo "  - pet (宠物表)"
    echo "  - medical_record (医疗记录表)"
    echo "  - vaccination (疫苗记录表)"
    echo "  - territory (领地标记表)"
    echo "  - walk_record (遛狗记录表)"
    echo "  - achievement (勋章成就表)"
    echo "  - expense (账本支出表)"
    echo "  - food_query (食物查询表)"
    echo "  - reminder (提醒表)"
    echo ""
    echo "已插入初始数据："
    echo "  - 23条狗可以吃的食物数据"
    echo "  - 10条猫可以吃的食物数据"
else
    echo -e "${RED}✗ 数据库初始化失败！${NC}"
    echo ""
    echo "可能的原因："
    echo "1. 数据库连接失败 - 检查网络和密码"
    echo "2. 表已存在 - 可以忽略此错误"
    echo "3. 权限不足 - 检查数据库用户权限"
    exit 1
fi

