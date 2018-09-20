package com.dragsun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @param
 * @Author: zhuangjiesen
 * @Description:
 * @Date: Created in 2018/9/20
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.dragsun"})
@EnableWebMvc
public class ConsumerApp extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApp.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ConsumerApp.class);
    }
}
