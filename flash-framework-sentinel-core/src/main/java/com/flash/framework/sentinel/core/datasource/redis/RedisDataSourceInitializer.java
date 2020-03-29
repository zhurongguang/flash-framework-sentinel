package com.flash.framework.sentinel.core.datasource.redis;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.redis.RedisDataSource;
import com.alibaba.csp.sentinel.datasource.redis.config.RedisConnectionConfig;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.fastjson.JSON;
import com.flash.framework.sentinel.core.autoconfigure.SentinelConfigure;
import com.flash.framework.sentinel.core.cluster.ClusterGroup;
import com.flash.framework.sentinel.core.datasource.DataSourceInitializer;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Optional;

/**
 * @author zhurg
 * @date 2019/9/2 - 下午4:49
 */
@ConditionalOnProperty(prefix = "sentinel", name = "datasource", havingValue = "REDIS")
public class RedisDataSourceInitializer implements DataSourceInitializer, EnvironmentAware {

    private static final String SPRING_REDIS_SENTINEL_MASTER = "spring.redis.sentinel.master";

    private static final String SPRING_REDIS_SENTINEL_NODES = "spring.redis.sentinel.nodes";

    private static final String SPRING_REDIS_HOST = "spring.redis.host";

    private static final String SPRING_REDIS_PORT = "spring.redis.port";

    private static final String SPRING_REDIS_DATABASE = "spring.redis.database";

    private static final String SPRING_REDIS_PASSWORD = "spring.redis.password";

    private Environment environment;

    private final String redisKeyPrefix;

    public RedisDataSourceInitializer(String redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }

    @Override
    public void localInit(SentinelConfigure sentinelConfigure) {
        //初始化限流
        if (StringUtils.isNotBlank(sentinelConfigure.getFlowRuleDataIdSuffix())) {
            String channel = buildDataId(sentinelConfigure.getFlowRuleDataIdSuffix());
            ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new RedisDataSource<>(buildRedisConnectionConfig(), buildKey(channel), channel,
                    (source) -> JSON.parseArray(source, FlowRule.class));
            FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
        }
        //初始化熔断降级规则
        if (StringUtils.isNotBlank(sentinelConfigure.getDegradeRuleDataIdSuffix())) {
            String channel = buildDataId(sentinelConfigure.getDegradeRuleDataIdSuffix());
            ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new RedisDataSource<>(buildRedisConnectionConfig(), buildKey(channel), channel,
                    (source) -> JSON.parseArray(source, DegradeRule.class));
            DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());
        }
        //初始化系统保护规则
        if (StringUtils.isNotBlank(sentinelConfigure.getSystemRuleDataIdSuffix())) {
            String channel = buildDataId(sentinelConfigure.getSystemRuleDataIdSuffix());
            ReadableDataSource<String, List<SystemRule>> systemRuleDataSource = new RedisDataSource<>(buildRedisConnectionConfig(), buildKey(channel), channel,
                    (source) -> JSON.parseArray(source, SystemRule.class));
            SystemRuleManager.register2Property(systemRuleDataSource.getProperty());
        }
        //初始化热点数据规则
        if (StringUtils.isNotBlank(sentinelConfigure.getParamFlowDataIdSuffix())) {
            String channel = buildDataId(sentinelConfigure.getParamFlowDataIdSuffix());
            ReadableDataSource<String, List<ParamFlowRule>> paramFlowDataSource = new RedisDataSource<>(buildRedisConnectionConfig(), buildKey(channel), channel,
                    (source) -> JSON.parseArray(source, ParamFlowRule.class));
            ParamFlowRuleManager.register2Property(paramFlowDataSource.getProperty());
        }
    }

    @Override
    public void aloneClusterInit(SentinelConfigure sentinelConfigure) {
        tokenClientInit(sentinelConfigure);
    }

    @Override
    public void embeddedClusterInit(SentinelConfigure sentinelConfigure) {
        tokenClientInit(sentinelConfigure);
        tokenServerInit(sentinelConfigure);
    }

    @Override
    public void tokenClientInit(SentinelConfigure sentinelConfigure) {
        //通过动态数据源初始化Token Client 的 requestTimeout
        if (StringUtils.isNotBlank(sentinelConfigure.getClusterClientConfigDataIdSuffix())) {
            String channel = buildDataId(sentinelConfigure.getClusterClientConfigDataIdSuffix());
            ReadableDataSource<String, ClusterClientConfig> clusterClientConfigNacosDataSource = new RedisDataSource<>(buildRedisConnectionConfig(), buildKey(channel), channel,
                    source -> JSON.parseObject(source, ClusterClientConfig.class));
            ClusterClientConfigManager.registerClientConfigProperty(clusterClientConfigNacosDataSource.getProperty());
        } else {
            initTokenClientByLocal(sentinelConfigure);
        }
        if (StringUtils.isNotBlank(sentinelConfigure.getClusterDataIdSuffix())) {
            //初始化Token Client 访问 Token Server的配置
            String channel = buildDataId(sentinelConfigure.getClusterDataIdSuffix());
            ReadableDataSource<String, ClusterClientAssignConfig> clientAssignDs = new RedisDataSource<>(buildRedisConnectionConfig(), buildKey(channel), channel, source -> {
                List<ClusterGroup> groupList = JSON.parseArray(source, ClusterGroup.class);
                return Optional.ofNullable(groupList)
                        .flatMap(this::extractClientAssignment)
                        .orElse(null);
            });
            ClusterClientConfigManager.registerServerAssignProperty(clientAssignDs.getProperty());
        } else {
            initClusterClientAssignConfigByLocal(sentinelConfigure);
        }
    }

    @Override
    public void tokenServerInit(SentinelConfigure sentinelConfigure) {
        //Token Server端注册限流动态数据源
        if (StringUtils.isNotBlank(sentinelConfigure.getFlowRuleDataIdSuffix())) {
            ClusterFlowRuleManager.setPropertySupplier(namespace -> {
                String channel = buildDataId(sentinelConfigure.getFlowRuleDataIdSuffix());
                ReadableDataSource<String, List<FlowRule>> ds = new RedisDataSource<>(buildRedisConnectionConfig(), buildKey(channel), channel,
                        source -> JSON.parseArray(source, FlowRule.class));
                return ds.getProperty();
            });
        }
        //Token Server端注册群热点数据动态数据源
        if (StringUtils.isNotBlank(sentinelConfigure.getParamFlowDataIdSuffix())) {
            ClusterParamFlowRuleManager.setPropertySupplier(namespace -> {
                String channel = buildDataId(sentinelConfigure.getParamFlowDataIdSuffix());
                ReadableDataSource<String, List<ParamFlowRule>> ds = new RedisDataSource<>(buildRedisConnectionConfig(), buildKey(channel), channel, source -> JSON.parseArray(source, ParamFlowRule.class));
                return ds.getProperty();
            });
        }
        if (StringUtils.isNotBlank(sentinelConfigure.getClusterDataIdSuffix())) {
            //初始化token server 的 ServerTransportConfig
            String channel = buildDataId(sentinelConfigure.getClusterDataIdSuffix());
            ReadableDataSource<String, ServerTransportConfig> serverTransportDs = new RedisDataSource<>(buildRedisConnectionConfig(), buildKey(channel), channel, source -> {
                List<ClusterGroup> groupList = JSON.parseArray(source, ClusterGroup.class);
                return Optional.ofNullable(groupList)
                        .flatMap(this::extractServerTransportConfig)
                        .orElse(null);
            });
            ClusterServerConfigManager.registerServerTransportProperty(serverTransportDs.getProperty());
        } else {
            initTokenServerByLocal(sentinelConfigure);
        }
    }

    @Override
    public void commonInit(SentinelConfigure sentinelConfigure) {
        if (StringUtils.isNotBlank(sentinelConfigure.getClusterDataIdSuffix())) {
            String channel = buildDataId(sentinelConfigure.getClusterDataIdSuffix());
            //初始化当前节点的状态
            ReadableDataSource<String, Integer> clusterModeDs = new RedisDataSource<>(buildRedisConnectionConfig(), buildKey(channel), channel, source -> {
                List<ClusterGroup> groupList = JSON.parseArray(source, ClusterGroup.class);
                return Optional.ofNullable(groupList)
                        .map(this::extractMode)
                        .orElse(ClusterStateManager.CLUSTER_NOT_STARTED);
            });
            ClusterStateManager.registerProperty(clusterModeDs.getProperty());
        } else {
            initCommonByLocal(sentinelConfigure);
        }
    }

    protected RedisConnectionConfig buildRedisConnectionConfig() {
        if (environment.containsProperty(SPRING_REDIS_SENTINEL_NODES)) {
            Integer database = environment.containsProperty(SPRING_REDIS_DATABASE) ? environment.getProperty(SPRING_REDIS_DATABASE, Integer.class) : 0;
            String password = environment.getProperty(SPRING_REDIS_PASSWORD);
            String sentinelMasterId = environment.getProperty(SPRING_REDIS_SENTINEL_MASTER);
            String nodes = environment.getProperty(SPRING_REDIS_SENTINEL_NODES);
            RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
            builder.withSentinelMasterId(sentinelMasterId)
                    .withDatabase(database);
            if (StringUtils.isNotBlank(password)) {
                builder.withPassword(password);
            }
            List<String> hosts = Splitter.on(",").splitToList(nodes);
            for (String host : hosts) {
                String[] hp = host.split(":");
                builder.withRedisSentinel(hp[0], Integer.parseInt(hp[1]));
            }
            return builder.build();
        } else {
            String host = environment.getProperty(SPRING_REDIS_HOST);
            Integer port = environment.getProperty(SPRING_REDIS_PORT, Integer.class);
            Integer database = environment.getProperty(SPRING_REDIS_DATABASE, Integer.class);
            String password = environment.getProperty(SPRING_REDIS_PASSWORD);
            RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
            builder.withHost(host).withPort(port).withDatabase(database);
            if (StringUtils.isNotBlank(password)) {
                builder.withPassword(password);
            }
            return builder.build();
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * 构建缓存存储的key
     *
     * @param dataId
     * @return
     */
    private String buildKey(String dataId) {
        return String.format("%s:%s:%s", redisKeyPrefix, AppNameUtil.getAppName(), dataId);
    }
}
