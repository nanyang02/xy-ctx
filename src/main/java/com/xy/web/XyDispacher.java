package com.xy.web;


import com.alibaba.fastjson.JSON;
import com.xy.beans.BeansException;
import com.xy.context.ApplicationContext;
import com.xy.factory.ApplicationContextAware;
import com.xy.factory.ApplicationDefaultContext;
import com.xy.factory.InitializingBean;
import com.xy.web.annotation.RequestMapping;
import com.xy.web.annotation.RestMapping;
import com.xy.web.annotation.Var;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Class <code>WebDispacther</code> 完成请求的接入
 *
 * @author yangnan 2022/11/17 19:35
 * @since 1.8
 */
public class XyDispacher extends Thread {
    private ExecutorService pool = new ThreadPoolExecutor(2, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    private static ServerSocket serverSocket;
    private static int port = 8665;

    private Map<String, MappingDefinition> controllerMapping = new ConcurrentHashMap<>();

    /**
     * WEB_ROOT is the directory where our HTML and other files reside. For this
     * package, WEB_ROOT is the "webroot" directory under the working directory. The
     * working directory is the location in the file system from where the java
     * command was invoked.
     */
    public static String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot";

    // shutdown command
    private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";


    // the shutdown command received
    private static boolean shutdown = false;

    /**
     * 基本思路： 采用单个线程来完成服务接受，然后用线程池来完成服务的响应
     *
     * @param port
     */
    public void runServer(int port) {
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
            System.out.println("Http Server Run At http://localhost:" + port);
            while (!shutdown) {
                final Socket cli = serverSocket.accept();
                // 客户端，由线程池来完成新消息的处理
                pool.submit(() -> {
                    try {
                        // create Request object and parse
                        Request request = new Request(cli.getInputStream());
                        request.parse();

                        // create Response object
                        Response response = new Response(cli.getOutputStream());
                        response.setRequest(request);
                        if (null != request.getUri() && controllerMapping.containsKey(request.getUri())) {
                            MappingDefinition definition = controllerMapping.get(request.getUri());

                            // 需要获取方法的参数列表的类型
                            Class<?>[] parameterTypes = definition.getMappingMethod().getParameterTypes();
                            Annotation[][] parameterAnnotations = definition.getMappingMethod().getParameterAnnotations();
                            Object[] args = new Object[parameterTypes.length];
                            Map<String, String> argsMap = request.getArgs();
                            for (int i = 0; i < parameterTypes.length; i++) {
                                Class<?> type = parameterTypes[i];
                                Annotation[] parameterAnnotation = parameterAnnotations[i];
                                if (parameterAnnotation.length > 0 && parameterAnnotation[0].annotationType() == Var.class) {
                                    Var var = (Var) parameterAnnotation[0];
                                    if (null != var) {
                                        String val = argsMap.get(var.value());
                                        if (null == val)
                                            args[i] = null;
                                        else
                                            args[i] = JSON.parseObject(val, type);
                                    }
                                }
                            }

                            Object apply = definition.getCall().apply(args);
                            boolean isPlain = definition.type.ordinal() == RequestMapping.Type.PLAIN.ordinal();
                            response.responseJson(apply == null ? null : isPlain ? apply.toString() : JSON.toJSONString(apply), isPlain);
                        } else {
                            response.sendStaticResource();
                        }
                        // Close the socket
                        cli.close();

                        // 检查关闭
                        checkShutdown(request);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkShutdown(Request request) {
        // check if the previous URI is a shutdown command
        shutdown = request.getUri().equals(SHUTDOWN_COMMAND);

        if (shutdown) {
            try {
                serverSocket.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * 默认的服务器
     */
    @Override
    public void run() {
        runServer(port);
    }

    static class MappingDefinition {
        private RequestMapping.Type type;
        private String mapping;
        private Class<?> controllerClass;
        private Method mappingMethod;
        private Function<Object[], Object> call;

        public RequestMapping.Type getType() {
            return type;
        }

        public void setType(RequestMapping.Type type) {
            this.type = type;
        }

        public String getMapping() {
            return mapping;
        }

        public void setMapping(String mapping) {
            this.mapping = mapping;
        }

        public Class<?> getControllerClass() {
            return controllerClass;
        }

        public void setControllerClass(Class<?> controllerClass) {
            this.controllerClass = controllerClass;
        }

        public Method getMappingMethod() {
            return mappingMethod;
        }

        public void setMappingMethod(Method mappingMethod) {
            this.mappingMethod = mappingMethod;
        }

        public Function<Object[], Object> getCall() {
            return call;
        }

        public void setCall(Function<Object[], Object> call) {
            this.call = call;
        }
    }

    public <T> void addMapping(Object controller) {
        Method[] methods = controller.getClass().getMethods();
        for (Method method : methods) {

            RequestMapping anno1 = method.getAnnotation(RequestMapping.class);
            RestMapping anno2 = method.getAnnotation(RestMapping.class);

            if (null == anno1 && null == anno2) continue;

            MappingDefinition definition = new MappingDefinition();
            definition.setCall(args -> {
                try {
                    return method.invoke(controller, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    return null;
                }
            });
            definition.setMappingMethod(method);
            definition.setControllerClass(controller.getClass());

            if (anno1 != null) {
                definition.setMapping(anno1.value());
                definition.setType(anno1.type());

            }

            if (anno2 != null) {
                definition.setMapping(anno2.value());
                definition.setType(RequestMapping.Type.JSON);
            }

            controllerMapping.put(definition.getMapping(), definition);
        }
    }
}
