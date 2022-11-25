package com.xy.web.header;

import com.xy.web.annotation.NotNull;
import com.xy.web.cookie.Cookie;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ResponseHeader {

    private Map<String, String> map = new LinkedHashMap<>();

    public ResponseHeader addHeader(@NotNull String name, @NotNull String value) {
        assert null != value;
        map.put(name, value);
        return this;
    }


    public ResponseHeader remove(@NotNull String name) {
        assert null != name;
        map.remove(name);
        return this;
    }

    public ResponseHeader removeAll() {
        map.clear();
        return this;
    }

    public boolean containHeader(String name) {
        assert null != name;
        return map.containsKey(name);
    }

    /**
     * 获取到请求头的串， 一般是 {k}: {v}\r\n 格式为一行
     *
     * @return
     */
    public String getHeaderStr() {
        return map.entrySet().stream()
                .map((e) -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\r\n"));
    }

    public ResponseHeader addToHeader(Map<String, String> header) {
        header.putAll(map);
        return this;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
