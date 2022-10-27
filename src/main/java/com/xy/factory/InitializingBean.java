package com.xy.factory;

public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}