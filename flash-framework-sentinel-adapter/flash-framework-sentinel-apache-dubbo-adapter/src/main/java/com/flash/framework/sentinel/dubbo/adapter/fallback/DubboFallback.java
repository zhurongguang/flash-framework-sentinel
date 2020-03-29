package com.flash.framework.sentinel.dubbo.adapter.fallback;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;

/**
 * Fallback handler for Dubbo services.
 *
 * @author Eric Zhao
 */
@FunctionalInterface
public interface DubboFallback {

    /**
     * Handle the block exception and provide fallback result.
     *
     * @param invoker    Dubbo invoker
     * @param invocation Dubbo invocation
     * @param ex         block exception
     * @return fallback result
     */
    Result handle(Invoker<?> invoker, Invocation invocation, Throwable ex);
}
