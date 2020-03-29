package com.flash.framework.sentinel.dubbo.adapter;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.flash.framework.sentinel.dubbo.adapter.config.DubboConfig;
import com.flash.framework.sentinel.dubbo.adapter.fallback.DubboFallback;
import com.flash.framework.sentinel.dubbo.adapter.fallback.DubboFallbackRegistry;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Objects;

/**
 * @author zhurg
 * @date 2020/3/28 - 上午10:55
 */
public abstract class BaseSentinelDubboFilter implements Filter, Filter.Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseSentinelDubboFilter.class);

    @Override
    public void onMessage(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        onSuccess(appResponse, invoker, invocation);
    }

    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
        SentinelResource sentinelResource = getSentinelResource(invoker, invocation);
        traceAndExit(t, invoker.getUrl(), sentinelResource);
    }

    private void onSuccess(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        SentinelResource sentinelResource = getSentinelResource(invoker, invocation);
        if (Objects.nonNull(sentinelResource)) {
            if (DubboConfig.getDubboBizExceptionTraceEnabled()) {
                traceAndExit(appResponse.getException(), invoker.getUrl(), sentinelResource);
            } else {
                traceAndExit(null, invoker.getUrl(), sentinelResource);
            }
        }
    }

    private void traceAndExit(Throwable throwable, URL url, SentinelResource sentinelResource) {
        Entry interfaceEntry = (Entry) RpcContext.getContext().get(DubboUtils.DUBBO_INTERFACE_ENTRY_KEY);
        Entry methodEntry = (Entry) RpcContext.getContext().get(DubboUtils.DUBBO_METHOD_ENTRY_KEY);
        if (methodEntry != null && trace(throwable, sentinelResource)) {
            Tracer.traceEntry(throwable, methodEntry);
            methodEntry.exit();
            RpcContext.getContext().remove(DubboUtils.DUBBO_METHOD_ENTRY_KEY);
        }
        if (interfaceEntry != null && trace(throwable, sentinelResource)) {
            Tracer.traceEntry(throwable, interfaceEntry);
            interfaceEntry.exit();
            RpcContext.getContext().remove(DubboUtils.DUBBO_INTERFACE_ENTRY_KEY);
        }
        if (CommonConstants.PROVIDER_SIDE.equals(url.getParameter(CommonConstants.SIDE_KEY))) {
            ContextUtil.exit();
        }
    }

    protected SentinelResource getSentinelResource(Invoker<?> invoker, Invocation invocation) {
        Method method = ReflectionUtils.findMethod(invoker.getInterface(), invocation.getMethodName(), invocation.getParameterTypes());
        return AnnotationUtils.findAnnotation(method, SentinelResource.class);
    }

    private boolean trace(Throwable throwable, SentinelResource sentinelResource) {
        if (Objects.isNull(sentinelResource)) {
            return false;
        }
        if (Objects.nonNull(sentinelResource.exceptionsToTrace()) && Objects.nonNull(throwable)) {
            for (Class<? extends Throwable> clazz : sentinelResource.exceptionsToTrace()) {
                if (clazz.equals(throwable.getClass())) {
                    return true;
                }
            }
        }
        if (Objects.nonNull(sentinelResource.exceptionsToIgnore()) && Objects.nonNull(throwable)) {
            for (Class<? extends Throwable> clazz : sentinelResource.exceptionsToIgnore()) {
                if (clazz.equals(throwable.getClass())) {
                    return false;
                }
            }
        }
        return true;
    }

    protected Result blockHandle(Invoker<?> invoker, Invocation invocation, BlockException ex, SentinelResource sentinelResource, DubboFallback defaultDubboFallback) {
        if (Objects.nonNull(sentinelResource)) {
            if (!StringUtils.isEmpty(sentinelResource.blockHandler())) {
                try {
                    doBlock(Class.forName(sentinelResource.blockHandler()), invoker, invocation, ex, sentinelResource, defaultDubboFallback);
                } catch (Exception e) {
                    LOGGER.warn(MessageFormat.format("[Sentinel] can not fund blockHandler {0} for resource {1}", sentinelResource.blockHandler(), sentinelResource.value()), e);
                    return defaultDubboFallback.handle(invoker, invocation, ex);
                }
            }
            if (Objects.nonNull(sentinelResource.blockHandlerClass())) {
                try {
                    doBlock(sentinelResource.blockHandlerClass()[0].newInstance(), invoker, invocation, ex, sentinelResource, defaultDubboFallback);
                } catch (Exception e) {
                    LOGGER.warn(MessageFormat.format("[Sentinel] can not instance blockHandler {0} for resource {1}", sentinelResource.blockHandler(), sentinelResource.value()), e);
                    return defaultDubboFallback.handle(invoker, invocation, ex);
                }
            }
        }
        return DubboFallbackRegistry.getProviderFallback().handle(invoker, invocation, ex);
    }

    private Result doBlock(Object blockHandler, Invoker<?> invoker, Invocation invocation, BlockException ex, SentinelResource sentinelResource, DubboFallback defaultDubboFallback) {
        if (blockHandler instanceof DubboFallback) {
            DubboFallback fallback = (DubboFallback) blockHandler;
            return fallback.handle(invoker, invocation, ex);
        } else {
            LOGGER.warn(MessageFormat.format("[Sentinel] blockHandler {0} for resource {1} not implements DubboFallback", sentinelResource.blockHandler(), sentinelResource.value()), ex);
            return defaultDubboFallback.handle(invoker, invocation, ex);
        }
    }
}
