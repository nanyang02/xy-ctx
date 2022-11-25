package com.xy.web;


import java.nio.ByteBuffer;

public class UploadFile {

    private ByteBuffer buffer;

    private String mime;

    private String name;

    public synchronized void writeBytes(byte[] bytes) {
        if (null == buffer) {
            buffer = ByteBuffer.allocate(bytes.length);
        }
        buffer.put(bytes);
    }

    public void write(String content) {
        writeBytes(WebUtil.getCharsBytes(content));
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getBytes() {
        return buffer.array();
    }
}
