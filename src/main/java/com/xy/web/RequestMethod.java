package com.xy.web;

/**
 * Class <code>RequestMethod</code>
 *
 * @author yangnan 2022/11/25 18:21
 * @since 1.8
 */
public enum RequestMethod {
    GET, POST, DELETE, PUT;

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

    public boolean isMatch(String name) {
        return this.name().equals(name);
    }
}
