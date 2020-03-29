package com.flash.framework.sentinel.dubbo.adapter;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.flash.framework.sentinel.dubbo.adapter.config.DubboConfig;
import com.flash.framework.sentinel.dubbo.adapter.fallback.DubboFallbackRegistry;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.support.RpcUtils;

import java.util.Objects;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;

/**
 * @author zhurg
 * @date 2020/3/28 - 下午2:14
 */
@Activate(group = CONSUMER)
public class SentinelDubboConsumerFilter extends BaseSentinelDubboFilter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Entry interfaceEntry;
        Entry methodEntry;
        RpcContext rpcContext = RpcContext.getContext();
        SentinelResource sentinelResource = getSentinelResource(invoker, invocation);
        try {
            if (Objects.nonNull(sentinelResource)) {
                String methodResourceName = Objects.isNull(sentinelResource.value()) ? DubboUtils.getResourceName(invoker, invocation, DubboConfig.getDubboConsumerPrefix()) : sentinelResource.value();
                String interfaceResourceName = DubboConfig.getDubboInterfaceGroupAndVersionEnabled() ? invoker.getUrl().getColonSeparatedKey()
                        : invoker.getInterface().getName();
                InvokeMode invokeMode = RpcUtils.getInvokeMode(invoker.getUrl(), invocation);

                if (InvokeMode.SYNC == invokeMode) {
                    interfaceEntry = SphU.entry(interfaceResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.OUT);
                    rpcContext.set(DubboUtils.DUBBO_INTERFACE_ENTRY_KEY, interfaceEntry);
                    methodEntry = SphU.entry(methodResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.OUT, invocation.getArguments());
                } else {
                    // should generate the AsyncEntry when the invoke model in future or async
                    interfaceEntry = SphU.asyncEntry(interfaceResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.OUT);
                    rpcContext.set(DubboUtils.DUBBO_INTERFACE_ENTRY_KEY, interfaceEntry);
                    methodEntry = SphU.asyncEntry(methodResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.OUT, 1, invocation.getArguments());
                }
                rpcContext.set(DubboUtils.DUBBO_METHOD_ENTRY_KEY, methodEntry);
            }
            return invoker.invoke(invocation);
        } catch (BlockException e) {
            return blockHandle(invoker, invocation, e, sentinelResource, DubboFallbackRegistry.getConsumerFallback());
        }
    }
}
