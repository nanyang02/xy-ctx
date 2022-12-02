package com.xy.web;

import java.util.HashMap;
import java.util.Map;

/**
 * Class <code>MimeType</code>
 *
 * @author yangnan 2022/11/30 21,46
 * @since 1.8
 */
public class MimeType {

    private static Map<String, String> map = new HashMap<>();

    static {
        map.put(".*", "application/octet-stream");
        map.put(".avi", "video/avi");
        map.put(".bmp", "application/x-bmp");
        map.put(".css", "text/css");
        map.put(".dll", "application/x-msdownload");
        map.put(".doc", "application/msword");
        map.put(".dtd", "text/xml");
        map.put(".dwf", "Model/vnd.dwf");
        map.put(".dwg", "application/x-dwg");
        map.put(".exe", "application/x-msdownload");
        map.put(".gif", "image/gif");
        map.put(".htm", "text/html");
        map.put(".html", "text/html");
        map.put(".ico", "image/x-icon");
        map.put(".img", "application/x-img");
        map.put(".java", "text/plain");
        map.put(".jpeg", "image/jpeg");
        map.put(".jpg", "image/jpeg");
        map.put(".js", "application/x-javascript");
        map.put(".jsp", "text/html");
        map.put(".m3u", "audio/mpegurl");
        map.put(".md", "text/plain");
        map.put(".mp3", "audio/mp3");
        map.put(".mp4", "video/mpeg4");
        map.put(".pdf", "application/pdf");
        map.put(".py", "text/plain");
        map.put(".png", "image/png");
        map.put(".ppt", "application/vnd.ms-powerpoint");
        map.put(".svg", "image/svg+xml");
        map.put(".swf", "application/x-shockwave-flash");
        map.put(".tiff", "image/tiff");
        map.put(".tld", "text/xml");
        map.put(".tsd", "text/xml");
        map.put(".txt", "text/plain");
        map.put(".vml", "text/xml");
        map.put(".vsd", "application/x-vsd");
        map.put(".wav", "audio/wav");
        map.put(".wma", "audio/x-ms-wma");
        map.put(".wsdl", "text/xml");
        map.put(".xhtml", "text/html");
        map.put(".xls", "application/vnd.ms-excel");
        map.put(".xml", "text/xml");
        map.put(".xsd", "text/xml");
        map.put(".conf", "text/plain");
        map.put(".properties", "text/plain");
    }

    public static String getMimeWithSuffix(String suffix) {
        boolean b = map.containsKey(suffix);
        if (b) {
            return map.get(suffix);
        } else {
            return map.get(".*");
        }
    }


}
