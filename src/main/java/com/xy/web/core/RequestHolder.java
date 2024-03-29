package com.xy.web.core;

import com.xy.web.Request;
import com.xy.web.Response;
import com.xy.web.cookie.Cookie;
import com.xy.web.exceptions.ParseRequestParamsException;
import com.xy.web.header.ResponseHeader;
import com.xy.web.session.Session;

import java.io.IOException;
import java.net.Socket;

/**
 * Class <code>RequestHolder</code>
 *
 * @author yangnan 2023/3/17 21:30
 * @since 1.8
 */
public class RequestHolder {
    private Request request;
    private Response response;
    private XyDispatcher dispatcher;
    private Session session;

    public String getPathName() {
        return request.getPathname();
    }

    public RequestHolder(XyDispatcher dispatcher, Socket socket) {
        try {
            this.dispatcher = dispatcher;
            request = new Request(this, socket.getInputStream());
            response = new Response(this, socket.getOutputStream());
            try {
                request.parse(dispatcher.getDefAppCtx().isUseOldParse());
            } catch (Exception e) {
                response.response500("500, Inner fault. can't parse request params.");
                throw e;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ParseRequestParamsException();
        }
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public Session getSession(String jSessionId) {
        return dispatcher.getSession(jSessionId);
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

    public void registerSession(Session session) {
        dispatcher.registerSession(session);
    }

    public boolean hasSessionIfAbsentReFlush(String jSessionId) {
        return dispatcher.hasSessionIfAbsentReflush(jSessionId);
    }

    public Session createSession(String jSessionId) {
        return dispatcher.createSession(jSessionId);
    }

    public Cookie getCookie() {
        return request.getCookie();
    }

    public ResponseHeader getResponseHeader() {
        return request.getResponseHeader();
    }

    public String getWebRoot() {
        return dispatcher.getWebRoot();
    }
}
