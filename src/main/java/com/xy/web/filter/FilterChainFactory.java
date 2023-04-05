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

    /**
     * 在最后添加
     *
     * @param filter
     */
    public void registerAtLast(Filter filter) {
        if (!ctx.isRunning())
            chain.addLast(filter);
    }

    /**
     * 在头部添加
     *
     * @param filter
     */
    public void registerAtFirst(Filter filter) {
        if (!ctx.isRunning()) chain.addFirst(filter);
    }

    /**
     * 在指定的位置处插入并占位，后面的如果有，往后移
     *
     * @param index
     * @param filter
     */
    public void register(int index, Filter filter) {
        if (!ctx.isRunning()) chain.add(index, filter);
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
