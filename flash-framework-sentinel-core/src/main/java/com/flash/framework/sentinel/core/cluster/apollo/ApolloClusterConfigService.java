package com.flash.framework.sentinel.core.cluster.apollo;

import com.ctrip.framework.apollo.ConfigService;
import com.flash.framework.sentinel.core.autoconfigure.SentinelConfigure;
import com.flash.framework.sentinel.core.cluster.ClusterConfigService;
import com.flash.framework.zookeeper.factory.ZkClientFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhurg
 * @date 2019/9/5 - 下午4:12
 */
public class ApolloClusterConfigService implements ClusterConfigService {

    private final SentinelConfigure sentinelConfigure;

    private final ZkClientFactory zkClientFactory;


    @Autowired
    public ApolloClusterConfigService(SentinelConfigure sentinelConfigure, ZkClientFactory zkClientFactory) {
        this.sentinelConfigure = sentinelConfigure;
        this.zkClientFactory = zkClientFactory;
    }

    @Override
    public String getConfig() {
        return ConfigService.getConfig(sentinelConfigure.getApolloNamespace()).getProperty(sentinelConfigure.getClusterDataId(), null);
    }

    @Override
    public boolean publishConfig(String data) {
        String path = String.format("/%s", sentinelConfigure.getClusterDataId());
        if (zkClientFactory.isExisted(path)) {
            zkClientFactory.update(path, data);
        } else {
            zkClientFactory.persist(path, data);
        }
        return true;
    }
}
