package com.xy.web.session;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class Session {

    public final static String JSESSION_KEY = "JSESIIONID";

    // JSESIIONID
    private String jSessionId;
    // 默认设置为3分钟就失效了
    private long expiredMillis = 2 * 60 * 1000;

    public Session() {
        jSessionId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
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
