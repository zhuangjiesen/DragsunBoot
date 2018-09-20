package com.dragsun.service;

import com.dragsun.ConsumerApplicationTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @param
 * @Author: zhuangjiesen
 * @Description:
 * @Date: Created in 2018/9/20
 */
public class TestServiceTest extends ConsumerApplicationTests {

    @Autowired
    private TestService testService;
    @Test
    public void testHello() {

        testService.doTest();
    }



}