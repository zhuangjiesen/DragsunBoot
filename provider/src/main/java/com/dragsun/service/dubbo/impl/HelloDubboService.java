package com.dragsun.service.dubbo.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.dragsun.service.dubbo.IHelloDubboService;
import lombok.extern.slf4j.Slf4j;

/**
 * @param
 * @Author: zhuangjiesen
 * @Description:
 * @Date: Created in 2018/9/20
 */
@Service(
        version = "1.0.0",
        application = "${dubbo.application.id}",
        protocol = "${dubbo.protocol.id}",
        registry = "${dubbo.registry.id}"
)
@Slf4j
public class HelloDubboService implements IHelloDubboService {
    @Override
    public String sayHello(String name) {
        log.debug(String.format(" name : %s " , name));
        return "哈哈哈哈_provider_" + System.currentTimeMillis();
    }
}
