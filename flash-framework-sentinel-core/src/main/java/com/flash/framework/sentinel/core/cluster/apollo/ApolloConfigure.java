package com.flash.framework.sentinel.core.cluster.apollo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zhurg
 * @date 2020/3/3 - 下午6:27
 */
@Data
@ConfigurationProperties(prefix = "sentinel.apollo")
public class ApolloConfigure {

    private String appId;

    private String env = "DEV";

    private String clusterName = "default";

    private String namespaceName = "application";

    private String portalUrl;

    private String token;
}