package com.geekq.miaosha.config;

import com.geekq.miasha.entity.MiaoshaUser;
import com.geekq.miasha.utils.UserContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;


/**
 * 自定义参数解析器
 * @author 宝亮
 */
@Configuration
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    /**
     * 此方法用于告诉解析器当前参数类型是否目标类型
     */
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        Class<?> clazz = methodParameter.getParameterType();
        return clazz == MiaoshaUser.class;
    }

    /**
     * 填充参数数据的方法
     */
    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest webRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        return UserContext.getUser();
    }

}
