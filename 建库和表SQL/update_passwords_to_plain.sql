-- 更新所有用户密码为明文格式
-- 密码统一设置为: 123456

USE oa_system;

-- 更新所有用户密码为明文
UPDATE auth_user SET password = '123456' WHERE is_deleted = 0;

-- 验证更新结果
SELECT username, password, '密码已更新为明文: 123456' as status
FROM auth_user
WHERE is_deleted = 0;

SELECT '========================================' as info;
SELECT '测试账号信息:' as info;
SELECT '账号: zhangsan  密码: 123456' as test_account1;
SELECT '账号: lisi      密码: 123456' as test_account2;
SELECT '账号: wangwu    密码: 123456' as test_account3;
SELECT '账号: zhaoliu   密码: 123456' as test_account4;
SELECT '========================================' as info;
