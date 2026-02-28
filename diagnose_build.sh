#!/bin/bash

cd "$(dirname "$0")"

echo "==== Build Diagnostic Report ===="
echo ""

echo "1. Checking Java Version:"
java -version 2>&1 | head -3
echo ""

echo "2. Checking Maven Version:"
mvn -version 2>&1 | head -3
echo ""

echo "3. Checking pom.xml configuration:"
grep -A 5 "<properties>" pom.xml | head -8
echo ""

echo "4. Attempting clean compile:"
echo ""
mvn clean compile -q 2>&1 | tee /tmp/build.log

BUILD_RESULT=$?
ERRORS=$(grep -c "ERROR" /tmp/build.log)
WARNINGS=$(grep -c "WARNING" /tmp/build.log)

echo ""
echo "==== Build Summary ===="
echo "Build Status: $([ $BUILD_RESULT -eq 0 ] && echo 'SUCCESS ✅' || echo 'FAILED ❌')"
echo "Errors Found: $ERRORS"
echo "Warnings Found: $WARNINGS"
echo ""

if [ $BUILD_RESULT -ne 0 ]; then
    echo "==== Build Errors ===="
    grep "ERROR" /tmp/build.log | head -20
fi

exit $BUILD_RESULT

