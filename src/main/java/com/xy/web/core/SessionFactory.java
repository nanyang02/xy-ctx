package com.xy.web.core;

import com.xy.web.session.Session;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class <code>SessionFactory</code>
 *
 * @author yangnan 2023/3/17 20:54
 * @since 1.8
 */
public class SessionFactory {

    private int expiredMs = 30 * 60 * 1000;

    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    public Session getSession(String jid) {
        if (null == jid) return null;
        return sessionMap.get(jid);
    }


    public void registerSession(Session session) {
        sessionMap.put(session.getJSessionId(), session);
    }

    private Set<Session> getSessionSet() {
        return new LinkedHashSet<>(sessionMap.values());
    }

    private Iterator<Session> getIterator() {
        return sessionMap.values().iterator();
    }

    public Session createSession(String jSessionId) {
        Session session = new Session(jSessionId);
        session.setExpired(expiredMs);

        // 每次创建的时候gc
        doGc();
        return session;
    }

    public void doGc() {
        Iterator<Session> iterator = getIterator();
        while (iterator.hasNext()) {
            Session next = iterator.next();
            if (next.hasExpired()) {
                sessionMap.remove(next.getJSessionId());
            }
        }
    }

    Thread gcThread = new Thread();

    public boolean hasSessionIfabsentReflush(String jSessionId) {
        Session session = getSession(jSessionId);

        if (session != null) {
            if (session.hasExpired()) {
                sessionMap.remove(jSessionId);
                return false;
            }
            // 重设过期时间
            session.setExpired(expiredMs);
            return true;
        }

        return false;
    }

    public void removeSession(String jSessionId) {
        if (null != jSessionId)
            sessionMap.remove(jSessionId);
    }
}
