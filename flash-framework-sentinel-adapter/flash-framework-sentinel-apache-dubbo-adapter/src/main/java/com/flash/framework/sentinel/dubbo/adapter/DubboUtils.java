package com.flash.framework.sentinel.dubbo.adapter;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.flash.framework.sentinel.dubbo.adapter.config.DubboConfig;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;

/**
 * @author Eric Zhao
 */
public final class DubboUtils {

    public static final String SENTINEL_DUBBO_APPLICATION_KEY = "dubboApplication";
    public static final String DUBBO_METHOD_ENTRY_KEY = "dubboMethodEntry";
    public static final String DUBBO_INTERFACE_ENTRY_KEY = "dubboInterfaceEntry";

    public static String getApplication(Invocation invocation, String defaultValue) {
        if (invocation == null || invocation.getAttachments() == null) {
            throw new IllegalArgumentException("Bad invocation instance");
        }
        return invocation.getAttachment(SENTINEL_DUBBO_APPLICATION_KEY, defaultValue);
    }

    public static String getResourceName(Invoker<?> invoker, Invocation invocation) {
        return getResourceName(invoker, invocation, false);
    }

    public static String getResourceName(Invoker<?> invoker, Invocation invocation, Boolean useGroupAndVersion) {
        StringBuilder buf = new StringBuilder(64);
        String interfaceResource = useGroupAndVersion ? invoker.getUrl().getColonSeparatedKey() : invoker.getInterface().getName();
        buf.append(interfaceResource)
                .append(":")
                .append(invocation.getMethodName())
                .append("(");
        boolean isFirst = true;
        for (Class<?> clazz : invocation.getParameterTypes()) {
            if (!isFirst) {
                buf.append(",");
            }
            buf.append(clazz.getName());
            isFirst = false;
        }
        buf.append(")");
        return buf.toString();
    }

    public static String getResourceName(Invoker<?> invoker, Invocation invocation, String prefix) {
        if (StringUtil.isNotBlank(prefix)) {
            return new StringBuilder(64)
                    .append(prefix)
                    .append(getResourceName(invoker, invocation, DubboConfig.getDubboInterfaceGroupAndVersionEnabled()))
                    .toString();
        } else {
            return getResourceName(invoker, invocation, DubboConfig.getDubboInterfaceGroupAndVersionEnabled());
        }
    }

    private DubboUtils() {
    }
}