package com.flash.framework.sentinel.demo.api.impl;

import com.flash.framework.sentinel.demo.api.OrderFacade;
import com.flash.framework.sentinel.demo.request.OrderCreateRequest;
import org.apache.dubbo.config.annotation.Service;

import java.util.Random;

/**
 * @author zhurg
 * @date 2020/3/3 - 下午2:47
 */
@Service(version = "1.0.0", timeout = 1000, retries = 0)
public class OrderFacadeImpl implements OrderFacade {

    private static final Random RANDOM = new Random();

    @Override
    public String createOrder(OrderCreateRequest request) {
        //模拟失败
        if ((RANDOM.nextInt(500) + 1) % 5 == 0) {
            throw new RuntimeException("模拟异常");
        }
        return request.getOrderNo();
    }
}
