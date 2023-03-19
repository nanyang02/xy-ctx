package com.xy;

import com.xy.ext.MyBatisAppContext;
import com.xy.factory.ApplicationDefaultContext;
import org.junit.Test;

/**
 * Class <code>WebServer</code>
 *
 * @author yangnan 2022/11/18 10:34
 * @since 1.8
 */
public class WebServer {

    private ApplicationDefaultContext ctx;


    @Test
    public void x() {
        ctx = new MyBatisAppContext();
        ctx.scan("com.xy.controller");
        ctx.enableDebugLog(true);
        ctx.useWeb(8080);
    }
}
