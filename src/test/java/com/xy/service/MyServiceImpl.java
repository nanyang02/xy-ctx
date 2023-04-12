package com.xy.service;

import com.xy.beans.BeansException;
import com.xy.config.RedisConfig;
import com.xy.context.ApplicationContext;
import com.xy.factory.ApplicationContextAware;
import com.xy.stereotype.Service;

/**
 * Class <code>MyService</code>
 *
 * @author yangnan 2022/10/24 10:41
 * @since 1.8
 */
@Service
public class MyServiceImpl implements MyService, ApplicationContextAware {

    private ApplicationContext ctx;

    private RedisConfig redisConfig = new RedisConfig() {
        @Override
        public int getPort() {
            return getInt(RedisConfig.class, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
    };

    @Override
    public void eatApple() {
        System.out.println("eat apple");
        // 可以知道自己方法和自己的信息在哪一行
        StackTraceElement se = Thread.currentThread().getStackTrace()[1];
        System.out.println(se.getMethodName());
        System.out.println(redisConfig.getPort());
    }

    @Override
    public void sayhi() {
        System.out.println("Good Day ToDay");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;

        System.out.println("ctx aware");
    }
}
