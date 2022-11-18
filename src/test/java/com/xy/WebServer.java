package com.xy;

import com.xy.context.annotation.Bean;
import com.xy.factory.ApplicationDefaultContext;
import com.xy.stereotype.Component;
import com.xy.stereotype.ComponentScan;
import com.xy.web.annotation.EnableWeb;
import org.junit.Before;
import org.junit.Test;

/**
 * Class <code>WebServer</code>
 *
 * @author yangnan 2022/11/18 10:34
 * @since 1.8
 */
@EnableWeb
@ComponentScan("com.xy.controller")
public class WebServer {

    private ApplicationDefaultContext ctx;

    @Before
    public void before() {
        ctx = new ApplicationDefaultContext();
        ctx.scan(WebServer.class);
        ctx.useWeb(WebServer.class);
    }

    @Test
    public void x() {
        System.out.println("run web server.");

        // waite web server stop
        // ctx.webDispatcherJoin();
        // System.out.println("Test Over");
    }
}
