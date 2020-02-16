package com.flash.framework.sentinel.core.cluster;

import lombok.Data;

import java.util.Set;

/**
 * @author zhurg
 * @date 2019/4/20 - 下午3:34
 */
@Data
public class ClusterGroup {

    /**
     * token server
     */
    private String machineId;
    /**
     * token server ip
     */
    private String ip;
    /**
     * token server port
     */
    private Integer port;
    /**
     * token server idleSeconds
     */
    private Integer idleSeconds = 600;
    /**
     * token client 集合
     */
    private Set<String> clientSet;
}