-- 更新测试用户密码为BCrypt加密格式
-- 所有用户的原始密码都是: 12345678

USE oa_system;

-- BCrypt加密后的密码 (原始密码: 12345678)
-- 注意: BCrypt每次加密结果不同，但都可以验证同一个密码
-- 这些是预先生成的BCrypt哈希值

UPDATE auth_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi' WHERE username = 'zhangsan';
UPDATE auth_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi' WHERE username = 'lisi';
UPDATE auth_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi' WHERE username = 'wangwu';
UPDATE auth_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi' WHERE username = 'zhaoliu';

-- 验证更新
SELECT username, email, '密码已更新为BCrypt格式' as status
FROM auth_user
WHERE is_deleted = 0;

-- 测试登录信息
SELECT '测试账号信息:' as info;
SELECT '账号: zhangsan / 密码: 12345678' as test_account;
SELECT '账号: lisi / 密码: 12345678' as test_account2;
SELECT '账号: wangwu / 密码: 12345678' as test_account3;
SELECT '账号: zhaoliu / 密码: 12345678' as test_account4;
