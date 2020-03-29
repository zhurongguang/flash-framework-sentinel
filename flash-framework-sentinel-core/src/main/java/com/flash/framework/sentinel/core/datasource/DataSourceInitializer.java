package com.flash.framework.sentinel.core.datasource;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.flash.framework.sentinel.core.autoconfigure.SentinelConfigure;
import com.flash.framework.sentinel.core.cluster.ClusterGroup;
import com.flash.framework.sentinel.core.cluster.TokenClient;
import com.flash.framework.sentinel.core.cluster.TokenServer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author zhurg
 * @date 2019/8/29 - 下午6:14
 */
public interface DataSourceInitializer extends DataIdBuilder {

    /**
     * 本地限流模式初始化
     *
     * @param sentinelConfigure
     */
    void localInit(SentinelConfigure sentinelConfigure);

    /**
     * Token Server Alone模式集群初始化
     *
     * @param sentinelConfigure
     */
    void aloneClusterInit(SentinelConfigure sentinelConfigure);

    /**
     * Token Server Embedded模式集群初始化
     *
     * @param sentinelConfigure
     */
    void embeddedClusterInit(SentinelConfigure sentinelConfigure);

    /**
     * Token Client 初始化
     *
     * @param sentinelConfigure
     */
    void tokenClientInit(SentinelConfigure sentinelConfigure);

    /**
     * Token Server 初始化
     *
     * @param sentinelConfigure
     */
    void tokenServerInit(SentinelConfigure sentinelConfigure);

    /**
     * 基础配置初始化
     *
     * @param sentinelConfigure
     */
    void commonInit(SentinelConfigure sentinelConfigure);

    /**
     * 判断当前节点状态，是token server 还是token client
     *
     * @param groupList
     * @return
     */
    default int extractMode(List<ClusterGroup> groupList) {
        if (groupList.stream().anyMatch(this::machineEqual)) {
            return ClusterStateManager.CLUSTER_SERVER;
        }
        boolean canBeClient = groupList.stream()
                .flatMap(e -> e.getClientSet().stream())
                .filter(Objects::nonNull)
                .anyMatch(e -> e.equals(getCurrentMachineId()));
        return canBeClient ? ClusterStateManager.CLUSTER_CLIENT : ClusterStateManager.CLUSTER_NOT_STARTED;
    }

    /**
     * 获取当期Token Server的配置
     *
     * @param groupList
     * @return
     */
    default Optional<ServerTransportConfig> extractServerTransportConfig(List<ClusterGroup> groupList) {
        return groupList.stream()
                .filter(this::machineEqual)
                .findAny()
                .map(e -> new ServerTransportConfig().setPort(e.getPort()).setIdleSeconds(e.getIdleSeconds()));
    }

    /**
     * 获取当前Token Client 需要访问的Token Server的Host地址
     *
     * @param groupList
     * @return
     */
    default Optional<ClusterClientAssignConfig> extractClientAssignment(List<ClusterGroup> groupList) {
        if (groupList.stream().anyMatch(this::machineEqual)) {
            return Optional.empty();
        }
        for (ClusterGroup group : groupList) {
            if (group.getClientSet().contains(getCurrentMachineId())) {
                String ip = group.getIp();
                Integer port = group.getPort();
                return Optional.of(new ClusterClientAssignConfig(ip, port));
            }
        }
        return Optional.empty();
    }

    default boolean machineEqual(ClusterGroup group) {
        return getCurrentMachineId().equals(group.getMachineId());
    }

    /**
     * 获取当前节点名称
     *
     * @return
     */
    default String getCurrentMachineId() {
        return HostNameUtil.getIp() + ":" + TransportConfig.getPort();
    }

    /**
     * 加载本地Token Client集群配置
     *
     * @param sentinelConfigure
     * @return
     */
    default TokenClient getLocalTokenClientProperties(SentinelConfigure sentinelConfigure) {
        if (Objects.nonNull(sentinelConfigure.getCluster())) {
            Optional<TokenClient> optionalTokenClient = sentinelConfigure.getCluster().getTokenClients().stream()
                    .filter(tokenClient -> getCurrentMachineId().equals(tokenClient.getHost() + ":" + tokenClient.getPort()))
                    .findFirst();
            return optionalTokenClient.isPresent() ? optionalTokenClient.get() : null;
        }
        return null;
    }

    /**
     * 加载本地Token Server集群配置
     *
     * @param sentinelConfigure
     * @return
     */
    default TokenServer getLocalTokenServerProperties(SentinelConfigure sentinelConfigure) {
        if (Objects.nonNull(sentinelConfigure.getCluster())) {
            TokenServer tokenServer = sentinelConfigure.getCluster().getTokenServer();
            if (getCurrentMachineId().equals((tokenServer.getHost() + ":" + tokenServer.getPort()))) {
                return tokenServer;
            }
            return null;
        }
        return null;
    }

    /**
     * 通过本地配置初始化TokenClient
     *
     * @param sentinelConfigure
     */
    default void initTokenClientByLocal(SentinelConfigure sentinelConfigure) {
        if (Objects.nonNull(getLocalTokenClientProperties(sentinelConfigure))) {
            TokenClient tokenClient = getLocalTokenClientProperties(sentinelConfigure);
            //初始化客户端规则
            ClusterClientConfig clusterClientConfig = new ClusterClientConfig();
            //指定获取Token超时时间
            clusterClientConfig.setRequestTimeout(tokenClient.getRequestTimeout());
            //Client指定配置
            ClusterClientConfigManager.applyNewConfig(clusterClientConfig);
        }
    }

    /**
     * 通过本地配置初始化TokenServer
     *
     * @param sentinelConfigure
     */
    default void initTokenServerByLocal(SentinelConfigure sentinelConfigure) {
        if (Objects.nonNull(getLocalTokenServerProperties(sentinelConfigure))) {
            TokenServer tokenServer = getLocalTokenServerProperties(sentinelConfigure);
            ServerTransportConfig ServerTransportConfig = new ServerTransportConfig(tokenServer.getPort(), tokenServer.getIdleSeconds());
            //加载配置
            ClusterServerConfigManager.loadGlobalTransportConfig(ServerTransportConfig);
        }
    }

    /**
     * 通过本地配置初始化基本配置
     *
     * @param sentinelConfigure
     */
    default void initCommonByLocal(SentinelConfigure sentinelConfigure) {
        if (Objects.nonNull(getLocalTokenClientProperties(sentinelConfigure))) {
            ClusterStateManager.setToClient();
        } else if (Objects.nonNull(getLocalTokenServerProperties(sentinelConfigure))) {
            ClusterStateManager.setToServer();
        }
    }

    default void initClusterClientAssignConfigByLocal(SentinelConfigure sentinelConfigure) {
        if (Objects.nonNull(getLocalTokenServerProperties(sentinelConfigure))) {
            TokenServer tokenServer = getLocalTokenServerProperties(sentinelConfigure);
            ClusterClientAssignConfig clusterClientAssignConfig = new ClusterClientAssignConfig();
            clusterClientAssignConfig.setServerHost(tokenServer.getHost());
            clusterClientAssignConfig.setServerPort(tokenServer.getPort());
            ClusterClientConfigManager.applyNewAssignConfig(clusterClientAssignConfig);
        }
    }
}