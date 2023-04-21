package com.xy.web.core;

import com.xy.web.MsgType;
import com.xy.web.RequestMethod;
import com.xy.web.WebUtil;
import com.xy.web.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Class <code>ControllerMapping</code>
 *
 * @author yangnan 2023/3/17 19:57
 * @since 1.8
 */
public class ApiMapping {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private WebContext ctx;

    public ApiMapping(WebContext ctx) {
        this.ctx = ctx;
    }

    private static Map<String, MappingDefinition> mm = new ConcurrentHashMap<>();

    public boolean existsMapping(String mapping) {
        return mm.containsKey(mapping);
    }

    public MappingDefinition getMapping(String mapping) {
        return mm.get(mapping);
    }

    public void register(String mapping, MappingDefinition definition) {
        if (null == mapping) throw new RuntimeException("mapping is null, can't add mappingDefinition");
        if (null == definition) throw new RuntimeException("definition is null, can't add!");
        mm.put(mapping, definition);
    }

    /**
     * 添加一个控制层的映射，自动解析放入到map中
     *
     * @param controller 控制器
     */
    public void parseController(Object controller) {
        Method[] methods = controller.getClass().getMethods();

        // 扩展一下Mapping注解，如果在Controller上添加的注解上有非空串就进行拼接
        Mapping controllerMapping = controller.getClass().getAnnotation(Mapping.class);
        String controllerUri = "";
        if (null != controllerMapping) {
            String m = controllerMapping.value().trim();
            if (m.length() > 0) controllerUri = m;
        }
        for (Method method : methods) {

            Mapping m1 = method.getAnnotation(Mapping.class);
            RequestMapping m2 = method.getAnnotation(RequestMapping.class);
            RestMapping m3 = method.getAnnotation(RestMapping.class);
            ToJson toJson = method.getAnnotation(ToJson.class);
            Api api = method.getAnnotation(Api.class);
            MethodFilter methodFilter = method.getAnnotation(MethodFilter.class);

            if (null == m2 && null == m3 && null == m1) continue;

            /*
             * 定义一个api的基本定义信息，包含了uri，method，controllerClass返回值是不是json
             */
            MappingDefinition definition = new MappingDefinition();
            definition.setCall(args -> {
                try {
                    return method.invoke(controller, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.error("Invoke Method[" + method.getName() + "] fail.", e);
                    throw new RuntimeException(e);
                }
            });
            definition.setMappingMethod(method);
            definition.setControllerClass(controller.getClass());

            if (m1 != null)
                fillUrlMapping(definition, ctx.getContextPath(), controllerUri, m1.value().trim(), m1.type());

            if (m2 != null)
                fillUrlMapping(definition, ctx.getContextPath(), controllerUri, m2.value().trim(), m2.type());

            if (m3 != null)
                fillUrlMapping(definition, ctx.getContextPath(), controllerUri, m3.value().trim(), MsgType.JSON);

            if (toJson != null) {
                definition.setType(MsgType.JSON);
            }

            if (null != methodFilter)
                definition.setApiMethodFilter(methodFilter.value());

            // 补充一些功能 API
            if (null != api) {
                definition.setApiDefinition(new ApiDefinition()
                        .setLabel(api.label())
                        .setDesc(api.desc()).setUrl(definition.getMapping())
                        .setContentType(api.apiContentType())
                        .setHostPort("http://" + ctx.getHostPort())
                        .generalDefArgs(api.args(), definition.getMappingMethod())
                        .generalKvs(api.kvs())
                        .generalDto(api.dtoClass())
                );
                if (null != methodFilter) {
                    definition.getApiDefinition().setAcceptRequestMethods(methodFilter.value());
                } else {
                    definition.getApiDefinition().setAcceptRequestMethods(new RequestMethod[]{RequestMethod.GET, RequestMethod.POST});
                }
            }


            register(definition.getMapping(), definition);
        }
    }

    private void fillUrlMapping(MappingDefinition definition, String ctx, String controller, String method, MsgType type) {
        definition.setMapping(WebUtil.concatPath(WebUtil.concatPath(ctx, controller), method));
        definition.setType(type);
    }

    public List<ApiDefinition> getApiDeinitions() {
        return mm.values().stream().map(MappingDefinition::getApiDefinition).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
