package com.dragsun.controller;

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
@Controller
@RequestMapping("/test")
@Slf4j
public class TestController {

    @ResponseBody
    @RequestMapping("/test")
    public String test(String name) {
        log.info(String.format(" name : %s " , name));
        return "test";
    }




    @ResponseBody
    @RequestMapping("/testError")
    public String testError(String name) {
        log.info(String.format(" name : %s " , name));

        if (true) {
            throw new RuntimeException("iam an exception");
        }

        return "testError";
    }


}
