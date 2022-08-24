package com.geekq.miaosha.rabbitmq;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author TangBaoLiang
 * @date 2022/8/24
 * @email developert163@163.com
 **/
public class SseEndPoint {
    public static Map<String, SseEmitter> sseCache = new ConcurrentHashMap<>();
}
