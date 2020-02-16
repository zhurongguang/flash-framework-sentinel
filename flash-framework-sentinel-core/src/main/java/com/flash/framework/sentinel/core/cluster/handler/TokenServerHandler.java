package com.flash.framework.sentinel.core.cluster.handler;

import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.fastjson.JSON;
import com.flash.framework.sentinel.core.autoconfigure.SentinelConfigure;
import com.flash.framework.sentinel.core.cluster.ClusterConfigService;
import com.flash.framework.sentinel.core.cluster.ClusterGroup;
import com.flash.framework.zookeeper.handler.LeaderHandler;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * Token Server 初始化
 *
 * @author zhurg
 * @date 2019/4/24 - 下午6:05
 */
@Slf4j
public class TokenServerHandler implements LeaderHandler {

    private final SentinelConfigure sentinelConfigure;

    private final ClusterConfigService clusterConfigService;

    public TokenServerHandler(SentinelConfigure sentinelConfigure, ClusterConfigService clusterConfigService) {
        this.clusterConfigService = clusterConfigService;
        this.sentinelConfigure = sentinelConfigure;
    }

    /**
     * 可能是集群刚刚启动，也可能是从standby提升为master
     */
    @Override
    public void leaderHandle() {
        try {
            //获取配置中心配置
            String data = clusterConfigService.getConfig();
            //配置中心无数据则自动初始化
            if (StringUtils.isBlank(data) || CollectionUtils.isEmpty(JSON.parseArray(data, ClusterGroup.class))) {
                ClusterGroup clusterGroup = new ClusterGroup();
                clusterGroup.setIp(HostNameUtil.getIp());
                clusterGroup.setPort(sentinelConfigure.getTokenServerPort());
                clusterGroup.setMachineId(clusterGroup.getIp() + ":" + TransportConfig.getPort());
                clusterConfigService.publishConfig(JSON.toJSONString(Lists.newArrayList(clusterGroup)));
            } else {
                String curr = HostNameUtil.getIp() + ":" + TransportConfig.getPort();
                List<ClusterGroup> clusterGroups = JSON.parseArray(data, ClusterGroup.class);
                Optional<ClusterGroup> optional = clusterGroups.stream()
                        .filter(clusterGroup -> clusterGroup.getMachineId().equals(curr) || clusterGroup.getClientSet().contains(curr))
                        .findFirst();
                if (optional.isPresent()) {
                    ClusterGroup group = optional.get();
                    String oldMaster = group.getMachineId();
                    //从slave切换到master
                    if (!oldMaster.equals(curr)) {
                        group.setMachineId(curr);
                        if (!CollectionUtils.isEmpty(group.getClientSet())) {
                            group.getClientSet().remove(curr);
                            group.getClientSet().add(oldMaster);
                        }
                        clusterConfigService.publishConfig(JSON.toJSONString(group));
                    }
                }
            }
        } catch (Exception e) {
            log.error("[DUBBO] Sentinel TokenServer master election failed ", e);
        }
    }
}