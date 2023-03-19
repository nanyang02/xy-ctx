package com.xy.web.filter;

import com.xy.web.Request;
import com.xy.web.Response;
import com.xy.web.core.MappingDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Class <code>ResFilter</code>
 *
 * @author yangnan 2023/3/19 16:02
 * @since 1.8
 */
public class ResFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger("mvc");

    private FilterChainFactory factory;


    public ResFilter setFactory(FilterChainFactory factory) {
        this.factory = factory;
        return this;
    }

    @Override
    public void doFilter(Request req, Response res, FilterChain chain) throws IOException {
        if (!factory.getWebContext().getMapping().existsMapping(req.getPathname())) {
            if (factory.getWebContext().enableDebugLog()) {
                logger.info("Resource# {}", req.getPathname());
            }
            res.sendStaticResource();
            return;
        }
        chain.doFilter(req, res);
    }
}
