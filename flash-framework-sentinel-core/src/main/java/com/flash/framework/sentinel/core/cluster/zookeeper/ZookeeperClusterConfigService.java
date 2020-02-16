package com.flash.framework.sentinel.core.cluster.zookeeper;

import com.flash.framework.sentinel.core.autoconfigure.SentinelConfigure;
import com.flash.framework.sentinel.core.cluster.ClusterConfigService;
import com.flash.framework.zookeeper.factory.ZkClientFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhurg
 * @date 2019/4/24 - 下午6:22
 */
public class ZookeeperClusterConfigService implements ClusterConfigService {

    private final SentinelConfigure sentinelConfigure;

    private final ZkClientFactory zkClientFactory;

    @Autowired
    public ZookeeperClusterConfigService(SentinelConfigure sentinelConfigure, ZkClientFactory zkClientFactory) {
        this.sentinelConfigure = sentinelConfigure;
        this.zkClientFactory = zkClientFactory;
    }

    @Override
    public String getConfig() {
        String path = String.format("/%s", sentinelConfigure.getClusterDataId());
        if (zkClientFactory.isExisted(path)) {
            return zkClientFactory.get(path);
        }
        return null;
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