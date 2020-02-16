package com.flash.framework.sentinel.core.autoconfigure;

import com.flash.framework.sentinel.core.cluster.TokenClient;
import com.flash.framework.sentinel.core.cluster.TokenServer;
import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhurg
 * @date 2019/9/4 - 下午5:03
 */
@Data
public class SentinelClusterConfigure implements Serializable {

    private static final long serialVersionUID = -3848915794207742411L;

    @NestedConfigurationProperty
    private TokenServer tokenServer;

    /**
     * token client 地址列表
     */
    @NestedConfigurationProperty
    private List<TokenClient> tokenClients;
}