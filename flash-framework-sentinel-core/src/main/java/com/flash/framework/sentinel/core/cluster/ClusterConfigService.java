package com.flash.framework.sentinel.core.cluster;

/**
 * @author zhurg
 * @date 2019/4/24 - 下午5:50
 */
public interface ClusterConfigService {

    /**
     * 从配置中心获取数据
     *
     * @return
     */
    String getConfig();

    /**
     * 向配置中心推送数据
     *
     * @param data
     * @return
     */
    boolean publishConfig(String data);
}