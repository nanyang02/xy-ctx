package com.xy.web.core;

import com.xy.web.MsgType;

import java.lang.reflect.Method;
import java.util.function.Function;

public class MappingDefinition {
    // 消息类型：JSON, PLAIN, HTML
    private MsgType type;
    // 匹配的url
    private String mapping;
    // 所在的controller的字节码文件
    private Class<?> controllerClass;
    // 所对应的方法
    private Method mappingMethod;
    // 对应方法回调函数
    private Function<Object[], Object> call;

    public MsgType getType() {
        return type;
    }

    public void setType(MsgType type) {
        this.type = type;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public Method getMappingMethod() {
        return mappingMethod;
    }

    public void setMappingMethod(Method mappingMethod) {
        this.mappingMethod = mappingMethod;
    }

    public Function<Object[], Object> getCall() {
        return call;
    }

    public void setCall(Function<Object[], Object> call) {
        this.call = call;
    }
}