package com.geekq.miaosha.controller;

import com.geekq.miaosha.redis.RedisService;
import com.geekq.miaosha.service.GoodsService;
import com.geekq.miaosha.service.MiaoShaUserService;
import com.geekq.miaosha.service.OrderService;
import com.geekq.miasha.entity.MiaoshaUser;
import com.geekq.miasha.entity.OrderInfo;
import com.geekq.miasha.enums.resultbean.ResultGeekQ;
import com.geekq.miasha.vo.GoodsVo;
import com.geekq.miasha.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.geekq.miasha.enums.enums.ResultStatus.ORDER_NOT_EXIST;
import static com.geekq.miasha.enums.enums.ResultStatus.SESSION_ERROR;

/**
 * 订单模块
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    MiaoShaUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    /**
     * 返回订单明细
     */
    @RequestMapping("/detail")
    @ResponseBody
    public ResultGeekQ<OrderDetailVo> info(Model model, MiaoshaUser user,
                                           @RequestParam("orderId") long orderId) {
        ResultGeekQ<OrderDetailVo> result = ResultGeekQ.build();

        //未获取到登录信息
        if (user == null) {
            result.withError(SESSION_ERROR.getCode(), SESSION_ERROR.getMessage());
            return result;
        }

        //传过来的订单 id 没有对应的订单
        OrderInfo order = orderService.getOrderById(orderId);
        if (order == null) {
            result.withError(ORDER_NOT_EXIST.getCode(), ORDER_NOT_EXIST.getMessage());
            return result;
        }
        long goodsId = order.getGoodsId();
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        OrderDetailVo vo = new OrderDetailVo();
        vo.setOrder(order);
        vo.setGoods(goods);
        result.setData(vo);
        return result;
    }

}
