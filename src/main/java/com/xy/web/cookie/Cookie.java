package com.xy.web.cookie;

import com.xy.web.annotation.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Cookie {
    private Map<String, String> map = new LinkedHashMap<>();

    public Cookie addCookie(@NotNull String name, @NotNull String value) {
        assert null != value;
        map.put(name, value);
        return this;
    }


    public Cookie remove(@NotNull String name) {
        assert null != name;
        map.remove(name);
        return this;
    }

    public Cookie removeAll() {
        map.clear();
        return this;
    }

    public boolean containCookie(String name) {
        assert null != name;
        return map.containsKey(name);
    }

    private String getCookieStr() {
        return map.entrySet().stream()
                .map((e) -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(";"));
    }

    public String getSetCookieHeader() {
        if (map.isEmpty()) return "";
        return "Set-Cookie: " + getCookieStr();
    }

    public String getCookieHeader() {
        return "Cookie: " + getCookieStr();
    }

    public Cookie addToHeader(Map<String, String> header) {
        header.putAll(map);
        return this;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
