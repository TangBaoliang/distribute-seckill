package com.geekq.miaosha.rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author TangBaoLiang
 * @date 2022/8/24
 * @email developert163@163.com
 **/
@Data
@AllArgsConstructor
public class OrderSuccessMsg {
    private long orderId;
    private long userId;
}
