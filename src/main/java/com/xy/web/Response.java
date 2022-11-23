package com.xy.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLConnection;
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
    Request request;
    OutputStream output;

    public Response(OutputStream output) {
        this.output = output;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void sendStaticResource() {
        byte[] bytes = new byte[BUFFER_SIZE];
        FileInputStream fis = null;
        try {
            File file = null;
            if (request.getUri().equals("/")) {
                file = new File(XyDispacher.WEB_ROOT, "/" + INDEX_FILE);
            } else {
                file = new File(XyDispacher.WEB_ROOT, request.getUri());
            }

            // (file.getName());
            // System.out.println(file.exists());
            if (file.exists()) {
                int fileLength = (int) file.length();
                PrintWriter out = new PrintWriter(this.output);

                // send HTTP Headers
                out.println("HTTP/1.1 200 OK");
                out.println("Server: Java HTTP Server from SSaurel : 1.0");
                out.println("Date: " + new Date());
                out.println("Content-type: " + URLConnection.getFileNameMap().getContentTypeFor(file.getName()));
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
        return java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("line.separator"));
    }

    public void responseJson(String content) {
        responseData(content, false);
    }

    public void responseData(String content, boolean plainText) {

        StringBuilder respHeader = new StringBuilder();
        try {
            byte[] data = content.getBytes("UTF-8");
            String contentType = plainText ? "text/html;UTF-8" : "application/json;charset=UTF-8";
            respHeader
                    .append("HTTP/1.1 200 OK").append(newLine())
                    .append("Server: Java HTTP Server from SSaurel : 1.0").append(newLine())
                    .append("Date: ").append(new Date()).append(newLine())
                    .append("Content-type: ").append(contentType).append(newLine())
                    .append("Content-length: ").append(data.length).append(newLine())
                    .append("Cache-control: no-cache, no-store, max-age=0").append(newLine())
                    // blank line between headers and content, very important !
                    .append(newLine())
            ;
            output.write(respHeader.toString().getBytes("UTF-8"));
            output.write(data);
            output.flush();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void resonseError(String message) {
        StringBuilder respHeader = new StringBuilder();
        try {
            byte[] data = message.getBytes("UTF-8");
            respHeader
                    .append("HTTP/1.1 500 Server Error").append(newLine())
                    .append("Server: Java HTTP Server from SSaurel : 1.0").append(newLine())
                    .append("Date: ").append(new Date()).append(newLine())
                    .append("Content-type: ").append("text/html;UTF-8").append(newLine())
                    .append("Content-length: ").append(data.length).append(newLine())
                    .append("Cache-control: no-cache, no-store, max-age=0").append(newLine())
                    // blank line between headers and content, very important !
                    .append(newLine())
            ;
            output.write(respHeader.toString().getBytes("UTF-8"));
            output.write(data);
            output.flush();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    // return supported MIME Types
    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
            return "text/html";
        else
            return "text/plain";
    }
}