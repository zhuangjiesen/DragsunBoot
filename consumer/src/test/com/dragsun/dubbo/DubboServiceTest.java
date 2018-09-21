package com.dragsun.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.config.annotation.Reference;
import com.dragsun.ConsumerApplicationTests;
import com.dragsun.service.TestService;
import com.dragsun.service.dubbo.IHelloDubboService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;


/**
 * @param
 * @Author: zhuangjiesen
 * @Description:
 * @Date: Created in 2018/9/20
 */
@Slf4j
public class DubboServiceTest extends ConsumerApplicationTests {

    @Reference(version = "1.0.0",
            application = "${dubbo.application.id}",
            loadbalance = "random",
            registry = "${dubbo.registry.id}"
    )
    private IHelloDubboService ihelloDubboService;

    @Test
    public void testHello() {
        log.debug(String.format("DubboServiceTest - testHelloing ..."));
        String result = ihelloDubboService.sayHello("spring_boot say hello ing...");
        log.debug(String.format("ihelloDubboService.sayHello - result : %s " , result));
    }



}
