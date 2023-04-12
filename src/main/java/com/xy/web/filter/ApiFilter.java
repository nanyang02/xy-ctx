package com.xy.web.filter;

import com.alibaba.fastjson.JSONObject;
import com.xy.web.MsgType;
import com.xy.web.Request;
import com.xy.web.Response;
import com.xy.web.WebUtil;
import com.xy.web.annotation.Json;
import com.xy.web.annotation.Var;
import com.xy.web.cookie.Cookie;
import com.xy.web.core.ApiDefinition;
import com.xy.web.core.MappingDefinition;
import com.xy.web.header.RequestHeader;
import com.xy.web.header.ResponseHeader;
import com.xy.web.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * 这个是最后的处理响应的地方
 *
 * @author yangnan 2023/3/17 23:02
 * @since 1.8
 */
public class ApiFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger("mvc");

    private FilterChainFactory factory;

    public ApiFilter() {
    }

    public ApiFilter setFactory(FilterChainFactory factory) {
        this.factory = factory;
        return this;
    }

    private MappingDefinition getMapping(String mapping) {
        if (null == mapping) return null;
        return factory.getWebContext().getMapping().getMapping(mapping);
    }

    private void registerSession(Session session) {
        factory.getWebContext().registerSession(session);
    }

    @Override
    public void doFilter(Request req, Response res, FilterChain chain) throws IOException {
        String pathname = req.getPathname();

        if (factory.getWebContext().getApisUrl().equals(pathname)) {
            try {
                List<ApiDefinition> apiDefinitions = factory.getWebContext().getMapping().getApiDeinitions();
                res.responseData(JSONObject.toJSONString(apiDefinitions), false);
                return;
            } catch (Exception e) {
                logger.error("apis definition parse fail", e);
                String message = e.getMessage();
                res.response500(message);
                return;
            }
        }


        if (factory.getWebContext().enableDebugLog()) {
            logger.info("API# {}", req.getPathname());
        }

        // 请求的请求头里面如果没有sessionid则创建一个
        if (!(req.getRequestHeader().hasCookie(Session.JSESSION_KEY) || req.getRequestHeader().hasCookie(Session.JSESSION_KEY.toLowerCase()))) {
            Session session = factory.getWebContext().createSession(null);
            req.getCookie().addCookie(Session.JSESSION_KEY, session.getJSessionId());
            registerSession(session);
        }

        MappingDefinition definition = getMapping(pathname);

        // 需要获取方法的参数列表的类型
        Class<?>[] parameterTypes = definition.getMappingMethod().getParameterTypes();
        // 二维数组结构
        Annotation[][] parameterAnnotations = definition.getMappingMethod().getParameterAnnotations();

        boolean userResponse = false;
        Object[] args = new Object[parameterTypes.length];
        Map<String, Object> argsMap = req.getRequestParams().getParams();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            if (parameterAnnotations[i].length > 0) {
                Annotation first = parameterAnnotations[i][0];
                if (first.annotationType() == Json.class) {
                    String bodyJson = req.getRequestParams().getBodyJson();
                    if (null != bodyJson && bodyJson.length() > 0)
                        args[i] = WebUtil.parseAnnoJson(argsMap, bodyJson, (Json) first, type);
                } else if (first.annotationType() == Var.class) {
                    args[i] = WebUtil.parseAnnoVar(argsMap, type, (Var) first);
                }
            } else if (type == Session.class) {
                args[i] = req.getSession();
            } else if (type == Cookie.class) {
                args[i] = req.getCookie();
            } else if (type == RequestHeader.class) {
                args[i] = req.getRequestHeader();
            } else if (type == ResponseHeader.class) {
                args[i] = req.getResponseHeader();
            } else if (type == Request.class) {
                args[i] = req;
            } else if (type == Response.class) {
                args[i] = res;
                userResponse = true;
            }
        }

        Object apply = definition.getCall().apply(args);

        // 如果用户取了输出，那么就由用户自己去实现输出
        if (userResponse) {
            return;
        }

        if (apply == null)
            res.responseData("null", true);
        else {
            boolean isJson = definition.getType().ordinal() == MsgType.JSON.ordinal();
            boolean isHtml = definition.getType().ordinal() == MsgType.HTML.ordinal();
            String resultMessage;
            if (isJson) {
                try {
                    resultMessage = apply instanceof String ? (String) apply : JSONObject.toJSONString(apply);
                } catch (Exception e) {
                    logger.error("json parse fail, data: " + apply.toString(), e);
                    String message = e.getMessage();
                    res.response500(message);
                    return;
                }
            } else if (isHtml) {
                res.responseHtml(apply.toString());
                return;
            } else {
                resultMessage = apply.toString().trim();
                if (resultMessage.startsWith("redirect:")) {
                    // 重定向
                    res.response302(resultMessage.substring(9));
                    return;
                }
            }
            res.responseData(resultMessage, !isJson);
        }
    }
}
