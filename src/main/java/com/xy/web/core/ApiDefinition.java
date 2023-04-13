package com.xy.web.core;

import com.xy.web.RequestMethod;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
                return null == def ? "string" : def;
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
}
