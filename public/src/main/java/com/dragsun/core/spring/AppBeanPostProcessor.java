package com.dragsun.core.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @param
 * @Author: zhuangjiesen
 * @Description:
 * @Date: Created in 2018/9/21
 */
@Component
@Slf4j
public class AppBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        log.info(String.format("AppBeanPostProcessor beanName : %s , bean : %s " , beanName , bean.getClass().getName()));
        return bean;
    }
}
