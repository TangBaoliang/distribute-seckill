package com.geekq.miaosha.rabbitmq;

import com.geekq.api.entity.GoodsVoOrder;
import com.geekq.api.utils.AbstractResultOrder;
import com.geekq.api.utils.ResultGeekQOrder;
import com.geekq.miaosha.redis.RedisService;
import com.geekq.miaosha.service.GoodsService;
import com.geekq.miaosha.service.MiaoshaService;
import com.geekq.miaosha.service.OrderService;
import com.geekq.miasha.entity.MiaoshaOrder;
import com.geekq.miasha.entity.MiaoshaUser;
import com.geekq.miasha.entity.OrderInfo;
import com.geekq.miasha.enums.enums.ResultStatus;
import com.geekq.miasha.enums.resultbean.ResultGeekQ;
import com.geekq.miasha.exception.GlobleException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;

@Service
@Slf4j
public class MQReceiver {

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Resource
    private MQSender mqSender;

    @Autowired
    private com.geekq.api.service.GoodsService goodsServiceRpc;

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receive(String message) {
        log.info("receive message:" + message);
        MiaoshaMessage mm = RedisService.stringToBean(message, MiaoshaMessage.class);
        MiaoshaUser user = mm.getUser();
        long goodsId = mm.getGoodsId();

        //风控校验，校验是否能够
        ResultGeekQOrder<GoodsVoOrder> goodsVoOrderResultGeekQOrder = goodsServiceRpc.getGoodsVoByGoodsId(goodsId);
        if (!AbstractResultOrder.isSuccess(goodsVoOrderResultGeekQOrder)) {
            throw new GlobleException(ResultStatus.SESSION_ERROR);
        }

        GoodsVoOrder goods = goodsVoOrderResultGeekQOrder.getData();
        int stock = goods.getStockCount();
        if (stock <= 0) {
            return;
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(Long.valueOf(user.getId()), goodsId);
        if (order != null) {
            return;
        }
        //减库存 下订单 写入秒杀订单
        try {
            OrderInfo miaosha = miaoshaService.miaosha(user, goods);
            //向消息队列广播下单成功的消息
            mqSender.sendOrderSuccessMsg(new OrderSuccessMsg(miaosha.getId(), user.getId()));
        }catch (Exception e) {
            return;
        }

    }

    @RabbitListener(queues = MQConfig.FANOUT_EXCHANGE)
    public void sendSuccessToClient(String message) throws InterruptedException {
        OrderSuccessMsg mm = RedisService.stringToBean(message, OrderSuccessMsg.class);
        SseEmitter sseEmitter = SseEndPoint.sseCache.get(String.valueOf(mm.getUserId()));
        log.info("下单成功, 用户不在线");
        if (sseEmitter != null) {
            try {
                sseEmitter.send(mm);
            } catch (IOException e) {
                log.error("向客户端发送成功消息出错");
                e.printStackTrace();
            }
            sseEmitter.complete();
            SseEndPoint.sseCache.remove(String.valueOf(mm.getUserId()));
        }

    }
}
