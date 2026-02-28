#!/bin/bash

# 最终验证脚本 - 确保所有修复都已应用

set -e

cd "$(dirname "$0")"

echo "╔════════════════════════════════════════════════════════════════════════════╗"
echo "║                      编译问题修复最终验证                                  ║"
echo "╚════════════════════════════════════════════════════════════════════════════╝"
echo ""

ERRORS=0
WARNINGS=0

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查函数
check_file_exists() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $1 存在"
    else
        echo -e "${RED}✗${NC} $1 不存在"
        ((ERRORS++))
    fi
}

check_contains() {
    if grep -q "$2" "$1" 2>/dev/null; then
        echo -e "${GREEN}✓${NC} $1 包含 '$2'"
    else
        echo -e "${RED}✗${NC} $1 不包含 '$2'"
        ((ERRORS++))
    fi
}

check_not_contains() {
    if ! grep -q "$2" "$1" 2>/dev/null; then
        echo -e "${GREEN}✓${NC} $1 不包含 '$2'"
    else
        echo -e "${YELLOW}⚠${NC} $1 仍然包含 '$2'"
        ((WARNINGS++))
    fi
}

echo "【第1步】检查关键文件存在性"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_file_exists "pom.xml"
check_file_exists "src/main/java/com/aipetbrain/service/PetPermissionService.java"
check_file_exists "src/main/java/com/aipetbrain/service/impl/PetPermissionServiceImpl.java"
check_file_exists "src/main/java/com/aipetbrain/mapper/WeightRecordMapper.java"
check_file_exists "src/test/java/com/aipetbrain/service/PetPermissionServiceTest.java"
check_file_exists "src/test/java/com/aipetbrain/service/WeightRecordServiceTest.java"
echo ""

echo "【第2步】检查 pom.xml 配置"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_contains "pom.xml" "java.version"
check_contains "pom.xml" "maven.compiler.source"
check_contains "pom.xml" "maven-compiler-plugin"
echo ""

echo "【第3步】检查 PetPermissionServiceTest 修复"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_contains "src/test/java/com/aipetbrain/service/PetPermissionServiceTest.java" "setNickname"
check_contains "src/test/java/com/aipetbrain/service/PetPermissionServiceTest.java" "isOwner"
check_contains "src/test/java/com/aipetbrain/service/PetPermissionServiceTest.java" "canView"
check_contains "src/test/java/com/aipetbrain/service/PetPermissionServiceTest.java" "canEdit"
check_not_contains "src/test/java/com/aipetbrain/service/PetPermissionServiceTest.java" "setUsername"
check_not_contains "src/test/java/com/aipetbrain/service/PetPermissionServiceTest.java" "hasWritePermission"
echo ""

echo "【第4步】检查 WeightRecordServiceTest 修复"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_contains "src/test/java/com/aipetbrain/service/WeightRecordServiceTest.java" "addOrUpdateWeightRecord"
check_contains "src/test/java/com/aipetbrain/service/WeightRecordServiceTest.java" "getWeightRecordsByYear"
check_contains "src/test/java/com/aipetbrain/service/WeightRecordServiceTest.java" "getRecentWeightRecords"
echo ""

echo "【第5步】检查 WeightRecordMapper 增强"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_contains "src/main/java/com/aipetbrain/mapper/WeightRecordMapper.java" "selectByPetIdAndYear"
check_contains "src/main/java/com/aipetbrain/mapper/WeightRecordMapper.java" "selectByPetIdAndYearMonth"
check_contains "src/main/java/com/aipetbrain/mapper/WeightRecordMapper.java" "selectRecentByPetId"
check_contains "src/main/java/com/aipetbrain/mapper/WeightRecordMapper.java" "deleteByPetIdAndYear"
echo ""

echo "【第6步】验证 Java 配置"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_contains "src/main/java/com/aipetbrain/service/impl/PetPermissionServiceImpl.java" "jakarta.annotation.Resource"
check_not_contains "src/main/java/com/aipetbrain/service/impl/PetPermissionServiceImpl.java" "javax.annotation.Resource"
echo ""

echo "【第7步】编译验证"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "开始编译测试（这可能需要几分钟）..."
echo ""

if mvn clean compile -q 2>&1 | tee /tmp/compile.log; then
    echo -e "${GREEN}✓${NC} 编译成功"
else
    echo -e "${RED}✗${NC} 编译失败"
    echo ""
    echo "错误信息："
    grep "ERROR" /tmp/compile.log || true
    ((ERRORS++))
fi
echo ""

# 总结
echo "╔════════════════════════════════════════════════════════════════════════════╗"
echo "║                          验证结果总结                                      ║"
echo "╚════════════════════════════════════════════════════════════════════════════╝"
echo ""

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}✓ 所有检查通过！${NC}"
    echo ""
    echo "✅ 编译问题已完全解决"
    echo "✅ 所有修复已正确应用"
    echo "✅ 可以进行下一步操作"
    echo ""
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo -e "${YELLOW}⚠ 有 $WARNINGS 个警告，但没有错误${NC}"
    echo ""
    echo "✅ 基本修复完成"
    echo "⚠️  需要注意上述警告"
    echo ""
    exit 0
else
    echo -e "${RED}✗ 发现 $ERRORS 个错误${NC}"
    echo ""
    echo "❌ 修复可能不完整"
    echo "需要检查上述错误并重新进行修复"
    echo ""
    exit 1
fi

