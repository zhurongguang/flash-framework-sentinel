package com.flash.framework.sentinel.dubbo.adapter.fallback;

import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * <p>Global fallback registry for Dubbo.</p>
 *
 * <p>
 * Note: Circuit breaking is mainly designed for consumer. The provider should not
 * give fallback result in most circumstances.
 * </p>
 *
 * @author Eric Zhao
 */
public final class DubboFallbackRegistry {

    private static volatile DubboFallback consumerFallback = new DefaultDubboFallback();
    private static volatile DubboFallback providerFallback = new DefaultDubboFallback();

    public static DubboFallback getConsumerFallback() {
        return consumerFallback;
    }

    public static void setConsumerFallback(DubboFallback consumerFallback) {
        AssertUtil.notNull(consumerFallback, "consumerFallback cannot be null");
        DubboFallbackRegistry.consumerFallback = consumerFallback;
    }

    public static DubboFallback getProviderFallback() {
        return providerFallback;
    }

    public static void setProviderFallback(DubboFallback providerFallback) {
        AssertUtil.notNull(providerFallback, "providerFallback cannot be null");
        DubboFallbackRegistry.providerFallback = providerFallback;
    }

    private DubboFallbackRegistry() {
    }
}