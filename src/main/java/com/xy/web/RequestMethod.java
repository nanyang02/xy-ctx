package com.xy.web;

/**
 * Class <code>RequestMethod</code>
 *
 * @author yangnan 2022/11/25 18:21
 * @since 1.8
 */
public enum RequestMethod {
    GET, POST, HEAD, PUT, OPTIONS, DELETE, TRACE,
    CONNECT, MOVE, PROXY, PRI;

    public static RequestMethod match(String name) {
        return GET.isMatch(name) ? GET
                : POST.isMatch(name) ? POST
                : DELETE.isMatch(name) ? DELETE
                : PUT.isMatch(name) ? PUT
                : null;
    }

    public boolean isMatch(RequestMethod o) {
        return isMatch(o.name());
    }

    public static RequestMethod fromStr(String name) {
        for (RequestMethod value : values()) {
            if (value.name().toLowerCase().equals(name.toLowerCase())) {
                return value;
            }
        }
        return null;
    }

    public boolean isMatch(String name) {
        return this.name().equals(name);
    }
}
