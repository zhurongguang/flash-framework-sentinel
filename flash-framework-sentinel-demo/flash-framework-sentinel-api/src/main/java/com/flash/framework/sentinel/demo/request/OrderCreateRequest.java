package com.flash.framework.sentinel.demo.request;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author zhurg
 * @date 2020/3/3 - 下午2:44
 */
@Data
public class OrderCreateRequest implements Serializable {

    private static final long serialVersionUID = 8874176718130711674L;

    private String orderNo = UUID.randomUUID().toString().replaceAll("-", "");
}