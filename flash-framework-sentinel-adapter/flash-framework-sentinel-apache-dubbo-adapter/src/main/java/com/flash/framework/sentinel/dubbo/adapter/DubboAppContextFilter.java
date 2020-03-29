package com.flash.framework.sentinel.dubbo.adapter;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;

/**
 * Puts current consumer's application name in the attachment of each invocation.
 *
 * @author Eric Zhao
 */
@Activate(group = CONSUMER)
public class DubboAppContextFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String application = invoker.getUrl().getParameter(CommonConstants.APPLICATION_KEY);
        if (application != null) {
            RpcContext.getContext().setAttachment(DubboUtils.SENTINEL_DUBBO_APPLICATION_KEY, application);
        }
        return invoker.invoke(invocation);
    }
}
