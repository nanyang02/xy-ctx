package com.xy.web;

import com.xy.web.cookie.Cookie;
import com.xy.web.header.RequestHeader;
import com.xy.web.header.ResponseHeader;
import com.xy.web.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class Request {

    private static final Logger logger = LoggerFactory.getLogger(Request.class);

    private InputStream input;

    private RequestParams requestParams;
    private XyDispacher dispacher;
    private Cookie cookie = new Cookie();
    private RequestHeader requestHeader = new RequestHeader();
    private ResponseHeader responseHeader = new ResponseHeader();
    private String protocolVersion;
    private String pathname;
    private String originHeaderStr;
    private Long contentLength;

    public Long getContentLength() {
        return contentLength;
    }

    public RequestHeader getRequestHeader() {
        return requestHeader;
    }

    public ResponseHeader getResponseHeader() {
        return responseHeader;
    }

    public String getOriginHeaderStr() {
        return originHeaderStr;
    }

    public String getPathname() {
        return pathname;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public Cookie getCookie() {
        return cookie;
    }

    public Request(XyDispacher dispacher) {
        this.dispacher = dispacher;
        this.requestParams = RequestParams.getInstace();
    }

    public void setInput(InputStream input) {
        this.input = input;
    }

    private static final String NEW_LINE_DELI = "\r\n";

    private static boolean isNewPart(byte[] rnrn) {
        int dat = 0x0000;
        dat = (dat | rnrn[0] << 12);
        dat = (dat | rnrn[1] << 8);
        dat = (dat | rnrn[2] << 4);
        return (dat | rnrn[3]) == 56026;
    }

    public void parse() {
        // Read a set of characters from the socket
        // 初始容量设置为 10M 字符容量
        StringBuilder request = new StringBuilder(10 * 1024 * 1024);

        // 1 取出4个字节，判断 \r\n\r\n 就是段结束，或者数据一行结束标志，一个段之前的就是头信息，后续的就是数据
        byte[] rnrn = new byte[4];

        LinkedList<Byte> dataList = new LinkedList<>();

        // 假设一般请求头不会超过100k
        byte[] buffer = new byte[100 * 1024 + 1];
        int read = -1;
        boolean hadParseHeader = false;
        try {
            // 注意不能循环read，因为客户端不会自动关闭，http还需要使用，所以会出现挂起，
            // 此时需要使用请求头来实现继续读取数据，读取到一定长度就结束
            do {
                read = input.read(buffer);
                if (read > -1) {
                    // 读取从缓存中获取到的2k数据
                    for (int j = 0; j < read; j++) {
                        if (!hadParseHeader) {
                            rnrn[0] = buffer[j];
                            rnrn[1] = buffer[j + 1];
                            rnrn[2] = buffer[j + 2];
                            rnrn[3] = buffer[j + 3];

                            if (isNewPart(rnrn)) {
                                hadParseHeader = true;
                                doParseHeaderPart(requestParams, request.toString());
                                request.delete(0, j);
                                // 跳过header头最后一个的 \n\r\n, 注意j本身就是 \r
                                j = j + 3;
                            }
                            char c = (char) buffer[j];
                            request.append(c);
                        } else {
                            // 数据的时候走这里
                            dataList.addLast(buffer[j]);
                        }
                    }
                }
            } while (read == buffer.length);// 这里存在一个风险就是刚刚好满字节，但是出现的可能性及其低，
            // 我们忽略。首先，我们的初始容量需要足够避免出现这样的问题
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }

        if ("GET".equals(requestParams.getMethod())) {
            try {
                doParseGet(requestParams);
            } catch (Exception e) {
                logger.error("Get 请求解析出错", e);
            }
        } else {
            int size = dataList.size();
            logger.info("LinkListSize: " + size + "Byte, Content-Length:" + contentLength + "Byte");

            Iterator<Byte> it = dataList.iterator();
            while (it.hasNext()) {
                request.append((char) it.next().byteValue());
            }
            String bodyContent = request.toString().trim();
            // 如果没有数据就不用解析了，上面将头解析完成就完成了
            if (bodyContent.length() > 0) {
                doParseData(requestParams, bodyContent);
            }
        }
    }

    private void doParseData(RequestParams instace, String content) {
        // 判定后续参数解析的是什么东西
        if (instace.getType().ordinal() == RequestParams.ContentType.JSON.ordinal()) {
            try {
                parseJson(instace, content);
            } catch (Exception e) {
                logger.error("Post 请求解析Json出错", e);
            }
        } else if (instace.getType().ordinal() == RequestParams.ContentType.FORMDATA.ordinal()) {
            try {
                parseFormdata(instace, content);
            } catch (Exception e) {
                logger.error("Post 请求解析Formdata出错", e);
            }
        } else if (instace.getType().ordinal() == RequestParams.ContentType.FORM_URLENCODED.ordinal()) {
            try {
                parseUrlEncoded(instace, content);
            } catch (Exception e) {
                logger.error("Post 请求解析Form-Urlencoded出错", e);
            }
        }
    }

    private void parseJson(RequestParams instace, String content) {
        instace.setBodyJson(content.trim());
    }

    private void parseUrlEncoded(RequestParams instace, String content) throws UnsupportedEncodingException {
        // 注意需要进行编码的解码，中文需要解码
        String str = URLDecoder.decode(content, "UTF-8");
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

    private void parseFormdata(RequestParams instace, String content) {
        String line;// 一段段的解析出Post的body的数据
        String overLine = "--" + instace.getVarSplit().trim() + "--";
        String[] split = content.split(NEW_LINE_DELI);
        String some;
        int i = 0;
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
            // 无需将所有的数据都拿来做比配
            some = split[++i].length() > 255 ? split[i].substring(0, 255) : split[i];
        } while (!overLine.equals(some.trim()));
    }

    private String contentUseUtf8(String line) {
        char[] chars = line.toCharArray();
        byte[] arr = new byte[chars.length];
        for (int k = 0; k < chars.length; k++) {
            arr[k] = (byte) chars[k];
        }
        line = new String(arr, StandardCharsets.UTF_8);
        return line;
    }

    private void doParseHeaderPart(RequestParams instace, String headerStr) {

        originHeaderStr = headerStr;
        String[] headerArr = headerStr.split(NEW_LINE_DELI);
        String[] base = headerArr[0].split(" ");
        instace.setMethod(base[0].toUpperCase().trim());
        instace.setPath(base[1].trim());
        pathname = base[1].trim();
        if(pathname.indexOf("?") > 0) {
            pathname = pathname.substring(0, pathname.indexOf("?"));
        }
        protocolVersion = base[2].trim();

        if (pathname.equals(XyDispacher.SHUTDOWN_COMMAND)) {
            XyDispacher.shutdown = true;
            logger.info("Checked Shutdown Command, Return!");
            return;
        }

        for (int i = 1; i < headerArr.length; i++) {
            doParseHeader(instace, headerArr[i]);
        }

        // 请求的请求头里面如果没有sessionid则创建一个
        if (!requestHeader.hasCookie("JSESSIONID")) {
            Session session = Session.create();
            cookie.addCookie("JSESSIONID", session.getJSessionId());
            dispacher.getSessionSet().add(session);
        }
    }

    private void doParseHeader(RequestParams instace, String line) {
        int i = line.indexOf(":");
        if (i > 0) {
            String headerKey = line.substring(0, i).toLowerCase();
            String headerValue = line.substring(i + 1).trim();
            requestHeader.addHeader(headerKey, headerValue);
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
            } else if ("cookie".equals(headerKey)) {
                Map<String, String> map = stdSplit(headerValue, ";", "=");

                // 将cookie里面的参数对进行解析放入到请求头的Cookie中
                map.forEach((key, value) -> requestHeader.getCookie().addCookie(key, value));

                String jsessionid = map.get("JSESSIONID");
                doCreateSessionIfAbsent(instace, jsessionid);
            } else if ("content-length".equals(headerKey)) {
                contentLength = Long.parseLong(headerValue);
            }
        }
    }

    private Map<String, String> stdSplit(String content, String assign, String joining) {
        Map<String, String> map = new HashMap<>();
        String[] split = content.split(assign);
        for (String s : split) {
            String[] kv = s.split(joining);
            if (kv.length == 1) {
                map.put(kv[0].trim(), "");
            } else {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }

    private void doCreateSessionIfAbsent(RequestParams instace, String jSessionId) {
        // 解析Cooket获取JsessionId
        Session session = Session.create(jSessionId);
        boolean hasSession = false;
        Iterator<Session> iterator = dispacher.getSessionSet().iterator();
        while (iterator.hasNext()) {
            Session next = iterator.next();
            if (next.hasExpired()) {
                dispacher.getSessionSet().remove(next);
            } else {
                if (!hasSession && next.equals(session)) {
                    hasSession = true;
                }
            }
        }
        // 如果没有，则新建一个
        if (!hasSession) {
            session.setExpired(30 * 60 * 1000);
            dispacher.getSessionSet().add(session);
            responseHeader.addHeader("JSESSIONID", session.getJSessionId());
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

}