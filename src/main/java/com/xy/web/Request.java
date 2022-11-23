package com.xy.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class Request {

    private static final Logger logger = LoggerFactory.getLogger(Request.class);

    private InputStream input;

    private RequestParams requestParams;

    public Request(InputStream input) {
        this.input = input;
    }

    private static boolean isNewPart(byte[] rnrn) {
        int dat = 0x0000;
        dat = (dat | rnrn[0] << 12);
        dat = (dat | rnrn[1] << 8);
        dat = (dat | rnrn[2] << 4);
        return (dat | rnrn[3]) == 56026;
    }

    public void parse() {
        // Read a set of characters from the socket
        StringBuilder request = new StringBuilder(30 * 1024);

        // 此处的读取可以进行解析，很简单

        // 1 取出4个字节，判断 \r\n\r\n 就是段结束，或者数据一行结束标志，一个段之前的就是头信息，后续的就是数据
        byte[] rnrn = new byte[4];

        // 2 循环读取出头（找到第一次出现rnrn的时候，就结束呀） TODO 通过移动4个字节的自动窗口来实现查找功能

        // 3 循环读取，直到限定大小，报错

        // 4 如果数据部分正常，则进行解出数据就好

        // TODO 后续的处理，推荐头和数据分开处理就好。
        // 目前，请求头没有涉及到中文，所以，先按照标准解析，最后做一下 URL解码操作，转换成中文支持


        int i, less;
        byte[] buffer = new byte[30*1024];

        try {
            while ((i = input.read(buffer)) != -1) {
                // 读取从缓存中获取到的2k数据
                for (int j = 0; j < i; j++) {

                    // TODO 需要一套完成比对和判断出需要解析出来的字节是不是换段的逻辑，目的是获取头的数据段

                    char c = (char) buffer[j];
                    request.append(c);

                }
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
            i = -1;
        }


        requestParams = parseParams(request.toString());
    }

    private RequestParams parseParams(String content) {
        RequestParams instace = RequestParams.getInstace();
        instace.setLineSplit("\r\n");

        String[] split = content.split("\r\n");
        String[] base = split[0].split(" ");
        instace.setMethod(base[0].toUpperCase().trim());
        instace.setPath(base[1].trim());
        instace.setHttpVer(base[2].trim());

        int i = 0;
        String line;
        // parse header ingo
        try {
            // 解析第一段，主要是请求头部分
            do {
                line = split[++i];
                doParseHeader(instace, line);

                // 如果没有后续
            } while ((i + 1) < split.length && (split[i+1].length() > 0));
        } catch (Exception e) {
            logger.error("请求头解析出错", e);
        }

        // parse params or body data
        if ("GET".equals(instace.getMethod())) {
            // only parse data
            try {
                doParseGet(instace);
            } catch (Exception e) {
                logger.error("Get 请求解析出错", e);
            }
        } else {
            // 判定后续参数解析的是什么东西
            if (instace.getType().ordinal() == RequestParams.ContentType.JSON.ordinal()) {
                try {
                    parseJson(instace, split, i);
                } catch (Exception e) {
                    logger.error("Post 请求解析Json出错", e);
                }
            } else if (instace.getType().ordinal() == RequestParams.ContentType.FORMDATA.ordinal()) {
                try {
                    parseFormdata(instace, split, i);
                } catch (Exception e) {
                    logger.error("Post 请求解析Formdata出错", e);
                }
            } else if (instace.getType().ordinal() == RequestParams.ContentType.FORM_URLENCODED.ordinal()) {
                try {
                    parseUrlEncoded(instace, split, i);
                } catch (Exception e) {
                    logger.error("Post 请求解析Form-Urlencoded出错", e);
                }
            }
        }

        return instace;
    }

    private void parseJson(RequestParams instace, String[] split, int i) throws UnsupportedEncodingException {
        instace.setBodyJson(concatLast(split, i));
    }

    private void parseUrlEncoded(RequestParams instace, String[] split, int i) throws UnsupportedEncodingException {
        // 注意需要进行编码的解码，中文需要解码
        String str = URLDecoder.decode(concatLast(split, i), "UTF-8");
        // username=22&dsds==32323
        if (str.length() > 0) {
            String[] kvs = str.split("&");
            for (String kv : kvs) {
                String[] ss = kv.split("=");
                String key = ss[0].trim();
                String value = ss.length > 1 ? ss[1].trim() : "";
                instace.getParams().put(key, value);
            }
        }
    }

    private void parseFormdata(RequestParams instace, String[] split, int i) throws UnsupportedEncodingException {
        String line;// 一段段的解析出Post的body的数据
        String overLine = "--" + instace.getVarSplit().trim() + "--";
        do {
            line = split[i].trim();

            // 匹配到参数段,参数段以--起步
            if (line.equals("--" + instace.getVarSplit())) {
                // 跳一行取出属性描述信息，一般就是文件和字段两种哦
                String[] pgs = split[++i].trim().split(";");
                String key = pgs[1].substring(pgs[1].indexOf("\"") + 1, pgs[1].length() - 1);

                // 取出下一行，要么是换行，要么就是文件
                line = split[++i].trim();
                boolean isFile = line.startsWith("Content-Type:");
                if (isFile) {
                    // 暂时不解析文件
                    String fileMime = line.substring(line.indexOf(":") + 2);
                    i = i + 2; // 加一行，再后第2行才是数据
                    line = split[i].trim();
                    // 文件数据已经取出来了
                } else {
                    line = split[++i].trim();
                    // 跳过换行符，取出数据
                    instace.getParams().put(key, contentUseUtf8(line));
                }
            }
        } while (!overLine.equals(split[++i].trim()));
    }

    private String concatLast(String[] split, int i) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (int j = i + 1; j < split.length; j++) {
            String trim = split[j].trim();
            if (trim.length() > 0) {
                sb.append(trim);
            }
        }
        return contentUseUtf8(sb.toString());
    }

    private String contentUseUtf8(String line) throws UnsupportedEncodingException {
        char[] chars = line.toCharArray();
        byte[] arr = new byte[chars.length];
        for (int k = 0; k < chars.length; k++) {
            arr[k] = (byte) chars[k];
        }
        line = new String(arr, StandardCharsets.UTF_8);
        return line;
    }

    private void doParseHeader(RequestParams instace, String line) {
        int i = line.indexOf(":");
        if (i > 0) {
            String headerKey = line.substring(0, i).toLowerCase();
            String headerValue = line.substring(i + 1).trim();
            instace.getHeader().put(headerKey, headerValue);
            if ("content-type".equals(headerKey)) {
                instace.setContentType(headerValue);
                if (headerValue.startsWith("application/json")) {
                    instace.setType(RequestParams.ContentType.JSON);
                } else if (headerValue.startsWith("multipart/form-data")) {
                    instace.setType(RequestParams.ContentType.FORMDATA);
                    int varSplitStart = headerValue.lastIndexOf("=");
                    instace.setVarSplit(headerValue.substring(varSplitStart + 1));
                } else if (headerValue.startsWith("application/x-www-form-urlencoded")) {
                    instace.setType(RequestParams.ContentType.FORM_URLENCODED);
                }
            }
        }
    }

    private void doParseGet(RequestParams instace) throws UnsupportedEncodingException {
        String url = URLDecoder.decode(instace.getPath(), "UTF-8");
        int i = url.indexOf("?");
        if (i != -1) {
            instace.setPath(url.substring(0, i));
            if (url.length() > (i + 1)) {
                String[] kvarr = url.substring(i + 1).split("&");
                for (String s : kvarr) {
                    String[] kv = s.split("=");
                    if (kv.length > 1) {
                        instace.getParams().put(kv[0], kv[1]);
                    } else {
                        instace.getParams().put(kv[0], "");
                    }
                }
            }
        } else {
            instace.setPath(url);
        }
    }

    public RequestParams getRequestParams() {
        return requestParams;
    }

    public String getUri() {
        return null == requestParams ? "/" : requestParams.getPath();
    }
}