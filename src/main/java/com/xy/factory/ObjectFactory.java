package com.xy.factory;

import com.xy.beans.BeansException;

public interface ObjectFactory<T> {
    T getObject() throws BeansException;
}
