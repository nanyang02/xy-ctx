package com.xy.web.session;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session 技术，用于存储客户端在服务端的信息的
 */
@Data
@Accessors(chain = true)
public class Session {

    public final static String JSESSION_KEY = "JSESSIONID";

    private Map<String, Object> attrMap = new ConcurrentHashMap<>();

    // JSESIIONID
    private String jSessionId;
    // 默认设置为3分钟就失效了
    private long expiredMillis = 2 * 60 * 1000;

    public Session(String jSessionId) {
        if (null == jSessionId || "".equals(jSessionId))
            jSessionId = UUID.randomUUID().toString().replace("-", "").toUpperCase();

        this.jSessionId = jSessionId;
    }

    public boolean hasExpired() {
        if (expiredMillis <= 0) {
            return false;
        } else {
            return expiredMillis - System.currentTimeMillis() <= 0;
        }
    }

    public Session setExpired(long ms) {
        expiredMillis = System.currentTimeMillis() + ms;
        return this;
    }

    public void setAttribute(String name, Object value) {
        if (null != name && value != null) {
            attrMap.put(name, value);
        }
    }

    public Object getAttrubute(String name) {
        if (null == name) return name;
        return attrMap.get(name);
    }

    public void removeAttribute(String name) {
        attrMap.remove(name);
    }

    public List<String> getAttrubuteNames() {
        return new ArrayList<>(attrMap.keySet());
    }

    public void clear() {
        attrMap.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Objects.equals(jSessionId, session.jSessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jSessionId);
    }
}
