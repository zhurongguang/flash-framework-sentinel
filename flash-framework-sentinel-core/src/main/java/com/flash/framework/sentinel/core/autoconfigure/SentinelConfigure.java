package com.flash.framework.sentinel.core.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author zhurg
 * @date 2019/4/19 - 下午3:54
 */
@Data
@ConfigurationProperties(prefix = "sentinel")
public class SentinelConfigure {

    /**
     * 是否启用
     */
    private boolean enable;

    /**
     * Sentinel Dashboard 地址
     */
    private String dashboardServer;

    /**
     * TokenClient port
     */
    private int tokenClientPort = 8720;

    /**
     * TokenServer port
     */
    private int tokenServerPort = 18730;

    /**
     * 配置中心groupId
     */
    private String groupId;

    /**
     * 配置中心Sentinel限流规则dataId
     */
    private String flowRuleDataId;

    /**
     * 配置中心Sentinel降级规则dataId
     */
    private String degradeRuleDataId;

    /**
     * 配置中心Sentinel系统保护规则dataId
     */
    private String systemRuleDataId;

    /**
     * 配置中心Sentinel热点数据规则dataId
     */
    private String paramFlowDataId;

    /**
     * 配置中心Sentinel tokenClient节点集群配置 dataId
     */
    private String clusterClientConfigDataId;

    /**
     * 配置中心Sentinel token server/token client集群配置dataId
     */
    private String clusterDataId;

    /**
     * 配置中心地址
     */
    private String remoteAddress;

    /**
     * apollo namespaceName
     */
    private String apolloNamespace;

    /**
     * 配置中心类型
     */
    private SentinelDataSourceType dataSource = SentinelDataSourceType.ZOOKEEPER;

    /**
     * Sentinel 启动模式
     */
    private SentinelMode mode = SentinelMode.LOCAL;

    /**
     * 启动token server集群
     */
    private boolean tokenServerCluster;

    /**
     * 集群配置
     */
    @NestedConfigurationProperty
    private SentinelClusterConfigure cluster;

}