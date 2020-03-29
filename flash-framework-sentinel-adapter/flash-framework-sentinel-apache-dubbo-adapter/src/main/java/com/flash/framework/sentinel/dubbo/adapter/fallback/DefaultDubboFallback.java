package com.flash.framework.sentinel.dubbo.adapter.fallback;

import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;

/**
 * @author Eric Zhao
 */
public class DefaultDubboFallback implements DubboFallback {

    @Override
    public Result handle(Invoker<?> invoker, Invocation invocation, Throwable ex) {
        // Just wrap and throw the exception.
        throw new SentinelRpcException(ex);
    }
}