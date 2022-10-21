package com.xy.beans;

import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Method;
import java.util.Map;

@Data
@Accessors(chain = true)
public class BeanDefine {

    /**
     * 对象的class
     */
    private Class<?> targetClass;
    /**
     * 接口class
     */
    private Class<?> interfaceClass;
    private String simpleName;
    private String name;
    private String alias;

    private boolean isPrototype = false;
    private boolean isCreate = false;
    private boolean isCglib = false;

    // 基于配置@Bean的用于生成Bean需要的两个东西
    private Method createBeanMethod;
    private Object createBeanConfig;


    public BeanDefine() {
    }

    public BeanDefine(Class<?> c) {
        setTargetClass(c);
    }

    public String getAlias() {
        return hasAlias() ? alias : name;
    }

    public BeanDefine setBeanConfig(Object obj) {
        createBeanConfig = obj;
        return this;
    }

    public BeanDefine setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        name = targetClass.getName();
        simpleName = targetClass.getSimpleName();
        return this;
    }

    public boolean isInterface() {
        return null != interfaceClass;
    }

    public boolean hasAlias() {
        return emptyStr(alias);
    }

    public boolean hasTargetAddMapping(Map<String, Object> map) {
        boolean has = null != targetClass && map.containsKey(name);

        // has obj and not alias, add alias -> this.obj
        if (has && hasAlias() && !map.containsKey(alias)) {
            map.put(alias, map.get(name));
        }

        // not interface -> this.obj, add it.
        if (has && isInterface() && !map.containsKey(interfaceClass.getName())) {
            map.put(interfaceClass.getName(), map.get(name));
        }

        return has;
    }

    public void addCache(Map<String, Object> sm, Object bean) {
        sm.put(name, bean);

        // 完成接口和别名的添加
        hasTargetAddMapping(sm);
    }

    public boolean hasName() {
        return emptyStr(name);
    }

    public boolean emptyStr(String s) {
        return null != s && s.length() > 0;
    }
}
