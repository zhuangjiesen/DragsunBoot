package com.dragsun.core.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @param
 * @Author: zhuangjiesen
 * @Description:
 * @Date: Created in 2018/9/20
 */
@Configuration
@Slf4j
public class RedisChannelConfig {



    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisTemplate redisTemplate){
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisTemplate.getConnectionFactory());
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        //给线程池命名，打日志或者看堆栈信息比较清楚
        executorService.setThreadFactory(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                String name = "redis-listener-" + thread.getId();
                thread.setName(name);
                return thread;
            }
        });
        redisMessageListenerContainer.setTaskExecutor(executorService);
        redisMessageListenerContainer.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message, @Nullable byte[] pattern) {
                log.debug(String.format(" message - Channel : %s , Body : %s " , new String(message.getChannel()) , new String(message.getBody())));
            }
        } , new ChannelTopic("redis-channelTest"));
        return redisMessageListenerContainer;
    }


}
