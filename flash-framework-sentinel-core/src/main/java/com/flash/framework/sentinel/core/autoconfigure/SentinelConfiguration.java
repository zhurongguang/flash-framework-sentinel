package com.flash.framework.sentinel.core.autoconfigure;

import com.flash.framework.sentinel.core.SentinelInitializer;
import com.flash.framework.sentinel.core.cluster.ClusterConfigService;
import com.flash.framework.sentinel.core.cluster.apollo.ApolloClusterConfigService;
import com.flash.framework.sentinel.core.cluster.handler.TokenServerHandler;
import com.flash.framework.sentinel.core.cluster.nacos.NacosClusterConfigService;
import com.flash.framework.sentinel.core.cluster.redis.RedisClusterConfigService;
import com.flash.framework.sentinel.core.cluster.zookeeper.ZookeeperClusterConfigService;
import com.flash.framework.sentinel.core.datasource.DataSourceInitializer;
import com.flash.framework.zookeeper.factory.ZkClientFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author zhurg
 * @date 2019/4/22 - 下午4:51
 */
@ConditionalOnProperty(prefix = "sentinel", name = "enable", havingValue = "true")
@Configuration
@EnableConfigurationProperties(SentinelConfigure.class)
@ComponentScan({"com.flash.framework.sentinel.core"})
public class SentinelConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SentinelInitializer sentinelClientInitializer(SentinelConfigure sentinelConfigure, DataSourceInitializer dataSourceInitializer,
                                                         Environment environment) {
        return new SentinelInitializer(sentinelConfigure, dataSourceInitializer, environment);
    }

    @Configuration
    @ConditionalOnProperty(prefix = "sentinel", name = "mode", havingValue = "CLUSTER_EMBEDDED")
    @EnableConfigurationProperties(SentinelConfigure.class)
    public static class EmbeddedClusterConfiguration {

        @Bean(name = "custerConfigService")
        @ConditionalOnProperty(prefix = "sentinel", name = "datasource", havingValue = "APOLLO")
        @ConditionalOnMissingBean
        public ClusterConfigService apolloClusterConfigService(SentinelConfigure sentinelConfigure, ZkClientFactory zkClientFactory) {
            return new ApolloClusterConfigService(sentinelConfigure, zkClientFactory);
        }

        @Bean(name = "custerConfigService")
        @ConditionalOnProperty(prefix = "sentinel", name = "datasource", havingValue = "NACOS")
        @ConditionalOnMissingBean
        public ClusterConfigService nacosClusterConfigService(SentinelConfigure sentinelConfigure) {
            return new NacosClusterConfigService(sentinelConfigure);
        }

        @Bean(name = "custerConfigService")
        @ConditionalOnProperty(prefix = "sentinel", name = "datasource", havingValue = "REDIS")
        @ConditionalOnMissingBean
        public ClusterConfigService redisClusterConfigService(SentinelConfigure sentinelConfigure, StringRedisTemplate redisTemplate) {
            return new RedisClusterConfigService(sentinelConfigure, redisTemplate);
        }

        @Bean(name = "custerConfigService")
        @ConditionalOnProperty(prefix = "sentinel", name = "datasource", havingValue = "ZOOKEEPER", matchIfMissing = true)
        @ConditionalOnMissingBean
        public ClusterConfigService zookeeperClusterConfigService(SentinelConfigure sentinelConfigure, ZkClientFactory zkClientFactory) {
            return new ZookeeperClusterConfigService(sentinelConfigure, zkClientFactory);
        }


        @Bean(name = "embeddedClusterTokenServerHandler")
        @ConditionalOnMissingBean
        @ConditionalOnProperty(prefix = "sentinel", name = "token-server-cluster", havingValue = "true")
        public TokenServerHandler tokenServerHandler(SentinelConfigure sentinelConfigure, ClusterConfigService clusterConfigService) {
            return new TokenServerHandler(sentinelConfigure, clusterConfigService);
        }
    }
}