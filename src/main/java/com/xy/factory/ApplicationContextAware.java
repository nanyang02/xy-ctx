package com.xy.factory;

import com.xy.beans.BeansException;
import com.xy.context.ApplicationContext;

public interface ApplicationContextAware extends Aware {
    /**
     * 注入容器上下文
     *
     * @param applicationContext ctx
     * @throws BeansException
     */
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException;
}