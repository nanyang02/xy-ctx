package com.xy.web;


import com.alibaba.fastjson.JSON;
import com.xy.web.annotation.Json;
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class <code>WebDispacther</code> 完成请求的接入
 *
 * @author yangnan 2022/11/17 19:35
 * @since 1.8
 */
public class XyDispacher extends Thread {
    private ExecutorService pool = new ThreadPoolExecutor(2, 50,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    private static ServerSocket serverSocket;
    private int port = 8665;

    private Map<String, MappingDefinition> controllerMapping = new ConcurrentHashMap<>();

    public void setPort(int port) {
        this.port = port;
    }

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

    private static final List<String> boolPool = Arrays.asList("true", "1", "on");

    private static Date parseDate(String time) {
        try {
            time = time.replaceAll("[-/年月]", "-");
            time = time.replaceAll("日", "");
            if (time.length() <= 10) {
                return new SimpleDateFormat("yyyy-MM-dd").parse(time);
            } else {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
            }
        } catch (Exception e) {
            return null;
        }
    }

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
                            doHandeApi(request, response);
                        } else {
                            doHandeStaticResource(response);
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

    private void doHandeStaticResource(Response response) throws IOException {
        response.sendStaticResource();
    }

    private void doHandeApi(Request request, Response response) {
        MappingDefinition definition = controllerMapping.get(request.getUri());

        // 需要获取方法的参数列表的类型
        Class<?>[] parameterTypes = definition.getMappingMethod().getParameterTypes();
        Annotation[][] parameterAnnotations = definition.getMappingMethod().getParameterAnnotations();
        Object[] args = new Object[parameterTypes.length];
        Map<String, String> argsMap = request.getRequestParams().getParams();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            Annotation first = parameterAnnotations[i][0];

            // 完成不同参数注解的参数值的注入
            if (first.annotationType() == Json.class) {
                args[i] = parseAnnoJson(request.getRequestParams().getBodyJson(), type);
            } else if (first.annotationType() == Var.class) {
                args[i] = parseAnnoVar(argsMap, type, (Var) first);
            }
        }

        Object apply = definition.getCall().apply(args);
        boolean isPlain = definition.type.ordinal() == RequestMapping.Type.PLAIN.ordinal();
        response.responseJson(apply == null ? null : isPlain ? apply.toString() : JSON.toJSONString(apply), isPlain);
    }

    private <T> T parseAnnoJson(String json, Class<T> type) {
        return JSON.parseObject(json, type);
    }

    private Object parseAnnoVar(Map<String, String> argsMap, Class<?> type, Var var) {
        if (null != var) {
            String val = argsMap.get(var.value());
            Supplier<Object> call = strToObject(type, val);
            if (null == val)
                return null;
            else
                return call.get();
        }
        return null;
    }

    private Supplier<Object> strToObject(Class<?> type, String val) {
        return () -> {
            if (String.class == type) {
                return val;
            } else if (Integer.class == type) {
                return Integer.valueOf(val);
            } else if (Date.class == type) {
                return val.matches("\\d*") ? new Date(Long.parseLong(val)) : parseDate(val);
            } else if (Long.class == type) {
                return Long.parseLong(val);
            } else if (Boolean.class == type) {
                return boolPool.contains(val);
            } else {
                try {
                    return JSON.parseObject(val, type);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
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
