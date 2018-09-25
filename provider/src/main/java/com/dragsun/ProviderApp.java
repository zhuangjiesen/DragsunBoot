package com.dragsun;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @param
 * @Author: zhuangjiesen
 * @Description:
 * @Date: Created in 2018/9/20
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.dragsun"})
@EnableWebMvc
@Slf4j
public class ProviderApp extends WebMvcConfigurerAdapter implements ServletContextInitializer, CommandLineRunner {


    public static void main( String[] args )
    {
        SpringApplication.run(ProviderApp.class, args);
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
    }


    @Override
    public void run(String... strings) throws Exception {
        log.debug("CommandLineRunner... run()..... ");
    }
}
