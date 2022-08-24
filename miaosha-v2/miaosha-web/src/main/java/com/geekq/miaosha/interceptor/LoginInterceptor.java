package com.geekq.miaosha.interceptor;

import com.alibaba.fastjson.JSON;
import com.geekq.miaosha.redis.RedisService;
import com.geekq.miaosha.service.MiaoShaUserService;
import com.geekq.miasha.entity.MiaoshaUser;
import com.geekq.miasha.enums.enums.ResultStatus;
import com.geekq.miasha.enums.resultbean.ResultGeekQ;
import com.geekq.miasha.utils.UserContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

import static com.geekq.miasha.enums.enums.ResultStatus.ACCESS_LIMIT_REACHED;
import static com.geekq.miasha.enums.enums.ResultStatus.SESSION_ERROR;


/**
 * @author 宝亮
 */
@Service
public class LoginInterceptor implements HandlerInterceptor {

    private static Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    @Resource
    MiaoShaUserService userService;

    @Resource
    RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler)
            throws Exception {
        /**
         * 获取调用 获取主要方法
         */
        if (handler instanceof HandlerMethod) {
            logger.info("打印拦截方法handler ：{} ", handler);
            HandlerMethod hm = (HandlerMethod) handler;
            MiaoshaUser user = getUser(request, response);

            //将 User 的信息设置到当前的线程
            UserContext.setUser(user);
            RequireLogin accessLimit = hm.getMethodAnnotation(RequireLogin.class);
            if (accessLimit == null) {
                return true;
            }
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();


            //使用 redis 针对某个用户以及特定的接口做限流，因为 redis 的数据有自动过期的功能，这样我们可以限制几秒内用户只能访问某个接口几次
            String key = request.getRequestURI();
            if (needLogin) {
                if (user == null) {
                    render(response, SESSION_ERROR);
                    return false;
                }
                key += "_" + user.getNickname();
            }
            AccessKey ak = AccessKey.withExpire(seconds);
            Integer count = redisService.get(ak, key, Integer.class);
            if (count == null) {
                redisService.set(ak, key, 1);
            } else if (count < maxCount) {
                redisService.incr(ak, key);
            } else {
                render(response, ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }

    /**
     * 该方法将在整个请求结束之后，也就是在 DispatcherServlet 渲染了对应的视图之后执行。此方法主要用来进行资源清理。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.removeUser();
    }

    /**
     * 手动渲染返回结果
     * 因为是在拦截器中做前端返回，所以需要手动操作
     */
    private void render(HttpServletResponse response, ResultStatus cm) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(ResultGeekQ.error(cm));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    /**
     * 获取当前登录用户的信息
     */
    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
        String paramToken = request.getParameter(MiaoShaUserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, MiaoShaUserService.COOKIE_NAME_TOKEN);
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        return userService.getByToken(response, token);
    }

    /**
     * 获取指定的 cookie
     */
    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookiName)) {
                return cookie.getValue();
            }
        }
        return null;
    }

}
