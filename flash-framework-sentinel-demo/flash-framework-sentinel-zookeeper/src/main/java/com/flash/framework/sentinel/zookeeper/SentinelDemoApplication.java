package com.flash.framework.sentinel.zookeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CountDownLatch;

/**
 * @author zhurg
 * @date 2020/3/3 - 下午3:13
 */
@SpringBootApplication
public class SentinelDemoApplication {

    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            SpringApplication.run(SentinelDemoApplication.class, args);
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
    }
}