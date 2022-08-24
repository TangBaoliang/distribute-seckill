package com.geekq.miaosha.config;

import com.geekq.miaosha.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * mvc 配置类， 配置拦截器和自定义的参数解析器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    final String[] notLoginInterceptPaths = {"/do_login/**"};
    @Autowired
    UserArgumentResolver resolver;
    @Autowired
    private LoginInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/**").excludePathPatterns(notLoginInterceptPaths);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        //添加一个参数解析器，这样在Controller 的参数列表中能够自动填充当前的用户信息
        argumentResolvers.add(resolver);
    }
}
