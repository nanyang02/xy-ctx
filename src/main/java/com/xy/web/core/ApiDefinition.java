package com.xy.web.core;

import com.xy.web.ApiContentType;
import com.xy.web.RequestMethod;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;

/**
 * Class <code>ApiDefinition</code>
 *
 * @author yangnan 2023/4/12 23:23
 * @since 1.8
 */
@Data
@Accessors(chain = true)
public class ApiDefinition {

    private String label, desc, url, args, hostPort, defUrlArgs;

    private Map<String, Object> defArgs = new HashMap<>();

    private RequestMethod[] acceptRequestMethods;

    private ApiContentType contentType;

    public ApiDefinition generalDefArgs(String args, Method method) {
        String trim = args.trim();
        this.args = trim;
        if (trim.length() > 0) {
            StringBuilder urlPars = new StringBuilder("?");
            String[] argss = trim.split(";");
            for (String arg : argss) {
                String[] arg_type = arg.split(":");
                if (arg_type.length < 2 || arg_type.length > 3) continue;
                String def = arg_type.length == 3 ? arg_type[2] : null;
                Object defVal = generalValue(arg_type[1], def);
                defArgs.put(arg_type[0], defVal);
                try {
                    urlPars.append(urlPars.length() == 1 ? "" : "&").append(arg_type[0]).append("=").append(
                            defVal == null ? "" :
                                    URLEncoder.encode(defVal.toString(), "UTF-8")
                    );
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            defUrlArgs = urlPars.toString();
        }
        return this;
    }

    private Object generalValue(String dt, String def) {
        switch (dt) {
            case "num":
                return null == def ? 0.0 : Double.parseDouble(def);
            case "int":
                return null == def ? 0 : Integer.parseInt(def);
            case "str":
            case "mv":
                return null == def ? "" : def;
            case "bool":
                return Boolean.parseBoolean(def);
            case "date":
                return null == def ? new Date() : def;
            case "json":
                return null == def ? "{}" : def;
            default:
                return null;
        }
    }

    /**
     * 通过kv的方式来解析数据
     *
     * @param kvs
     * @return
     */
    public ApiDefinition generalKvs(String[] kvs) {
        String k = "";
        for (int i = 0; i < kvs.length; i++) {
            if (i % 2 == 0) {
                k = kvs[i];
            } else {
                String vt = kvs[i];
                String[] vts = vt.split(":");
                String tp = vts.length == 1 ? "mv" : vts[1];
                defArgs.put(k, generalValue(tp, vts[0]));
            }
        }
        return this;
    }

    public ApiDefinition generalDto(Class<?> dtoClass) {
        if (!void.class.getName().equals(dtoClass.getName())) {
            Field[] fields = dtoClass.getDeclaredFields();
            for (Field field : fields) {
                String fdName = field.getType().getName();
                String signature = field.toGenericString();
                String name = dtoClass.getName();
                String prop = signature.substring(signature.indexOf(name) + name.length() + 1);
                boolean isArray = signature.contains("[]");

                Object val = isArray ? new ArrayList<>()
                        : fdName.equals(String.class.getName()) ? generalValue("str", "str")
                        : fdName.equals(Integer.class.getName()) ? generalValue("int", null)
                        : fdName.equals(Long.class.getName()) ? generalValue("int", null)
                        : fdName.equals(Short.class.getName()) ? generalValue("int", null)
                        : fdName.equals(Byte.class.getName()) ? generalValue("int", null)
                        : fdName.equals(Double.class.getName()) ? generalValue("num", null)
                        : fdName.equals(Float.class.getName()) ? generalValue("num", null)
                        : fdName.equals(BigDecimal.class.getName()) ? generalValue("num", null)
                        : fdName.equals(Date.class.getName()) ? generalValue("date", null)
                        : fdName.equals(Boolean.class.getName()) ? generalValue("bool", null)
                        : field.getType().isAssignableFrom(Map.class) ? generalValue("json", null)
                        : field.getType().isAssignableFrom(Collection.class) ? generalValue("json", null)
                        : "{}";

                defArgs.put(prop, val);
            }
        }
        return this;
    }

}
