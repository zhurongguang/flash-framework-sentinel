package com.flash.framework.sentinel.dubbo.adapter;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.flash.framework.sentinel.dubbo.adapter.config.DubboConfig;
import com.flash.framework.sentinel.dubbo.adapter.fallback.DubboFallbackRegistry;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.Objects;

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

/**
 * @author zhurg
 * @date 2020/3/28 - 上午11:03
 */
@Activate(group = PROVIDER)
public class SentinelDubboProviderFilter extends BaseSentinelDubboFilter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // Get origin caller.
        String application = DubboUtils.getApplication(invocation, "");
        RpcContext rpcContext = RpcContext.getContext();
        Entry interfaceEntry;
        Entry methodEntry;

        SentinelResource sentinelResource = getSentinelResource(invoker, invocation);
        try {
            if (Objects.nonNull(sentinelResource)) {
                String methodResourceName = Objects.isNull(sentinelResource.value()) ? DubboUtils.getResourceName(invoker, invocation, DubboConfig.getDubboProviderPrefix()) : sentinelResource.value();
                String interfaceResourceName = DubboConfig.getDubboInterfaceGroupAndVersionEnabled() ? invoker.getUrl().getColonSeparatedKey()
                        : invoker.getInterface().getName();
                // Only need to create entrance context at provider side, as context will take effect
                // at entrance of invocation chain only (for inbound traffic).
                ContextUtil.enter(methodResourceName, application);
                interfaceEntry = SphU.entry(interfaceResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.IN);
                rpcContext.set(DubboUtils.DUBBO_INTERFACE_ENTRY_KEY, interfaceEntry);
                methodEntry = SphU.entry(methodResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.IN, invocation.getArguments());
                rpcContext.set(DubboUtils.DUBBO_METHOD_ENTRY_KEY, methodEntry);
            }
            return invoker.invoke(invocation);
        } catch (BlockException e) {
            return blockHandle(invoker, invocation, e, sentinelResource, DubboFallbackRegistry.getProviderFallback());
        }
    }
}
