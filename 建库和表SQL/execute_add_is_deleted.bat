@echo off
echo ========================================
echo OA系统数据库修复脚本
echo ========================================
echo.

REM 设置MySQL连接信息
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_PASS=root
set MYSQL_DB=oa_system

echo [步骤 1/2] 更新测试用户密码为BCrypt格式...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% %MYSQL_DB% < "%~dp0update_test_passwords.sql"

if %errorlevel% equ 0 (
    echo ✓ 密码更新成功
) else (
    echo ✗ 密码更新失败
    echo.
    pause
    exit /b 1
)

echo.
echo [步骤 2/2] 添加 is_deleted 字段到所有表...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% %MYSQL_DB% < "%~dp0add_is_deleted_fields.sql"

if %errorlevel% equ 0 (
    echo ✓ 字段添加成功
) else (
    echo ✗ 字段添加失败（可能已存在）
)

echo.
echo ========================================
echo 数据库修复完成！
echo ========================================
echo.
echo 测试账号信息：
echo   账号: zhangsan  密码: 12345678
echo   账号: lisi      密码: 12345678
echo   账号: wangwu    密码: 12345678
echo   账号: zhaoliu   密码: 12345678
echo.
echo 现在可以启动后端服务测试登录功能了！
echo ========================================
echo.
pause
