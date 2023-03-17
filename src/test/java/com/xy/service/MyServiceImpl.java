package com.xy.service;

import com.xy.beans.BeansException;
import com.xy.context.ApplicationContext;
import com.xy.factory.ApplicationContextAware;
import kz.greetgo.stereotype.Service;

/**
 * Class <code>MyService</code>
 *
 * @author yangnan 2022/10/24 10:41
 * @since 1.8
 */
@Service
public class MyServiceImpl implements MyService, ApplicationContextAware {

    ApplicationContext ctx;

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
