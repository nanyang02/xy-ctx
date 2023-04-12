package com.xy.web.header;

import com.xy.web.annotation.NotNull;
import com.xy.web.cookie.Cookie;
import com.xy.web.core.HttpHeaders;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestHeader {

    private Map<String, String> map = new LinkedHashMap<>();

    private Cookie cookie = new Cookie();

    public Cookie getCookie() {
        return cookie;
    }

    public boolean hasCookie(String jsessionid) {
        return cookie.containCookie(jsessionid);
    }

    public RequestHeader addHeader(@NotNull String name, @NotNull String value) {
        assert null != value;
        map.put(name, value);
        return this;
    }


    public RequestHeader remove(@NotNull String name) {
        assert null != name;
        map.remove(name);
        return this;
    }

    public RequestHeader removeAll() {
        map.clear();
        return this;
    }

    public boolean containHeader(String name) {
        assert null != name;
        return map.containsKey(name);
    }

    public String getHeader(String name) {
        return map.get(name);
    }

    public List<String> getHeaders(String name) {
        String value = map.get(name);
        List<String> arr = new ArrayList<>();
        arr.add(value);
        return arr;
    }

    public boolean isChunked() {
        List<String> chunked = getHeaders(HttpHeaders.Names.TRANSFER_ENCODING);
        if (chunked.isEmpty()) {
            return false;
        }
        for (String v : chunked) {
            if (v.equalsIgnoreCase(HttpHeaders.Values.CHUNKED)) {
                return true;
            }
        }
        return false;
    }

    public static void validateHeaderName(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c > 127) {
                throw new IllegalArgumentException(
                        "name contains non-ascii character: " + name);
            }

            // Check prohibited characters.
            switch (c) {
                case '\t':
                case '\n':
                case 0x0b:
                case '\f':
                case '\r':
                case ' ':
                case ',':
                case ':':
                case ';':
                case '=':
                    throw new IllegalArgumentException(
                            "name contains one of the following prohibited characters: " +
                                    "=,;: \\t\\r\\n\\v\\f: " + name);
            }
        }
    }

    public static void validateHeaderValue(String value) {
        if (value == null) {
            throw new NullPointerException("value");
        }

        // 0 - the previous character was neither CR nor LF
        // 1 - the previous character was CR
        // 2 - the previous character was LF
        int state = 0;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            // Check the absolutely prohibited characters.
            switch (c) {
                case 0x0b: // Vertical tab
                    throw new IllegalArgumentException(
                            "value contains a prohibited character '\\v': " + value);
                case '\f':
                    throw new IllegalArgumentException(
                            "value contains a prohibited character '\\f': " + value);
            }

            // Check the CRLF (HT | SP) pattern
            switch (state) {
                case 0:
                    switch (c) {
                        case '\r':
                            state = 1;
                            break;
                        case '\n':
                            state = 2;
                            break;
                    }
                    break;
                case 1:
                    switch (c) {
                        case '\n':
                            state = 2;
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    "Only '\\n' is allowed after '\\r': " + value);
                    }
                    break;
                case 2:
                    switch (c) {
                        case '\t':
                        case ' ':
                            state = 0;
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    "Only ' ' and '\\t' are allowed after '\\n': " + value);
                    }
            }
        }

        if (state != 0) {
            throw new IllegalArgumentException(
                    "value must not end with '\\r' or '\\n':" + value);
        }
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

    public RequestHeader addToHeader(Map<String, String> header) {
        header.putAll(map);
        return this;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

}
