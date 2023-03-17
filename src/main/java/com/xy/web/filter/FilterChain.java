package com.xy.web.filter;

import com.xy.web.Request;
import com.xy.web.Response;

import java.io.IOException;

/**
 * Class <code>FilterChain</code>
 *
 * @author yangnan 2023/3/17 19:23
 * @since 1.8
 */
public interface FilterChain {

    void doFilter(Request req, Response res) throws IOException;

}
