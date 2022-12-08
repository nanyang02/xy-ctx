package com.xy.ext;

import com.xy.context.BeanConfigure;
import com.xy.factory.ApplicationDefaultContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 应用上下文,封装好数据源和容器启动初始化的方法调用
 */
@SuppressWarnings("all")
public class MyBatisAppContext extends ApplicationDefaultContext {
    @Override
    public ApplicationDefaultContext getApplicationContext() {
        return this;
    }

    private String[] ds;
    private Object beanConfig;

    private List<SqlSession> sessions = new ArrayList<>();

    public void regSqlSessionFactory(String alias, String xmlFileName) {
        try {
            InputStream resourceAsStream = Resources.getResourceAsStream(xmlFileName);
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(resourceAsStream);

            // the def is off auto commit.
            SqlSession session = factory.openSession(true);
            regProxyBean(session, alias);
            Collection<Class<?>> mappers = session.getConfiguration().getMapperRegistry().getMappers();
            for (Class<?> mapper : mappers) {
                Object m = session.getMapper(mapper);
                Class aClass = (Class) mapper.getGenericSuperclass();
                regProxyBean(m, mapper.getName());
            }
            sessions.add(session);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnectDatasource() {
        for (SqlSession session : sessions) {
            try {
                session.close();
            } catch (Exception ignore) {
            }
        }
    }

    public MyBatisAppContext() {
    }

    public MyBatisAppContext(Class<?> appClass) {
        this(appClass, null);
    }

    public MyBatisAppContext(Class<?> appClass, int port) {
        // 如果有添加扫描注解，就直接扫描和记录bean的记录信息
        scan(appClass);
        // web
        useWeb(appClass, port);
    }



    public MyBatisAppContext(Class<?> appClass, String[] ds) {
        if (null != ds) {
            this.ds = ds;
            this.beanConfig = this;
            registDs();
        }

        // 如果有添加扫描注解，就直接扫描和记录bean的记录信息
        scan(appClass);

        // web
        useWeb(appClass);
    }

    public MyBatisAppContext(Class<?> appClass, String[] ds, int port) {
        if (null != ds) {
            this.ds = ds;
            this.beanConfig = this;
            registDs();
        }
        // 如果有添加扫描注解，就直接扫描和记录bean的记录信息
        scan(appClass);
        // web
        useWeb(appClass, port);
    }

    public MyBatisAppContext(String[] ds) {
        this.ds = ds;
        this.beanConfig = this;
        registDs();
    }

    public MyBatisAppContext(String[] ds, BeanConfigure config) {
        this.ds = ds;
        this.beanConfig = config;
        registDs();
    }

    public boolean registDs() {
        return registDs(ds);
    }

    public boolean registDs(String[] ds) {
        if (ds == null || ds.length % 2 != 0) {
            System.out.println("Datasource Config Not Exists.");
            return true;
        }

        String key = null;
        for (int i = 0; i < ds.length; i++) {
            if (i % 2 != 0) {
                if (hasBean(key)) continue;
                regSqlSessionFactory(key, ds[i]);
            } else {
                key = ds[i];
            }
        }
        return false;
    }

    public boolean hasBean(String key) {
        Object bean = getBean(key);
        return null != bean;
    }

    @Override
    public void close() {
        disconnectDatasource();
    }
}
