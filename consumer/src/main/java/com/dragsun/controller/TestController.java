package com.dragsun.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dragsun.service.dubbo.IHelloDubboService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @param
 * @Author: zhuangjiesen
 * @Description:
 * @Date: Created in 2018/9/20
 */
@Slf4j
@Controller
@RequestMapping("/test")
public class TestController {


    @Reference(version = "1.0.0",
            application = "${dubbo.application.id}",
            loadbalance = "random",
            registry = "${dubbo.registry.id}"
    )
    private IHelloDubboService ihelloDubboService;


    @ResponseBody
    @RequestMapping("/test/hello")
    public String testHello() {
        log.debug(String.format("DubboServiceTest - testHelloing ..."));
        String result = ihelloDubboService.sayHello("spring_boot say hello ing...");
        log.debug(String.format("ihelloDubboService.sayHello - result : %s " , result));
        return "testHello";
    }


    @ResponseBody
    @RequestMapping("/test")
    public String test() {
        return "test";
    }


}
