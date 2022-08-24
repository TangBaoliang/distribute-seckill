package com.geekq.miaosha.controller;

import com.geekq.miaosha.redis.KeyPrefix;
import com.geekq.miaosha.redis.RedisService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Controller
@ComponentScan(basePackages = {"com.geekq.miaosha.*"})
public class BaseController {


    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;
    @Autowired
    RedisService redisService;
    //加一个配置项
    @Value("#{'${pageCache.enbale}'}")
    private boolean pageCacheEnable;

    public static void out(HttpServletResponse res, String html) {
        res.setContentType("text/html");
        res.setCharacterEncoding("UTF-8");
        try {
            OutputStream out = res.getOutputStream();
            out.write(html.getBytes("UTF-8"));
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String render(HttpServletRequest request, HttpServletResponse response, Model model, String tplName, KeyPrefix prefix, String key) {
        //没有开启缓存，直接返回试图名字
        if (!pageCacheEnable) {
            return tplName;
        }
        //开启了缓存，从 redis 中取出缓存页面
        String html = redisService.get(prefix, key, String.class);
        if (!StringUtils.isEmpty(html)) {
            out(response, html);
            return null;
        }
        //如果redis 中没有缓存，手动渲染后存入缓存，以便下一次取缓存
        WebContext ctx = new WebContext(request, response,
                request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process(tplName, ctx);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(prefix, key, html);
        }
        out(response, html);
        return null;
    }
}
