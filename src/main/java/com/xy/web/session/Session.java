package com.xy.web.session;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class Session {

    final static String JSESSION_KEY = "JSESIIONID";

    // JSESIIONID
    private String jSessionId;
    private long expiredMillis = 0;

    private Session(String sessionId) {
        if (null != sessionId && !"".equals(sessionId.trim())) {
            jSessionId = sessionId;
        } else {
            jSessionId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        }
    }

    public static Session create() {
        return new Session(null);
    }

    public static Session create(String jSessionId) {
        return new Session(jSessionId);
    }

    public boolean hasExpired() {
        if (expiredMillis <= 0) {
            return false;
        } else {
            return expiredMillis - System.currentTimeMillis() <= 0;
        }
    }

    public Session setExpired(int ms) {
        expiredMillis = System.currentTimeMillis() + ms;
        return this;
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
