package com.xy.service;

import com.xy.context.annotation.Autowired;
import com.xy.stereotype.Service;

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

    @Override
    public void eatApple() {
        System.out.println("eat apple");
        System.out.println("invoke MyService:");
        myService.sayhi();
    }
}
