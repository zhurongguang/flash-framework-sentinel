package com.flash.framework.sentinel.core.cluster;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhurg
 * @date 2019/9/4 - 下午5:10
 */
@Data
public class TokenServer implements Serializable {

    private static final long serialVersionUID = -4569567725820106226L;

    private String host;

    private Integer port = 18730;

    private Integer idleSeconds = 600;
}