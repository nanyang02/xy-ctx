package com.xy.web;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xy.web.annotation.Json;
import com.xy.web.annotation.RequestMapping;
import com.xy.web.annotation.RestMapping;
import com.xy.web.annotation.Var;
import com.xy.web.cookie.Cookie;
import com.xy.web.header.RequestHeader;
import com.xy.web.header.ResponseHeader;
import com.xy.web.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
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

    private static final Logger logger = LoggerFactory.getLogger(XyDispacher.class);

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
    public static final String SHUTDOWN_COMMAND = "/SHUTDOWN";

    // the shutdown command received
    public static boolean shutdown = false;

    private static final List<String> boolPool = Arrays.asList("true", "1", "on");

    private final Set<Session> sessionSet = new HashSet<>();

    public Set<Session> getSessionSet() {
        return sessionSet;
    }

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
        } catch (IOException e) {
            throw new RuntimeException("无法绑定服务端口", e);
        }
        logger.info("Http Server Run At http://localhost:" + port);
        while (!shutdown) {
            try {
                submitTask(serverSocket.accept());
            } catch (Exception e) {
                logger.error("无法建立客户端请求", e);
            }
        }

        try {
            logger.info("正在关闭线程池和服务线程");
            if (!pool.isShutdown()) pool.shutdown();
            if (!serverSocket.isClosed()) serverSocket.close();
        } catch (IOException ignore) {
            logger.info("服务关闭出现异常");
        }

        logger.info("-- The End --");
    }

    private void submitTask(Socket cli) {
        final XyDispacher dispacher = this;

        // 客户端，由线程池来完成新消息的处理
        pool.submit(() -> {
            Request request = new Request(dispacher);
            try {
                // create Request object and parse
                request.setInput(cli.getInputStream());
                // create Response object
                final Response response = new Response(cli.getOutputStream());

                response.setRequest(request);
                try {
                    request.parse();
                } catch (Exception e) {
                    response.resonse500("500, Inner fault. can't parse request params.");
                    return;
                }

                if (shutdown) {
                    if (!cli.isClosed()) cli.close();
                    serverSocket.close();
                    return;
                }

                if (null != request.getPathname() && controllerMapping.containsKey(request.getPathname())) {
                    doHandeApi(request, response);
                } else {
                    doHandeStaticResource(response);
                }
            } catch (Exception e) {
                logger.error("无法建立客户端请求", e);
            } finally {
                // Close the socket
                try {
                    if (null != cli && !cli.isClosed()) {
                        cli.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void doHandeStaticResource(Response response) {
        response.sendStaticResource();
    }

    private void doHandeApi(Request request, Response response) {
        MappingDefinition definition = controllerMapping.get(request.getPathname());

        // 需要获取方法的参数列表的类型
        Class<?>[] parameterTypes = definition.getMappingMethod().getParameterTypes();
        // 二维数组结构
        Annotation[][] parameterAnnotations = definition.getMappingMethod().getParameterAnnotations();

        Object[] args = new Object[parameterTypes.length];
        Map<String, String> argsMap = request.getRequestParams().getParams();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            if (parameterAnnotations[i].length > 0) {
                Annotation first = parameterAnnotations[i][0];
                if (first.annotationType() == Json.class) {
                    args[i] = parseAnnoJson(argsMap, request.getRequestParams().getBodyJson(), (Json) first, type);
                } else if (first.annotationType() == Var.class) {
                    args[i] = parseAnnoVar(argsMap, type, (Var) first);
                }
            } else if (type == Cookie.class) {
                args[i] = request.getCookie();
            } else if (type == RequestHeader.class) {
                args[i] = request.getRequestHeader();
            } else if (type == ResponseHeader.class) {
                args[i] = request.getResponseHeader();
            }
        }

        Object apply = definition.getCall().apply(args);

        if (apply == null)
            response.responseData("null", true);
        else {
            boolean isJson = definition.type.ordinal() == RequestMapping.Type.JSON.ordinal();
            String resultMessage;
            if (isJson) {
                try {
                    resultMessage = apply instanceof String ? (String) apply : JSONObject.toJSONString(apply);
                } catch (Exception e) {
                    logger.error("json响应序列化出错", e);
                    String message = e.getMessage();
                    response.resonse500(message);
                    return;
                }
            } else {
                resultMessage = apply.toString().trim();
                if (resultMessage.startsWith("redirect:")) {
                    // 重定向
                    response.response302(resultMessage.substring(9));
                    return;
                }
            }
            response.responseData(resultMessage, !isJson);
        }
    }

    private Object parseAnnoJson(Map<String, String> argsMap, String bodyJson, Json json, Class<?> type) {
        String val = "";
        if (json.fromBody() && bodyJson != null && bodyJson.length() > 0) {
            val = bodyJson;
        } else {
            if (json.value().trim().length() > 0) {
                String valJson = argsMap.get(json.value());
                if (valJson != null && valJson.length() > 0) {
                    val = valJson;
                }
            }
        }
        if (!"".equals(val)) {
            try {
                return JSON.parseObject(val, type);
            } catch (Exception e) {
                logger.warn("Json [{}] Parse Error: {}", type.getName(), e.getMessage());
            }
        }

        return null;
    }

    private Object parseAnnoVar(Map<String, String> argsMap, Class<?> type, Var var) {
        if (null != var) {
            String val = argsMap.get(var.value());
            Supplier<Object> call = strToObject(type, val);
            if (null == val) {
                if (var.defVal().length() > 0) return var.defVal();
                else return null;
            } else return call.get();
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
                    logger.warn("入参[" + val + "]字符串转换对象处理出错", e);
                    return null;
                }
            }
        };
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

    public void addMapping(Object controller) {
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
                    logger.error("Invoke Method[" + method.getName() + "] fail.", e);
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
