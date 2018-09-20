package com.dragsun.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @param
 * @Author: zhuangjiesen
 * @Description:
 * @Date: Created in 2018/9/20
 */
@Service
@Slf4j
public class TestService {

    public void doTest() {
        log.debug(String.format(" iam testing ...."));
    }
}
