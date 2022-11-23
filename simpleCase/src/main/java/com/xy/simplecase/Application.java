package com.xy.simplecase;

import com.xy.factory.ApplicationDefaultContext;

public class Application {

    /**
     * 使用main方法启动容器上下文，提供容器服务支持
     * @param args
     */
    public static void main(String[] args) {

        ApplicationDefaultContext ctx = new ApplicationDefaultContext();
        ctx.scan("com.xy.simplecase");
        ctx.useWeb(8080);
        ctx.webDispatcherJoin();
    }


}
