package com.xy.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xy.web.annotation.Json;
import com.xy.web.annotation.Var;
import com.xy.web.cookie.Cookie;
import com.xy.web.core.MappingDefinition;
import com.xy.web.header.RequestHeader;
import com.xy.web.header.ResponseHeader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Class <code>WebUtil</code>
 *
 * @author yangnan 2022/11/25 21:00
 * @since 1.8
 */
public class WebUtil {

    /**
     * 完成一行的字符串转换成UTF-8
     *
     * @param line 数据行内容
     * @return 转码后的数据
     */
    public static String contentUseUtf8(String line) {
        byte[] arr = getCharsBytes(line);
        line = new String(arr, StandardCharsets.UTF_8);
        return line;
    }

    /**
     * 字符串转换成字符数组后转换成字节数组
     *
     * @param line 数据行内容
     * @return 字节数据
     */
    public static byte[] getCharsBytes(String line) {
        char[] chars = line.toCharArray();
        byte[] arr = new byte[chars.length];
        for (int k = 0; k < chars.length; k++) {
            arr[k] = (byte) chars[k];
        }
        return arr;
    }


    public static Object parseAnnoJson(Map<String, Object> argsMap, String bodyJson, Json json, Class<?> type) {
        String val = "";
        if (json.value().length() > 0) {
            JSONObject jsonObject = JSON.parseObject(bodyJson);
            Object o = jsonObject.get(json.value());
            if (null != o)
                val = o.toString();
        } else if (json.fromBody()) {
            val = bodyJson;
        } else if (json.fromFormDataParam().trim().length() > 0) {
            Object v = argsMap.get(json.fromFormDataParam());
            if (v instanceof String) {
                val = (String) v;
            }
        }
        if (!"".equals(val)) {
            // check if arr or json
            if (val.contains("{")) {
                try {
                    return JSON.parseObject(val, type);
                } catch (Exception e) {
                    throw new RuntimeException("Json [" + type.getName() + "] Parse Error: " + e.getMessage());
                }
            } else {
                return val;
            }
        }

        return null;
    }

    public static Object parseAnnoVar(Map<String, Object> argsMap, Class<?> type, Var var) {
        if (null != var) {
            Object val = argsMap.get(var.value());
            Supplier<Object> call = toObject(type, val);
            if (null == val) {
                if (var.defVal().length() > 0) return var.defVal();
                else return null;
            } else return call.get();
        }
        return null;
    }

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

    public static Supplier<Object> toObject(Class<?> type, Object o) {
        return () -> {
            if (o.getClass() == type) {
                return o;
            } else {

                if (o instanceof String) {
                    String val = (String) o;

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
                            return null;
                        }
                    }
                } else {
                    return null;
                }
            }
        };
    }

    public static void closeSocket(Socket cli) {
        try {
            if (null != cli && !cli.isClosed()) {
                cli.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void doParseGet(RequestParams instace) throws UnsupportedEncodingException {
        String url = URLDecoder.decode(instace.getPath(), "UTF-8");
        int i = url.indexOf("?");
        if (i != -1) {
            instace.setPath(url.substring(0, i));
            if (url.length() > (i + 1)) {
                String[] kvarr = url.substring(i + 1).split("&");
                for (String s : kvarr) {
                    String[] kv = s.split("=");
                    if (kv.length > 1) {
                        instace.getParams().put(kv[0], kv[1]);
                    } else {
                        instace.getParams().put(kv[0], "");
                    }
                }
            }
        } else {
            instace.setPath(url);
        }
    }

}
