package com.flash.framework.sentinel.redis;

import com.flash.framework.sentinel.demo.api.OrderFacade;
import com.flash.framework.sentinel.demo.request.OrderCreateRequest;
import com.google.common.base.Throwables;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author zhurg
 * @date 2020/3/3 - 下午3:38
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SentinelDemoApplication.class)
public class OrderFacadeTest {

    @Reference(version = "1.0.0", retries = 0)
    private OrderFacade orderFacade;

    @Test
    public void execute() {
        for (int i = 1; i <= 5; i++) {
            try {
                System.out.println(orderFacade.createOrder(new OrderCreateRequest()));
            } catch (Exception e) {
                System.out.println(Throwables.getStackTraceAsString(e));
            }
        }
    }
}