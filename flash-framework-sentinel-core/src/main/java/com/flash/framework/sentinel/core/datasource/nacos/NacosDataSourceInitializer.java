package com.flash.framework.sentinel.core.datasource.nacos;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.flash.framework.sentinel.core.autoconfigure.SentinelConfigure;
import com.flash.framework.sentinel.core.cluster.ClusterGroup;
import com.flash.framework.sentinel.core.datasource.DataSourceInitializer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * @author zhurg
 * @date 2019/9/2 - 下午3:01
 */
@Component
@ConditionalOnProperty(prefix = "sentinel", name = "datasource", havingValue = "NACOS")
public class NacosDataSourceInitializer implements DataSourceInitializer {

    @Override
    public void localInit(SentinelConfigure sentinelConfigure) {
        //初始化限流
        if (StringUtils.isNotBlank(sentinelConfigure.getFlowRuleDataId())) {
            ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(sentinelConfigure.getRemoteAddress(), sentinelConfigure.getGroupId(),
                    sentinelConfigure.getFlowRuleDataId(), (source) -> JSON.parseArray(source, FlowRule.class));
            FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
        }
        //初始化熔断降级规则
        if (StringUtils.isNotBlank(sentinelConfigure.getDegradeRuleDataId())) {
            ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new NacosDataSource<>(sentinelConfigure.getRemoteAddress(), sentinelConfigure.getGroupId(),
                    sentinelConfigure.getFlowRuleDataId(), (source) -> JSON.parseArray(source, DegradeRule.class));
            DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());
        }
        //初始化系统保护规则
        if (StringUtils.isNotBlank(sentinelConfigure.getSystemRuleDataId())) {
            ReadableDataSource<String, List<SystemRule>> systemRuleDataSource = new NacosDataSource<>(sentinelConfigure.getRemoteAddress(), sentinelConfigure.getGroupId(),
                    sentinelConfigure.getFlowRuleDataId(), (source) -> JSON.parseArray(source, SystemRule.class));
            SystemRuleManager.register2Property(systemRuleDataSource.getProperty());
        }
        //初始化热点数据规则
        if (StringUtils.isNotBlank(sentinelConfigure.getParamFlowDataId())) {
            ReadableDataSource<String, List<ParamFlowRule>> paramFlowDataSource = new NacosDataSource<>(sentinelConfigure.getRemoteAddress(), sentinelConfigure.getGroupId(),
                    sentinelConfigure.getFlowRuleDataId(), (source) -> JSON.parseArray(source, ParamFlowRule.class));
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
        if (StringUtils.isNotBlank(sentinelConfigure.getClusterClientConfigDataId())) {
            ReadableDataSource<String, ClusterClientConfig> clusterClientConfigNacosDataSource = new NacosDataSource<>(sentinelConfigure.getRemoteAddress(), sentinelConfigure.getGroupId(),
                    sentinelConfigure.getClusterClientConfigDataId(), source -> JSON.parseObject(source, ClusterClientConfig.class));
            ClusterClientConfigManager.registerClientConfigProperty(clusterClientConfigNacosDataSource.getProperty());
        } else {
            initTokenClientByLocal(sentinelConfigure);
        }
        if (StringUtils.isNotBlank(sentinelConfigure.getClusterDataId())) {
            //初始化Token Client 访问 Token Server的配置
            ReadableDataSource<String, ClusterClientAssignConfig> clientAssignDs = new NacosDataSource<>(sentinelConfigure.getRemoteAddress(), sentinelConfigure.getGroupId(),
                    sentinelConfigure.getClusterDataId(), source -> {
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
        if (StringUtils.isNotBlank(sentinelConfigure.getFlowRuleDataId())) {
            ClusterFlowRuleManager.setPropertySupplier(namespace -> {
                Properties properties = new Properties();
                properties.setProperty(PropertyKeyConst.NAMESPACE, namespace);
                properties.setProperty(PropertyKeyConst.SERVER_ADDR, sentinelConfigure.getRemoteAddress());
                ReadableDataSource<String, List<FlowRule>> ds = new NacosDataSource<>(properties, sentinelConfigure.getGroupId(),
                        sentinelConfigure.getFlowRuleDataId(), source -> JSON.parseArray(source, FlowRule.class));
                return ds.getProperty();
            });
        }
        //Token Server端注册群热点数据动态数据源
        if (StringUtils.isNotBlank(sentinelConfigure.getParamFlowDataId())) {
            ClusterParamFlowRuleManager.setPropertySupplier(namespace -> {
                Properties properties = new Properties();
                properties.setProperty(PropertyKeyConst.NAMESPACE, namespace);
                ReadableDataSource<String, List<ParamFlowRule>> ds = new NacosDataSource<>(properties, sentinelConfigure.getGroupId(),
                        sentinelConfigure.getParamFlowDataId(), source -> JSON.parseArray(source, ParamFlowRule.class));
                return ds.getProperty();
            });
        }
        if (StringUtils.isNotBlank(sentinelConfigure.getClusterDataId())) {
            //初始化token server 的 ServerTransportConfig
            ReadableDataSource<String, ServerTransportConfig> serverTransportDs = new NacosDataSource<>(sentinelConfigure.getRemoteAddress(), sentinelConfigure.getGroupId(),
                    sentinelConfigure.getClusterDataId(), source -> {
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
        if (StringUtils.isNotBlank(sentinelConfigure.getClusterDataId())) {
            //初始化当前节点的状态
            ReadableDataSource<String, Integer> clusterModeDs = new NacosDataSource<>(sentinelConfigure.getRemoteAddress(), sentinelConfigure.getGroupId(),
                    sentinelConfigure.getClusterDataId(), source -> {
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
}
