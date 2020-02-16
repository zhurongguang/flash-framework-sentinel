package com.flash.framework.sentinel.core.cluster.redis;

import com.flash.framework.sentinel.core.autoconfigure.SentinelConfigure;
import com.flash.framework.sentinel.core.cluster.ClusterConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author zhurg
 * @date 2019/4/24 - 下午6:13
 */
public class RedisClusterConfigService implements ClusterConfigService {

    private final SentinelConfigure sentinelConfigure;

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisClusterConfigService(SentinelConfigure sentinelConfigure, StringRedisTemplate redisTemplate) {
        this.sentinelConfigure = sentinelConfigure;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String getConfig() {
        return redisTemplate.opsForValue().get(sentinelConfigure.getClusterDataId());
    }

    @Override
    public boolean publishConfig(String data) {
        redisTemplate.opsForValue().set(sentinelConfigure.getClusterDataId(), data);
        redisTemplate.convertAndSend(sentinelConfigure.getClusterDataId(), data);
        return true;
    }
}