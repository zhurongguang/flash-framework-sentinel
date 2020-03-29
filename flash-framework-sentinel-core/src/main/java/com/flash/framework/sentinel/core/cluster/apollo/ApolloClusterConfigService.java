package com.flash.framework.sentinel.core.cluster.apollo;

import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.flash.framework.sentinel.core.autoconfigure.SentinelConfigure;
import com.flash.framework.sentinel.core.cluster.ClusterConfigService;
import com.flash.framework.sentinel.core.datasource.DataIdBuilder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhurg
 * @date 2019/9/5 - 下午4:12
 */
public class ApolloClusterConfigService implements ClusterConfigService, DataIdBuilder {

    private final SentinelConfigure sentinelConfigure;

    private final ApolloOpenApiClient apolloOpenApiClient;

    private final ApolloConfigure apolloConfigure;


    @Autowired
    public ApolloClusterConfigService(SentinelConfigure sentinelConfigure, ApolloOpenApiClient apolloOpenApiClient, ApolloConfigure apolloConfigure) {
        this.sentinelConfigure = sentinelConfigure;
        this.apolloOpenApiClient = apolloOpenApiClient;
        this.apolloConfigure = apolloConfigure;
    }

    @Override
    public String getConfig() {
        return ConfigService.getConfig(sentinelConfigure.getApolloNamespace()).getProperty(buildDataId(sentinelConfigure.getClusterDataIdSuffix()), null);
    }

    @Override
    public boolean publishConfig(String data) {
        String flowDataId = buildDataId(sentinelConfigure.getClusterDataIdSuffix());
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(flowDataId);
        openItemDTO.setValue(data);
        //openItemDTO.setComment("Program auto-join");
        //openItemDTO.setDataChangeCreatedBy("some-operator");
        apolloOpenApiClient.createOrUpdateItem(apolloConfigure.getAppId(), apolloConfigure.getEnv(), apolloConfigure.getClusterName(), apolloConfigure.getNamespaceName(), openItemDTO);

        // Release configuration
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setEmergencyPublish(true);
        //namespaceReleaseDTO.setReleaseComment("Modify or add configurations");
        //namespaceReleaseDTO.setReleasedBy("some-operator");
        //namespaceReleaseDTO.setReleaseTitle("Modify or add configurations");
        apolloOpenApiClient.publishNamespace(apolloConfigure.getAppId(), apolloConfigure.getEnv(), apolloConfigure.getClusterName(), apolloConfigure.getNamespaceName(), namespaceReleaseDTO);
        return true;
    }
}
