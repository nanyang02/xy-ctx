package com.xy.web.filter;

import com.xy.web.Request;
import com.xy.web.RequestMethod;
import com.xy.web.Response;
import com.xy.web.core.MappingDefinition;

import java.io.IOException;

/**
 * Class <code>RequestMethodFilter</code>
 *
 * @author yangnan 2023/4/6 0:13
 * @since 1.8
 */
public class RequestMethodFilter implements Filter {

    private FilterChainFactory factory;

    public RequestMethodFilter setFactory(FilterChainFactory factory) {
        this.factory = factory;
        return this;
    }


    @Override
    public void doFilter(Request req, Response res, FilterChain chain) throws IOException {

        // 此处提供一个过滤请求类型的过滤
        MappingDefinition m = factory.getWebContext().getMapping().getMapping(req.getPathname());
        RequestMethod[] requestMethod = m.getApiMethodFilter();
        if (null != requestMethod) {
            // 检查是否是这个请求的类型
            String reqMethod = req.getRequestParams().getMethod();

            boolean matched = false;
            for (RequestMethod method : requestMethod) {
                matched = matched || method.isMatch(reqMethod);
                if (matched) break;
            }

            if (matched) {
                res.response500("不支持的请求类型：" + reqMethod);

                // 不继续向后流转
                return;
            }
        }

        chain.doFilter(req, res);
    }
}
