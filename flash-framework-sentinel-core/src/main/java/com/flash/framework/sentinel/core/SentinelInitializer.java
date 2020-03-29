package com.flash.framework.sentinel.core;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.flash.framework.sentinel.core.autoconfigure.SentinelConfigure;
import com.flash.framework.sentinel.core.autoconfigure.SentinelDataSourceType;
import com.flash.framework.sentinel.core.autoconfigure.SentinelMode;
import com.flash.framework.sentinel.core.datasource.DataSourceInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

/**
 * @author zhurg
 * @date 2019/4/22 - 下午3:24
 */
public class SentinelInitializer {

    private static final String SPRING_APPLICATION_NAME = "spring.application.name";

    private final SentinelConfigure sentinelConfigure;

    private final DataSourceInitializer dataSourceInitializer;

    private final Environment environment;


    @Autowired
    public SentinelInitializer(SentinelConfigure sentinelConfigure, DataSourceInitializer dataSourceInitializer, Environment environment) {
        this.sentinelConfigure = sentinelConfigure;
        this.dataSourceInitializer = dataSourceInitializer;
        this.environment = environment;
    }

    @PostConstruct
    public void doinit() {
        //校验配置
        validateConfigure();

        //初始化系统配置
        initSystemConfig();

        //初始化sentinel配置
        initSentinel();
    }

    /**
     * 初始化系统配置
     */
    protected void initSystemConfig() {
        String appName = environment.containsProperty(SPRING_APPLICATION_NAME) ? environment.getProperty(SPRING_APPLICATION_NAME) : AppNameUtil.getAppName();
        System.setProperty(AppNameUtil.APP_NAME, appName);
        System.setProperty(TransportConfig.CONSOLE_SERVER, sentinelConfigure.getDashboardServer());
        System.setProperty(TransportConfig.SERVER_PORT, String.valueOf(sentinelConfigure.getTokenClientPort()));
        SentinelConfig.setConfig(TransportConfig.SERVER_PORT, String.valueOf(sentinelConfigure.getTokenClientPort()));
    }

    /**
     * 校验配置文件
     *
     * @throws IllegalArgumentException
     */
    protected void validateConfigure() throws IllegalArgumentException {
        Assert.notNull(sentinelConfigure.getDashboardServer(), "[Sentinel] Sentinel property sentinel.dashboard.server can not be null");
        if (!SentinelDataSourceType.APOLLO.equals(sentinelConfigure.getDataSource())) {
            Assert.notNull(sentinelConfigure.getRemoteAddress(), "[Sentinel] Sentinel property sentinel.remote-address can not be null");
        }
        if (SentinelDataSourceType.NACOS.equals(sentinelConfigure.getDataSource())) {
            Assert.notNull(sentinelConfigure.getNacosGroupId(), "[Sentinel] Sentinel property sentinel.group-id can not be null");
        }
        if (SentinelDataSourceType.APOLLO.equals(sentinelConfigure.getDataSource())) {
            Assert.notNull(sentinelConfigure.getApolloNamespace(), "[Sentinel] Sentinel property sentinel.apollo-namespace can not be null");
        }
    }

    /**
     * 初始化Sentinel
     */
    protected void initSentinel() {
        dataSourceInitializer.localInit(sentinelConfigure);
        if (SentinelMode.CLUSTER_SERVER_ALONE.equals(sentinelConfigure.getMode())) {
            dataSourceInitializer.aloneClusterInit(sentinelConfigure);
            dataSourceInitializer.commonInit(sentinelConfigure);
        }
        if (SentinelMode.CLUSTER_EMBEDDED.equals(sentinelConfigure.getMode())) {
            dataSourceInitializer.embeddedClusterInit(sentinelConfigure);
            dataSourceInitializer.commonInit(sentinelConfigure);
        }
    }
}