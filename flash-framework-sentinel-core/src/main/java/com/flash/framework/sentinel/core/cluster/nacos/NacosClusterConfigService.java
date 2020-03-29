package com.flash.framework.sentinel.core.cluster.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.flash.framework.sentinel.core.autoconfigure.SentinelConfigure;
import com.flash.framework.sentinel.core.cluster.ClusterConfigService;
import com.flash.framework.sentinel.core.datasource.DataIdBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * @author zhurg
 * @date 2019/4/24 - 下午5:53
 */
@Slf4j
public class NacosClusterConfigService implements ClusterConfigService, DataIdBuilder {

    private ConfigService configService;

    private final SentinelConfigure sentinelConfigure;

    @Autowired
    public NacosClusterConfigService(SentinelConfigure sentinelConfigure) {
        this.sentinelConfigure = sentinelConfigure;
    }

    @PostConstruct
    public void init() throws NacosException {
        configService = NacosFactory.createConfigService(sentinelConfigure.getRemoteAddress());
    }


    @Override
    public String getConfig() {
        try {
            return configService.getConfig(buildDataId(sentinelConfigure.getClusterDataIdSuffix()), sentinelConfigure.getNacosGroupId(), 3000);
        } catch (Exception e) {
            log.error("[Sentinel] Sentinel Nacos client get config failed ", e);
            return null;
        }
    }

    @Override
    public boolean publishConfig(String data) {
        try {
            return configService.publishConfig(buildDataId(sentinelConfigure.getClusterDataIdSuffix()), sentinelConfigure.getNacosGroupId(), data);
        } catch (Exception e) {
            log.error("[Sentinel] Sentinel Nacos client publish config failed ", e);
            return false;
        }
    }
}