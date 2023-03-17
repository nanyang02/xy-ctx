package com.xy.web.filter;

import com.xy.web.core.WebContext;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Class <code>FileChainFactory</code>
 *
 * @author yangnan 2023/3/17 21:15
 * @since 1.8
 */
public class FilterChainFactory {

    private WebContext ctx;

    private LinkedList<Filter> chain = new LinkedList<>();
    private ArrayDeque queue = new ArrayDeque();

    public void register(Filter filter) {
        if (!ctx.isRunning())
            chain.addLast(filter);
    }

    public void register(int index, Filter filter) {
        if (!ctx.isRunning())
            chain.add(index, filter);
    }

    public UrlFilterChain getChain() {
        return new UrlFilterChain(this);
    }

    public FilterChainFactory(WebContext ctx) {
        this.ctx = ctx;
    }

    public WebContext getWebContext() {
        return ctx;
    }

    public Filter getFilter(int index) {
        return chain.get(index);
    }
}
