package com.xy.web.core;

import com.xy.beans.BeanDefine;
import com.xy.factory.ApplicationDefaultContext;
import com.xy.stereotype.Controller;
import com.xy.web.filter.*;
import com.xy.web.session.Session;

import java.util.HashSet;
import java.util.Set;

/**
 * Class <code>WebContext</code>
 *
 * @author yangnan 2023/3/17 20:04
 * @since 1.8
 */
public class WebContext {

    private ApiMapping mapping;

    private SessionFactory sessionFactory;
    private FilterChainFactory filterFactory;
    private XyDispatcher dispatcher;
    private ApplicationDefaultContext defaultContext;

    private String contextPath = "", host = "localhost", webroot = "webroot";
    private int port = 8080;

    public boolean enableDebugLog() {
        return defaultContext.isUseDebug();
    }

    enum Status {
        OFF, RUNNING, INITED
    }

    private Status status = Status.OFF;

    public WebContext(ApplicationDefaultContext ctx) {
        defaultContext = ctx;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void init() {
        if (Status.OFF.ordinal() == status.ordinal()) {
            mapping = new ApiMapping(this);
            filterFactory = new FilterChainFactory(this);
            sessionFactory = new SessionFactory();
            status = Status.INITED;
        }
    }

    public ApiMapping getMapping() {
        return mapping;
    }

    public Session getSession(String jid) {
        init();
        return sessionFactory.getSession(jid);
    }

    public void registerSession(Session session) {
        init();
        sessionFactory.registerSession(session);
    }

    public Session createSession(String jSessionId) {
        init();
        return sessionFactory.createSession(jSessionId);
    }

    public boolean hasSessionIfabsentReflush(String jSessionId) {
        return sessionFactory.hasSessionIfabsentReflush(jSessionId);
    }

    public void removeSession(String jSessionId) {
        sessionFactory.removeSession(jSessionId);
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public void registerFilter(Filter filter) {
        init();
        filterFactory.registerAtLast(filter);
    }

    private void parseController(Object c) {
        init();
        mapping.parseController(c);
    }

    /**
     * 启动web服务的时候，自动的将一些内置的过滤器配置好，添加在请求处理的过程中
     * - 静态资源访问过滤，启动时自动加载最前面
     * - 请求方法的过滤，如果配置的不符，拦截
     * - API的处理过滤，能走到这一步，才算api的请求，然后调用controller层的服务处理层来接受参数进行调用
     */
    public void start() {
        try {
            dispatcher = new XyDispatcher(filterFactory);
            dispatcher.setHost(host);
            dispatcher.setPort(port);
            dispatcher.setWebRoot(webroot);
            // before api, check mapping-api-RequestMethod check
            filterFactory.registerAtLast(new RequestMethodFilter().setFactory(filterFactory));
            // beore start, add api filter to web server
            filterFactory.registerAtLast(new ApiFilter().setFactory(filterFactory));
            // static resource add first
            filterFactory.registerAtFirst(new ResFilter().setFactory(filterFactory));
            status = Status.RUNNING;
            registerControllerMapping();
            dispatcher.start();
            dispatcher.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void registerControllerMapping() {
        Set<Class<?>> controllerClassList = new HashSet<>();

        for (BeanDefine value : defaultContext.getBeanFactory().getDefinitions()) {
            if (value.getTargetClass().getAnnotation(Controller.class) == null) continue;
            boolean add = controllerClassList.add(value.getTargetClass());
            if (add) {
                parseController(defaultContext.getBeanFactory().getBean(value.getTargetClass()));
            }
        }
    }

    public boolean isRunning() {
        return Status.RUNNING.ordinal() == status.ordinal();
    }
}
