package com.flash.framework.sentinel.dubbo.adapter.config;

/**
 * @author
 * @date 2020/3/28 - 上午10:52
 */

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * <p>
 * Responsible for dubbo service provider, consumer attribute configuration
 * </p>
 *
 * @author lianglin
 * @since 1.7.0
 */
public final class DubboConfig {

    public static final String DUBBO_USE_PREFIX = "csp.sentinel.dubbo.resource.use.prefix";
    private static final String TRUE_STR = "true";

    public static final String DUBBO_PROVIDER_PREFIX = "csp.sentinel.dubbo.resource.provider.prefix";
    public static final String DUBBO_CONSUMER_PREFIX = "csp.sentinel.dubbo.resource.consumer.prefix";

    private static final String DEFAULT_DUBBO_PROVIDER_PREFIX = "dubbo:provider:";
    private static final String DEFAULT_DUBBO_CONSUMER_PREFIX = "dubbo:consumer:";

    public static final String DUBBO_INTERFACE_GROUP_VERSION_ENABLED = "csp.sentinel.dubbo.interface.group.version.enabled";

    public static final String TRACE_BIZ_EXCEPTION_ENABLED = "csp.sentinel.dubbo.trace.biz.exception.enabled";


    public static boolean isUsePrefix() {
        return TRUE_STR.equalsIgnoreCase(SentinelConfig.getConfig(DUBBO_USE_PREFIX));
    }

    public static String getDubboProviderPrefix() {
        if (isUsePrefix()) {
            String config = SentinelConfig.getConfig(DUBBO_PROVIDER_PREFIX);
            return StringUtil.isNotBlank(config) ? config : DEFAULT_DUBBO_PROVIDER_PREFIX;
        }
        return null;
    }

    public static String getDubboConsumerPrefix() {
        if (isUsePrefix()) {
            String config = SentinelConfig.getConfig(DUBBO_CONSUMER_PREFIX);
            return StringUtil.isNotBlank(config) ? config : DEFAULT_DUBBO_CONSUMER_PREFIX;
        }
        return null;
    }

    public static Boolean getDubboInterfaceGroupAndVersionEnabled() {
        return TRUE_STR.equalsIgnoreCase(SentinelConfig.getConfig(DUBBO_INTERFACE_GROUP_VERSION_ENABLED));
    }

    public static Boolean getDubboBizExceptionTraceEnabled() {
        String traceBizExceptionEnabled = SentinelConfig.getConfig(TRACE_BIZ_EXCEPTION_ENABLED);
        if (StringUtil.isNotBlank(traceBizExceptionEnabled)) {
            return TRUE_STR.equalsIgnoreCase(traceBizExceptionEnabled);
        }
        return true;
    }

}