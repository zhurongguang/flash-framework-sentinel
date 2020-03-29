package com.flash.framework.sentinel.core.cluster.redis;

import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.flash.framework.sentinel.core.autoconfigure.SentinelConfigure;
import com.flash.framework.sentinel.core.cluster.ClusterConfigService;
import com.flash.framework.sentinel.core.datasource.DataIdBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author zhurg
 * @date 2019/4/24 - 下午6:13
 */
public class RedisClusterConfigService implements ClusterConfigService, DataIdBuilder {

    private final SentinelConfigure sentinelConfigure;

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisClusterConfigService(SentinelConfigure sentinelConfigure, StringRedisTemplate redisTemplate) {
        this.sentinelConfigure = sentinelConfigure;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String getConfig() {
        return redisTemplate.opsForValue().get(buildKey(sentinelConfigure.getClusterDataIdSuffix()));
    }

    @Override
    public boolean publishConfig(String data) {
        redisTemplate.convertAndSend(buildKey(sentinelConfigure.getClusterDataIdSuffix()), data);
        return true;
    }

    /**
     * 构建缓存存储的key
     *
     * @param dataId
     * @return
     */
    private String buildKey(String dataId) {
        return String.format("%s:%s:%s", sentinelConfigure.getRedisKeyPrefix(), AppNameUtil.getAppName(), dataId);
    }
}