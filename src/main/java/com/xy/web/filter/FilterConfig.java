package com.xy.web.filter;

import com.xy.web.core.XyDispatcher;

import java.util.Enumeration;

public interface FilterConfig {
    /**
     * 过滤器的名称
     *
     * @return
     */
    String getFilterName();

    /**
     * 拿到核心的dispatcher
     *
     * @return
     */
    XyDispatcher getDispatcher();

    /**
     * 获取到初始化的参数
     *
     * @param var1
     * @return
     */
    String getInitParameter(String var1);

    /**
     * 获取初始化参数得到名称
     *
     * @return
     */
    Enumeration<String> getInitParameterNames();
}
