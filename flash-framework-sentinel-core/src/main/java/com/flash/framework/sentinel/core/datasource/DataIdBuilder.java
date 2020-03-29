package com.flash.framework.sentinel.core.datasource;

import com.alibaba.csp.sentinel.util.AppNameUtil;

/**
 * @author zhurg
 * @date 2020/3/3 - 下午2:30
 */
public interface DataIdBuilder {

    /**
     * 构建dataId
     *
     * @param dataId
     * @return
     */
    default String buildDataId(String dataId) {
        return String.format("%s%s", AppNameUtil.getAppName(), dataId);
    }
}