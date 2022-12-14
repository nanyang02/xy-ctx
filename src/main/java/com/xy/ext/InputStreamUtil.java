package com.xy.ext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Class <code>StrInputStream</code>
 *
 * @author yangnan 2022/12/9 0:17
 * @since 1.8
 */
public class InputStreamUtil {

    public static InputStream getInputStream(String content, String charset) {
        InputStream resourceAsStream = null;
        if (content != null) {
            try {
                resourceAsStream = new ByteArrayInputStream(content.getBytes(charset));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return resourceAsStream;
    }

    public static InputStream getInputStreamByUTF8(String content) {
        return getInputStream(content, "UTF-8");
    }

}
