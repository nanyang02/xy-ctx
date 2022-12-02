package com.xy.web;

import com.xy.factory.ApplicationDefaultContext;
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
import java.util.*;

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
    private RequestMethod method;

    private static int maxDataLimit = 21 * 1024 * 1024;

    private void setMaxDataLengthLimit(int len) {
        maxDataLimit = len;
    }

    public RequestMethod getMethod() {
        return method;
    }

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
        StringBuilder request = new StringBuilder(maxDataLimit);

        // 1 取出4个字节，判断 \r\n\r\n 就是段结束，或者数据一行结束标志，一个段之前的就是头信息，后续的就是数据
        byte[] rnrn = new byte[4];

        LinkedList<Byte> dataList = new LinkedList<>();

        // 假设一般请求头不会超过100k
        byte[] buffer = new byte[100 * 1024 + 1];
        byte[] headBuffer = null;
        int read, searchHeadercount = 0;
        boolean hadParseHeader = false;
        try {
            // 注意不能循环read，因为客户端不会自动关闭，http还需要使用，所以会出现挂起，
            // 此时需要使用请求头来实现继续读取数据，读取到一定长度就结束
            for (; ; ) {
                read = input.read(buffer);

                if (read > -1) {
                    int p = 0;
                    // 需要将buffer进行拼接，以满足解析请求头的部分，如果上一次有数据，则本次需要拼接
                    if (null != headBuffer) {
                        // 将本次的数据备份一下
                        byte[] bytes = Arrays.copyOf(buffer, read);
                        // 将headBuffer填充到buffer里面
                        System.arraycopy(headBuffer, 0, buffer, 0, headBuffer.length);
                        // 将备份的数据添加再buffer后面
                        System.arraycopy(bytes, 0, buffer, headBuffer.length, read);

                        // 设置起始检查点位置
                        p = headBuffer.length - 1;
                        read = headBuffer.length + read;
                    }
                    for (int j = p; j < read; j++) {
                        if (!hadParseHeader) {
                            rnrn[0] = buffer[j];
                            rnrn[1] = buffer[j + 1];
                            rnrn[2] = buffer[j + 2];
                            rnrn[3] = buffer[j + 3];

                            if (isNewPart(rnrn)) {
                                hadParseHeader = true;
                                for (int i = 0; i < Arrays.copyOf(buffer, j).length; i++) {
                                    char c = (char) buffer[i];
                                    request.append(c);
                                }
                                doParseHeaderPart(requestParams, request.toString());
                                request.delete(0, j);
                                // 跳过header头最后一个的 \n\r\n, 注意j本身就是 \r
                                j = j + 3;

                                if (ApplicationDefaultContext.enabledDebug()) {
                                    logger.debug("已经对请求进行了请求头的解析工作");
                                    logger.debug("Method: {}, DataType: {}, Path: {}"
                                            , requestParams.getMethod()
                                            , requestParams.getType().name()
                                            , pathname);
                                }
                            }
                        } else {
                            // 数据的时候走这里
                            if (dataList.size() > maxDataLimit) throw new RuntimeException(
                                    "Too Long Request Data Length, max length is " + maxDataLimit + " byte");
                            else dataList.addLast(buffer[j]);
                        }
                    }

                    // 如果第一段没有获取到完整的请求头，需要将前一次的数据回收备份
                    if (!hadParseHeader) {
                        headBuffer = Arrays.copyOf(buffer, read);
                    }
                }

                // 没找到头，继续
                if (!hadParseHeader) {
                    if (searchHeadercount > 5) {
                        throw new RuntimeException("解析请求头失败，拒绝响应");
                    }
                    searchHeadercount++;

                    continue;
                }
                // 没有数据，跳过, 有数据，但是数据还不够长度，则继续
                if (null != contentLength && contentLength > 0) {
                    // content length
                    int length = dataList.size();

                    // 如果数据的内容比请求头中定义的长度小则忽略
                    if (length < contentLength) {
                        continue;
                    }
                }

                // 跳出循环，结束请求头和数据的收集工作
                if (ApplicationDefaultContext.enabledDebug())
                    logger.debug("已经解析完毕：path：{}, contentLen:{}, dataSize:{}", pathname, contentLength, dataList.size());
                break;
            }

//            do {
//                read = input.read(buffer);
//                if (read > -1) {
//                    // 读取从缓存中获取到的2k数据
//                    for (int j = 0; j < read; j++) {
//                        if (!hadParseHeader) {
//                            rnrn[0] = buffer[j];
//                            rnrn[1] = buffer[j + 1];
//                            rnrn[2] = buffer[j + 2];
//                            rnrn[3] = buffer[j + 3];
//
//                            if (isNewPart(rnrn)) {
//                                hadParseHeader = true;
//                                doParseHeaderPart(requestParams, request.toString());
//                                request.delete(0, j);
//                                // 跳过header头最后一个的 \n\r\n, 注意j本身就是 \r
//                                j = j + 3;
//                            }
//                            char c = (char) buffer[j];
//                            request.append(c);
//                        } else {
//                            // 数据的时候走这里
//                            if (dataList.size() > maxDataLimit) throw new RuntimeException(
//                                    "Too Long Request Data Length, max length is " + maxDataLimit + " byte");
//                            else dataList.addLast(buffer[j]);
//                        }
//                    }
//                }
//            } while (read == buffer.length);// 这里存在一个风险就是刚刚好满字节，但是出现的可能性及其低，
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
            String bodyContent = getDataStr(request, dataList);
            // 如果没有数据就不用解析了，上面将头解析完成就完成了
            if (bodyContent.length() > 0) {
                doParseData(requestParams, bodyContent);
            }
        }
    }

    private String getDataStr(StringBuilder request, LinkedList<Byte> dataList) {
        Iterator<Byte> it = dataList.iterator();
        while (it.hasNext()) {
            request.append((char) it.next().byteValue());
        }
        return request.toString().trim();
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
                parseFormData(instace, content);
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
        instace.setBodyJson(WebUtil.contentUseUtf8(content));
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

    private void parseFormData(RequestParams instance, String content) {
        String startMark = "--" + instance.getVarSplit();
        String endMark = "--" + instance.getVarSplit() + "--";
        int overOffset = content.lastIndexOf(endMark);
        int markLen = startMark.length();

        // 找到第一个位置
        int offset = content.indexOf(startMark);
        if (offset > -1) {
            do {
                // remove head
                content = content.substring(offset + markLen);
                // remove head len remove
                overOffset = overOffset - (offset + markLen);

                // next head
                offset = content.indexOf(startMark);

                // 解析一段内容
                parseFormDataArg(instance, content.substring(0, offset));
            } while (offset > 0 && offset != overOffset);
        }
    }

    private void parseFormDataArg(RequestParams instance, String pice) {

        // 先将换行和尾部的换行去掉
        String trim = pice.trim();
        int lineEnd = trim.indexOf('\n');
        if (lineEnd > 0) {
            // 读取出属性描述行
            String line = trim.substring(0, lineEnd).trim();
            trim = trim.substring(lineEnd + 1).trim();

            // 跳一行取出属性描述信息，一般就是文件和字段两种哦
            String[] arr = line.split(";");
            // 属性名称
            String name = null, fileName = null;
            boolean isFile = false;
            for (int i = 0; i < arr.length; i++) {
                // param
                if (arr[i].contains("=")) {
                    String[] kv = arr[i].replaceAll("['\"]", "").split("=");
                    String key = kv[0].trim();
                    if ("name".equals(key)) {
                        name = kv.length == 2 ? kv[1].trim() : "";
                    } else if ("filename".equals(key)) {
                        fileName = kv.length == 2 ? kv[1].trim() : "";
                        isFile = true;
                    }
                }
            }

            // 如果是文件，需要读取mime和剩下的数据
            if (isFile) {
                // 新一行的结束位置
                lineEnd = trim.indexOf("\n");
                line = trim.substring(0, lineEnd);
                trim = trim.substring(lineEnd + 1).trim();
                String mime = line.substring(line.lastIndexOf(":") + 1).trim();

                // 文件
                UploadFile uploadFile = new UploadFile();
                uploadFile.setMime(mime);
                uploadFile.setName(fileName);

                // 写数据的部分
                uploadFile.write(trim);

                // 文件列表中
                instance.getFiles().add(uploadFile);
                // 参数列表中
                instance.getParams().put(name, uploadFile);
            }
            // 如果是属性，line里就是数据
            else {
                instance.getParams().put(name, WebUtil.contentUseUtf8(trim));
            }
        }

    }


    private void doParseHeaderPart(RequestParams instace, String headerStr) {

        originHeaderStr = headerStr;
        String[] headerArr = headerStr.split(NEW_LINE_DELI);
        String[] base = headerArr[0].split(" ");
        instace.setMethod(base[0].toUpperCase().trim());
        instace.setPath(base[1].trim());
        // 转换成枚举
        method = RequestMethod.match(instace.getMethod());
        pathname = base[1].trim();
        if (pathname.indexOf("?") > 0) {
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
        Map<String, Session> map = dispacher.getSessionMap();
        Iterator<Session> iterator = map.values().iterator();
        List<String> removeKeys = new ArrayList<>();
        while (iterator.hasNext()) {
            Session next = iterator.next();
            if (next.hasExpired()) {
                removeKeys.add(next.getJSessionId());
            } else {
                if (!hasSession && next.equals(session)) {
                    hasSession = true;
                    // 重设过期时间
                    next.setExpired(30 * 60 * 1000);
                }
            }
        }
        if (removeKeys.size() > 0) {
            for (String removeKey : removeKeys) {
                map.remove(removeKey);
            }
        }
        // 如果没有，则新建一个
        if (!hasSession) {
            // 设置过期时间半个小时
            session.setExpired(30 * 60 * 1000);
            map.put(session.getJSessionId(), session);
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