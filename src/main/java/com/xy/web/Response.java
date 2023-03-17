package com.xy.web;

import com.xy.web.core.RequestHolder;
import com.xy.web.core.XyDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/*
  HTTP Response = Status-Line
    *(( general-header | response-header | entity-header ) CRLF)
    CRLF
    [ message-body ]
    Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
*/

public class Response {
    private static final String INDEX_FILE = "index.html";

    private static final Logger logger = LoggerFactory.getLogger(Response.class);

    private static final int BUFFER_SIZE = 1024;
    private OutputStream output;
    private RequestHolder holder;

    enum HttpStatus {
        S200, S301, S302, S500
    }

    public Response(RequestHolder holder, OutputStream output) {
        this.output = output;
        this.holder = holder;
    }

    public OutputStream getOutput() {
        return output;
    }

    public void setOutput(OutputStream output) {
        this.output = output;
    }

    public void sendStaticResource() {
        byte[] bytes = new byte[BUFFER_SIZE];
        FileInputStream fis = null;
        try {
            File file = null;
            if (holder.getPathName().equals("/")) {
                file = new File(XyDispatcher.WEB_ROOT, "/" + INDEX_FILE);
            } else {
                file = new File(XyDispatcher.WEB_ROOT, holder.getPathName());
            }

            // (file.getName());
            // System.out.println(file.exists());
            if (file.exists()) {
                int fileLength = (int) file.length();
                PrintWriter out = new PrintWriter(this.output);

                // search file:: java.home/lib/content-types.properties;
                // but, i search this at jre.home/lib/content-types.properties;
                // so, normal, i can't use this, or else define jre as java home.
                // String contentType = URLConnection.getFileNameMap().getContentTypeFor(file.getName());

                // send HTTP Headers
                out.println("HTTP/1.1 200 OK");
                out.println("Server: Java HTTP Server from SSaurel : 1.0");
                out.println("Date: " + new Date());
                out.println("Content-type: " + MimeType.getMimeWithSuffix(file.getName().substring(file.getName().lastIndexOf("."))));
                out.println("Content-length: " + fileLength);
                out.println(); // blank line between headers and content, very important !
                out.flush(); // flush character output stream buffer

                fis = new FileInputStream(file);
                int ch = fis.read(bytes, 0, BUFFER_SIZE);
                while (ch != -1) {
                    output.write(bytes, 0, ch);
                    ch = fis.read(bytes, 0, BUFFER_SIZE);
                }
            } else {
                // file not found
                String errorMessage = "HTTP/1.1 404 File Not Found\r\n" + "Content-Type: text/html\r\n"
                        + "Content-Length: 23\r\n" + "\r\n" + "<h1>File Not Found</h1>";
                output.write(errorMessage.getBytes());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.warn("文件读取流关闭失败，忽略", e);
                }
            }
        }
    }

    private String newLine() {
//        return java.security.AccessController.doPrivileged(
//                new sun.security.action.GetPropertyAction("line.separator"));
        return "\r\n";
    }

    public void responseJson(String content) {
        responseData(content, false);
    }

    public void responseData(String content, boolean plainText) {
        try {
            byte[] data = content.getBytes(StandardCharsets.UTF_8);
            String contentType = plainText ? "text/plain;charset=UTF-8" : "application/json;charset=UTF-8";
            StringBuilder sb = defaultHeader(HttpStatus.S200)
                    .append("Content-type: ").append(contentType).append(newLine())
                    .append("Content-length: ").append(data.length).append(newLine());
            dyncAppendAndFlush(data, sb);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void dyncAppendAndFlush(byte[] data, StringBuilder sb) throws IOException {
        // 如果有额外的cookie
        if (!holder.getCookie().isEmpty()) {
            sb.append(holder.getCookie().getSetCookieHeader()).append(newLine());
        }

        // 如果有额外的响应头
        if (!holder.getResponseHeader().isEmpty()) {
            sb.append(holder.getResponseHeader().getHeaderStr()).append(newLine());
        }

        // blank line between headers and content, very important !
        sb.append(newLine());
        output.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        output.write(data);
        output.flush();
    }

    public StringBuilder defaultHeader(HttpStatus status) {
        StringBuilder sb = new StringBuilder();
        switch (status) {
            case S200:
                sb.append("HTTP/1.1 200 OK").append(newLine());
                break;
            case S301:
                sb.append("HTTP/1.1 302 Moved Permanently").append(newLine());
                break;
            case S302:
                sb.append("HTTP/1.1 302 Found").append(newLine());
                break;
            case S500:
                sb.append("HTTP/1.1 500 HTTP-Internal Server Error").append(newLine());
                break;
        }

        return sb
                .append("Server: Java HTTP Server 1.0").append(newLine())
                .append("Date: ").append(new Date().toString()).append(newLine())
//                .append("Cache-control: no-cache, no-store, max-age=0").append(new Date().toString()).append(newLine())
                ;
    }

    public void response302(String path) {
        try {
            byte[] data = "ok".getBytes();
            // 301 Moved Permanently 永久重定向 --> 搜索引擎会自动更新链接地址
            // 302 Moved Temporarily 临时重定向 --> 搜索引擎不会自动更新链接地址
            StringBuilder sb = defaultHeader(HttpStatus.S302)
                    .append("Content-type: ").append("text/plain;UTF-8").append(newLine())
                    .append("Content-length: ").append(data.length).append(newLine())
                    .append("Location: ").append(path).append(newLine());

            dyncAppendAndFlush(data, sb);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void response500(String message) {
        try {
            byte[] data = message.getBytes(StandardCharsets.UTF_8);
            StringBuilder sb = defaultHeader(HttpStatus.S500)
                    .append("Content-type: ").append("text/html;UTF-8").append(newLine())
                    .append("Content-length: ").append(data.length).append(newLine())
                    // blank line between headers and content, very important !
                    .append(newLine());
            output.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            output.write(data);
            output.flush();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void responseHtml(String html) {
        try {
            byte[] data = html.getBytes();
            StringBuilder sb = defaultHeader(HttpStatus.S200)
                    .append("Content-type: ").append("text/html;UTF-8").append(newLine())
                    .append("Content-length: ").append(data.length).append(newLine());

            dyncAppendAndFlush(data, sb);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void responseBase(String content) {
        try {
            byte[] data = content.getBytes("UTF-8");
            StringBuilder sb = defaultHeader(HttpStatus.S200)
                    .append("Content-length: ").append(data.length).append(newLine());

            dyncAppendAndFlush(data, sb);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}