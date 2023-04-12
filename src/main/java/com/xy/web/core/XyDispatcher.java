package com.xy.web.core;


import com.xy.factory.ApplicationDefaultContext;
import com.xy.web.WebUtil;
import com.xy.web.exceptions.ParseRequestParamsException;
import com.xy.web.filter.FilterChainFactory;
import com.xy.web.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class <code>WebDispacther</code> 完成请求的接入
 *
 * @author yangnan 2022/11/17 19:35
 * @since 1.8
 */
public class XyDispatcher extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(XyDispatcher.class);

    private FilterChainFactory fFactory;

    public XyDispatcher(FilterChainFactory filterFactory) {
        fFactory = filterFactory;
    }

    public ApplicationDefaultContext getDefAppCtx() {
        return fFactory.getWebContext().getDefaultContext();
    }

    /**
     * Core Thread - pool for accept client connect server and handle message.
     */
    private static ExecutorService pool = new ThreadPoolExecutor(5, 50,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>());

    private static ServerSocket serverSocket;
    private static int PORT = 8080;
    private static String HOST;
    private static String WEB_PATH;

    public void setPort(int port) {
        PORT = port;
    }

    public void setHost(String host) {
        HOST = host;
    }

    public void setWebRoot(String webRoot) {
        WEB_PATH = webRoot;
    }

    public String getWebRoot() {
        return System.getProperty("user.dir") + File.separator + WEB_PATH;
    }

    public void registerSession(Session session) {
        fFactory.getWebContext().registerSession(session);
    }

    public boolean hasSessionIfAbsentReflush(String jSessionId) {
        boolean noExpired = fFactory.getWebContext().hasSessionIfabsentReflush(jSessionId);
        if (!noExpired) {
            fFactory.getWebContext().removeSession(jSessionId);
        }
        return noExpired;
    }

    public Session createSession(String jSessionId) {
        return fFactory.getWebContext().createSession(jSessionId);
    }

    // shutdown command
    public static final String SHUTDOWN_COMMAND = "/SHUTDOWN";

    // the shutdown command received
    public static boolean shutdown = false;

    private void runServer(int port, String host) {
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName(host));
        } catch (IOException e) {
            throw new RuntimeException("Can't bind port [" + port + "] now, please check any other process on using.", e);
        }
        logger.info("Http Server Run At http://" + HOST + ":" + port);
        while (!shutdown) {
            try {
                submitTask(serverSocket.accept());
            } catch (IOException e) {
                logger.warn("server can't accept client connect now!");
            }
        }

        try {
            logger.info("shutdown work-thread pool now!");
            if (!pool.isShutdown()) pool.shutdown();
            if (!serverSocket.isClosed()) serverSocket.close();
        } catch (IOException ignore) {
            logger.info("server shutdown fail! exit(0)");
        }

        logger.info("-- The End --");
    }

    private void submitTask(final Socket cli) {
        pool.submit(() -> {
            try {
                RequestHolder holder = new RequestHolder(this, cli);

                fFactory.getChain().doFilter(holder.getRequest(), holder.getResponse());

                if (shutdown) {
                    if (!cli.isClosed()) cli.close();
                    serverSocket.close();
                }
            } catch (ParseRequestParamsException e) {
                logger.warn("Client Request Params parse fail!");
            } catch (Exception e) {
                logger.error("server can't accept client connect, it has some error occur, please check error message.", e);
            } finally {
                WebUtil.closeSocket(cli);
            }
        });
    }

    @Override
    public void run() {
        runServer(PORT, HOST);
    }

    public Session getSession(String jSessionId) {
        return fFactory.getWebContext().getSession(jSessionId);
    }
}
