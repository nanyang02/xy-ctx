package com.xy.web.filter;

import com.xy.web.Request;
import com.xy.web.Response;

import java.io.IOException;

/**
 * 目的就是启动一下
 *
 * @author yangnan 2023/3/17 19:29
 * @since 1.8
 */
public class UrlFilterChain implements FilterChain {

    private FilterChainFactory factory;
    private int index = -1;

    public UrlFilterChain(FilterChainFactory factory) {
        this.factory = factory;
    }

    @Override
    public void doFilter(Request request, Response response) throws IOException {
        index++;
        factory.getFilter(index).doFilter(request, response, this);
    }
}
