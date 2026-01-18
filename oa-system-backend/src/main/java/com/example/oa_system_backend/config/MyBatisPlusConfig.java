package com.example.oa_system_backend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.oa_system_backend.module.*.mapper")
public class MyBatisPlusConfig {

    /**
     * MyBatis Plus 拦截器配置
     * 添加分页插件和乐观锁插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 添加分页拦截器
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置请求的页面大于最大页后的操作
        // true：返回首页；false：继续请求
        paginationInterceptor.setOverflow(false);
        // 设置单页分页条数限制（可选，默认无限制）
        paginationInterceptor.setMaxLimit(10000L);

        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }
}
