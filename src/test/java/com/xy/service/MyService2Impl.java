package com.xy.service;

import com.xy.config.RedisConfig;
import com.xy.context.annotation.Autowired;
import kz.greetgo.stereotype.Service;

/**
 * Class <code>MyService2Impl</code>
 *
 * @author yangnan 2022/10/26 18:32
 * @since 1.8
 */
@Service
public class MyService2Impl implements MyService2 {

    @Autowired
    MyService myService;

    RedisConfig redisConfig = new RedisConfig() {
        @Override
        public int getPort() {
            return getInt(RedisConfig.class, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
    };

    @Override
    public void eatApple() {
        System.out.println("eat apple");
        System.out.println("invoke MyService:");
        myService.sayhi();

        // 可以知道自己方法和自己的信息在哪一行
        StackTraceElement se = Thread.currentThread().getStackTrace()[1];
        System.out.println(se.getMethodName());

        System.out.println(redisConfig.getPort());


    }
}
