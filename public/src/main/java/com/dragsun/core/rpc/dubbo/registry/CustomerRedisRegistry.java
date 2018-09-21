package com.dragsun.core.rpc.dubbo.registry;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.support.FailbackRegistry;
import com.alibaba.dubbo.rpc.RpcException;
import com.dragsun.core.spring.ApplicationHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.lang.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @param
 * @Author: zhuangjiesen
 * @Description:
 * @Date: Created in 2018/9/19
 */
@Slf4j
public class CustomerRedisRegistry extends FailbackRegistry {

    private final static String DEFAULT_ROOT = "dubbo";

    private final ScheduledExecutorService expireExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("DubboRegistryExpireTimer", true));

    private final ScheduledFuture<?> expireFuture;

    private final String root;

    private final ConcurrentMap<String, Notifier> notifiers = new ConcurrentHashMap<String, Notifier>();

    private final int reconnectPeriod;

    private final int expirePeriod;

    private volatile boolean admin = false;

    private final StringRedisTemplate redisTemplate;

    private final RedisMessageListenerContainer redisMessageListenerContainer;

    public CustomerRedisRegistry(URL url) {
        super(url);
        this.redisTemplate = ApplicationHolder.getApplicationContext().getBean(StringRedisTemplate.class);
        this.redisMessageListenerContainer = ApplicationHolder.getApplicationContext().getBean(RedisMessageListenerContainer.class);


        this.reconnectPeriod = url.getParameter(Constants.REGISTRY_RECONNECT_PERIOD_KEY, Constants.DEFAULT_REGISTRY_RECONNECT_PERIOD);
        String group = url.getParameter(Constants.GROUP_KEY, DEFAULT_ROOT);
        if (! group.startsWith(Constants.PATH_SEPARATOR)) {
            group = Constants.PATH_SEPARATOR + group;
        }
        if (! group.endsWith(Constants.PATH_SEPARATOR)) {
            group = group + Constants.PATH_SEPARATOR;
        }
        this.root = group;

        this.expirePeriod = url.getParameter(Constants.SESSION_TIMEOUT_KEY, Constants.DEFAULT_SESSION_TIMEOUT);
        this.expireFuture = expireExecutor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                    deferExpired(); // 延长过期时间
                } catch (Throwable t) { // 防御性容错
                    logger.error("Unexpected exception occur at defer expire time, cause: " + t.getMessage(), t);
                }
            }
        }, expirePeriod / 2, expirePeriod / 2, TimeUnit.MILLISECONDS);
    }

    private void deferExpired() {

    }

    // 监控中心负责删除过期脏数据
    private void clean() {
        Set<String> keys = redisTemplate.keys(root + Constants.ANY_VALUE);
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                Map<Object, Object> values = redisTemplate.opsForHash().entries(key);
                if (values != null && values.size() > 0) {
                    boolean delete = false;
                    long now = System.currentTimeMillis();
                    for (Map.Entry<Object, Object> entry : values.entrySet()) {
                        URL url = URL.valueOf((String) entry.getKey());
                        if (url.getParameter(Constants.DYNAMIC_KEY, true)) {
                            long expire = Long.parseLong((String) entry.getValue());
                            if (expire < now) {
                                redisTemplate.opsForHash().delete(key, entry.getKey());
                                delete = true;
                                if (logger.isWarnEnabled()) {
                                    logger.warn("Delete expired key: " + key + " -> value: " + entry.getKey() + ", expire: " + new Date(expire) + ", now: " + new Date(now));
                                }
                            }
                        }
                    }
                    if (delete) {
                        redisTemplate.convertAndSend(key , Constants.UNREGISTER);
                    }
                }
            }
        }
    }

    public boolean isAvailable() {
        return !redisTemplate.getConnectionFactory().getConnection().isClosed();
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            expireFuture.cancel(true);
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
        try {
            for (CustomerRedisRegistry.Notifier notifier : notifiers.values()) {
                notifier.shutdown();
            }
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
    }

    @Override
    public void doRegister(URL url) {
        String key = toCategoryPath(url);
        String value = url.toFullString();
        String expire = String.valueOf(System.currentTimeMillis() + expirePeriod);
        log.info(String.format("doRegister - key : %s , value : %s , expire : %s " , key, value, expire));
        boolean success = false;
        RpcException exception = null;
        try {
            redisTemplate.opsForHash().put(key, value, expire);
            //发送订阅事件
            redisTemplate.convertAndSend(key , Constants.REGISTER);
            success = true;
        } catch (Throwable t) {
            log.error(t.getMessage() , t);
            exception = new RpcException("Failed to register service to redis registry. registry: " + key + ", service: " + url + ", cause: " + t.getMessage(), t);
        }
        if (exception != null) {
            if (success) {
                logger.warn(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
    }

    @Override
    public void doUnregister(URL url) {
        String key = toCategoryPath(url);
        String value = url.toFullString();
        log.info(String.format("doRegister - key : %s , value : %s " , key, value));
        RpcException exception = null;
        boolean success = false;
        try {
            redisTemplate.opsForHash().delete(key, value);
            //发送订阅事件
            redisTemplate.convertAndSend(key , Constants.UNREGISTER);
            success = true;
        } catch (Throwable t) {
            log.error(t.getMessage() , t);
            exception = new RpcException("Failed to register service to redis registry. registry: " + key + ", service: " + url + ", cause: " + t.getMessage(), t);
        }
        if (exception != null) {
            if (success) {
                logger.warn(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
    }

    @Override
    public void doSubscribe(final URL url, final NotifyListener listener) {
        String service = toServicePath(url);
        log.info(String.format("doSubscribe - url : %s , service : %s ", url, service));
        Notifier notifier = notifiers.get(service);
        if (notifier == null) {
            Notifier newNotifier = new Notifier(service);
            notifiers.putIfAbsent(service, newNotifier);
            notifier = notifiers.get(service);
            if (notifier == newNotifier) {
                notifier.start();
            }
        }
        boolean success = false;
        RpcException exception = null;
        try {
            if (service.endsWith(Constants.ANY_VALUE)) {
                admin = true;
                Set<String> keys = redisTemplate.keys(service);
                if (keys != null && keys.size() > 0) {
                    Map<String, Set<String>> serviceKeys = new HashMap<String, Set<String>>();
                    for (String key : keys) {
                        String serviceKey = toServicePath(key);
                        Set<String> sk = serviceKeys.get(serviceKey);
                        if (sk == null) {
                            sk = new HashSet<String>();
                            serviceKeys.put(serviceKey, sk);
                        }
                        sk.add(key);
                    }
                    for (Set<String> sk : serviceKeys.values()) {
                        doNotify(sk, url, Arrays.asList(listener));
                    }
                }
            } else {
                doNotify(redisTemplate.keys(service + Constants.PATH_SEPARATOR + Constants.ANY_VALUE), url, Arrays.asList(listener));
            }
            success = true;
        } catch(Throwable t) { // 尝试下一个服务器
            exception = new RpcException("Failed to subscribe service from redis registry. registry: " + redisTemplate.getConnectionFactory().getConnection().toString() + ", service: " + url + ", cause: " + t.getMessage(), t);
        }
        if (exception != null) {
            if (success) {
                logger.warn(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {
    }



    private void doNotify(String key) {
        for (Map.Entry<URL, Set<NotifyListener>> entry : new HashMap<URL, Set<NotifyListener>>(getSubscribed()).entrySet()) {
            this.doNotify(Arrays.asList(key), entry.getKey(), new HashSet<NotifyListener>(entry.getValue()));
        }
    }



    private void doNotify(Collection<String> keys, URL url, Collection<NotifyListener> listeners) {
        if (keys == null || keys.size() == 0
                || listeners == null || listeners.size() == 0) {
            return;
        }
        long now = System.currentTimeMillis();
        List<URL> result = new ArrayList<URL>();
        List<String> categories = Arrays.asList(url.getParameter(Constants.CATEGORY_KEY, new String[0]));
        String consumerService = url.getServiceInterface();
        for (String key : keys) {
            if (! Constants.ANY_VALUE.equals(consumerService)) {
                String prvoiderService = toServiceName(key);
                if (! prvoiderService.equals(consumerService)) {
                    continue;
                }
            }
            String category = toCategoryName(key);
            if (! categories.contains(Constants.ANY_VALUE) && ! categories.contains(category)) {
                continue;
            }
            List<URL> urls = new ArrayList<URL>();
            Map<Object, Object> values = redisTemplate.opsForHash().entries(key);
            if (values != null && !values.isEmpty()) {
                for (Map.Entry<Object, Object> entryObj : values.entrySet()) {
                    String keyItem = (String) entryObj.getKey();
                    String value = (String) entryObj.getValue();
                    URL u = URL.valueOf(keyItem);
                    if (! u.getParameter(Constants.DYNAMIC_KEY, true)
                            || Long.parseLong(value) >= now) {
                        if (UrlUtils.isMatch(url, u)) {
                            urls.add(u);
                        }
                    }
                }
            }
            if (urls.isEmpty()) {
                urls.add(url.setProtocol(Constants.EMPTY_PROTOCOL)
                        .setAddress(Constants.ANYHOST_VALUE)
                        .setPath(toServiceName(key))
                        .addParameter(Constants.CATEGORY_KEY, category));
            }
            result.addAll(urls);
            if (logger.isInfoEnabled()) {
                logger.info("redis notify: " + key + " = " + urls);
            }
        }
        if (result == null || result.size() == 0) {
            return;
        }
        for (NotifyListener listener : listeners) {
            notify(url, listener, result);
        }
    }


    private String toServiceName(String categoryPath) {
        String servicePath = toServicePath(categoryPath);
        return servicePath.startsWith(root) ? servicePath.substring(root.length()) : servicePath;
    }

    private String toCategoryName(String categoryPath) {
        int i = categoryPath.lastIndexOf(Constants.PATH_SEPARATOR);
        return i > 0 ? categoryPath.substring(i + 1) : categoryPath;
    }

    private String toServicePath(String categoryPath) {
        int i;
        if (categoryPath.startsWith(root)) {
            i = categoryPath.indexOf(Constants.PATH_SEPARATOR, root.length());
        } else {
            i = categoryPath.indexOf(Constants.PATH_SEPARATOR);
        }
        return i > 0 ? categoryPath.substring(0, i) : categoryPath;
    }

    private String toServicePath(URL url) {
        return root + url.getServiceInterface();
    }

    private String toCategoryPath(URL url) {
        return toServicePath(url) + Constants.PATH_SEPARATOR + url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
    }


    private class NotifyMessageListener implements MessageListener {

        @Override
        public void onMessage(Message message, @Nullable byte[] pattern) {
            String key = new String(message.getChannel());
            String msg = new String(message.getBody());
            if (logger.isInfoEnabled()) {
                logger.info("redis event: " + key + " = " + msg);
            }
            if (msg.equals(Constants.REGISTER)
                    || msg.equals(Constants.UNREGISTER)) {
                try {
                    doNotify(key);
                } catch (Throwable t) { // TODO 通知失败没有恢复机制保障
                    logger.error(t.getMessage(), t);
                }
            } else {
                logger.info(String.format("NOT REGISTER or UNREGISTER event "));
            }
        }
    }


    private class Notifier extends Thread {

        private final static String THREAD_NAME = "DubboRedisSubscribe-";

        private final String service;

        private volatile boolean first = true;

        private volatile boolean running = true;

        private final AtomicInteger connectSkip = new AtomicInteger();

        private final AtomicInteger connectSkiped = new AtomicInteger();

        private final Random random = new Random();

        private volatile int connectRandom;

        private void resetSkip() {
            connectSkip.set(0);
            connectSkiped.set(0);
            connectRandom = 0;
        }

        private boolean isSkip() {
            int skip = connectSkip.get(); // 跳过次数增长
            if (skip >= 10) { // 如果跳过次数增长超过10，取随机数
                if (connectRandom == 0) {
                    connectRandom = random.nextInt(10);
                }
                skip = 10 + connectRandom;
            }
            if (connectSkiped.getAndIncrement() < skip) { // 检查跳过次数
                return true;
            }
            connectSkip.incrementAndGet();
            connectSkiped.set(0);
            connectRandom = 0;
            return false;
        }

        public Notifier(String service) {
            super.setDaemon(true);
            this.service = service;
            this.setName(THREAD_NAME + this.getId());
        }

        @Override
        public void run() {
            while (running) {
                try {
                    boolean isSkip = isSkip();
                    if (! isSkip) {
                        if (service.endsWith(Constants.ANY_VALUE)) {
                            if (! first) {
                                first = false;
                                Set<String> keys = redisTemplate.keys(service);
                                if (keys != null && keys.size() > 0) {
                                    for (String keyItem : keys) {
                                        doNotify(keyItem);
                                    }
                                }
                                resetSkip();
                            }
                            log.info(String.format("addMessageListener - service : %s " , service));
                            redisMessageListenerContainer.addMessageListener(new NotifyMessageListener() , new ChannelTopic(service));
                        } else {
                            if (! first) {
                                first = false;
                                doNotify(service);
                                resetSkip();
                            }
                            String topic = service + Constants.PATH_SEPARATOR + Constants.ANY_VALUE;
                            log.info(String.format("addMessageListener - topic : %s " , topic));
                            redisMessageListenerContainer.addMessageListener(new NotifyMessageListener() , new PatternTopic(topic));
                        }
                        break;
                    }
                } catch (Throwable t) {
                    logger.error(t.getMessage(), t);
                }
            }
        }

        public void shutdown() {

        }

    }


}
