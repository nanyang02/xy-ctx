package com.xy.web.core;

import com.xy.beans.BeanDefine;
import com.xy.factory.BeanFactory;
import com.xy.web.filter.ApiFilter;
import com.xy.web.filter.Filter;
import com.xy.web.filter.FilterChainFactory;
import com.xy.web.session.Session;
import com.xy.stereotype.Controller;

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

    private String contextPath = "", host = "localhost", webroot = "webroot";
    private int port = 8080;

    enum Status {
        OFF, RUNNING, INITED
    }

    private Status status = Status.OFF;

    public WebContext(BeanFactory beanFactory) {
        Set<Class<?>> controllerClassList = new HashSet<>();

        for (BeanDefine value : beanFactory.getDefinitions()) {
            if (value.getTargetClass().getAnnotation(Controller.class) == null) continue;
            boolean add = controllerClassList.add(value.getTargetClass());
            if (add) {
                parseController(beanFactory.getBean(value.getTargetClass()));
            }
        }
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

    public Session createSession() {
        init();
        return sessionFactory.createSession();
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
        filterFactory.register(filter);
    }

    private void parseController(Object c) {
        init();
        mapping.parseController(c);
    }

    public void start() {
        try {
            dispatcher = new XyDispatcher(filterFactory);
            dispatcher.setHost(host);
            dispatcher.setPort(port);
            dispatcher.setWebRoot(webroot);
            // beore start, add api filter to web server
            filterFactory.register(new ApiFilter().setFactory(filterFactory));
            status = Status.RUNNING;
            dispatcher.start();
            dispatcher.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return Status.RUNNING.ordinal() == status.ordinal();
    }
}
