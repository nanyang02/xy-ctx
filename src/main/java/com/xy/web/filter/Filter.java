package com.xy.web.filter;

import com.xy.web.Request;
import com.xy.web.Response;

import java.io.IOException;

/**
 * 过滤器
 */
public interface Filter {

    /**
     * 过滤器初始化
     *
     * @param filterConfig
     */
    default void init(FilterConfig filterConfig) {
    }

    /**
     * 进行过滤的操作
     *
     * @param req
     * @param res
     * @param chain
     * @throws IOException
     */
    void doFilter(Request req, Response res, FilterChain chain) throws IOException;

    /**
     * 过滤器的销毁操作
     */
    default void destroy() {
    }
}

