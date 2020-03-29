package com.flash.framework.sentinel.demo.api;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.flash.framework.sentinel.demo.request.OrderCreateRequest;

/**
 * @author zhurg
 * @date 2020/3/3 - 下午2:44
 */
public interface OrderFacade {

    @SentinelResource("createOrder")
    String createOrder(OrderCreateRequest request);
}