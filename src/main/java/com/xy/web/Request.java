package com.xy.web;

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {

    private InputStream input;
    private String uri;
    private Map<String, String> args = new HashMap<>();

    public Request(InputStream input) {
        this.input = input;
    }

    public void parse() {
        // Read a set of characters from the socket
        StringBuffer request = new StringBuffer(2048);
        int i;
        byte[] buffer = new byte[2048];
        try {
            i = input.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            i = -1;
        }
        for (int j = 0; j < i; j++) {
            request.append((char) buffer[j]);
        }
        // System.out.print(request.toString());
        uri = parseUri(request.toString());
    }

    private String parseUri(String requestString) {
        int index1, index2;
        String url = null, uri = null;
        index1 = requestString.indexOf(' ');
        if (index1 != -1) {
            index2 = requestString.indexOf(' ', index1 + 1);
            if (index2 > index1) {
                url = requestString.substring(index1 + 1, index2);
            }
        }
        try {
            url = URLDecoder.decode(url, "UTF-8");
            int i = url.indexOf("?");
            if(i!=-1) {
                uri = url.substring(0, i);
                if(url.length() > i+1) {
                    String[] split = url.substring(i + 1).split("&");
                    for (String s : split) {
                        String[] kv = s.split("=");
                        if(kv.length > 1) {
                            args.put(kv[0], kv[1]);
                        } else {
                            args.put(kv[0], "");
                        }
                    }
                }
            } else {
                uri = url;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return uri;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, String> getArgs() {
        return args;
    }
}