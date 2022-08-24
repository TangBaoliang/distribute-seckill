package com.geekq.miaosha.controller;

import com.geekq.miaosha.interceptor.RequireLogin;
import com.geekq.miaosha.rabbitmq.SseEndPoint;
import com.geekq.miasha.entity.MiaoshaUser;
import com.geekq.miasha.enums.enums.ResultStatus;
import com.geekq.miasha.enums.resultbean.ResultGeekQ;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author TangBaoLiang
 * @date 2022/8/24
 * @email developert163@163.com
 **/
@Controller
@RequestMapping(path = "/sse")
public class SseController {
//    public static Map<String, SseEmitter> sseCache = new ConcurrentHashMap<>();

    @RequireLogin(seconds = 6, maxCount = 10000)
    @GetMapping(path = "/subscribe")
    public SseEmitter push(MiaoshaUser user) {
        // 超时时间设置为1小时
        if (user == null) {
            throw new RuntimeException("session 数据出错");
        }
        SseEmitter sseEmitter = new SseEmitter(3600_000L);
        SseEndPoint.sseCache.put(String.valueOf(user.getId()), sseEmitter);
        sseEmitter.onTimeout(() -> SseEndPoint.sseCache.remove(String.valueOf(user.getId())));
        sseEmitter.onCompletion(() -> System.out.println("完成！！！"));
//        sseEmitter.onError((rr) -> SseEndPoint.sseCache.remove(String.valueOf(user.getId())));
        return sseEmitter;
    }
}
