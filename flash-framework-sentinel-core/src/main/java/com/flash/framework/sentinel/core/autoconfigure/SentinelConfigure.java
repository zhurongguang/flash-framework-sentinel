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
    private boolean enable = true;

    /**
     * Sentinel Dashboard 地址
     */
    private String dashboardServer;

    /**
     * TokenClient port
     */
    private int tokenClientPort = 8719;

    /**
     * TokenServer port
     */
    private int tokenServerPort = 18730;

    /**
     * 配置中心groupId,Nacos使用
     */
    private String nacosGroupId;

    /**
     * 配置中心Sentinel限流规则dataId后缀
     */
    private String flowRuleDataIdSuffix;

    /**
     * 配置中心Sentinel降级规则dataId后缀
     */
    private String degradeRuleDataIdSuffix;

    /**
     * 配置中心Sentinel系统保护规则dataId后缀
     */
    private String systemRuleDataIdSuffix;

    /**
     * 配置中心Sentinel热点数据规则dataId后缀
     */
    private String paramFlowDataIdSuffix;

    /**
     * 配置中心Sentinel tokenClient节点集群配置 dataId后缀
     */
    private String clusterClientConfigDataIdSuffix;

    /**
     * 配置中心Sentinel token server/token client集群配置dataId后缀
     */
    private String clusterDataIdSuffix;

    /**
     * 配置中心地址
     */
    private String remoteAddress;

    /**
     * apollo namespaceName
     */
    private String apolloNamespace;

    /**
     * zookeeper path
     */
    private String zookeeperPath = "sentinel-rules";

    /**
     * redis key 前缀
     */
    private String redisKeyPrefix = "sentinel-rules";

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