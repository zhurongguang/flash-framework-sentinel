package com.flash.framework.sentinel.core.cluster;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhurg
 * @date 2019/9/4 - 下午5:11
 */
@Data
public class TokenClient implements Serializable {

    private static final long serialVersionUID = 5401278646448092598L;

    private String host;

    private Integer port = 8720;

    private Integer requestTimeout = 1000;
}