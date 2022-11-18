package com.xy;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Class <code>Sample</code>
 *
 * @author yangnan 2022/11/18 13:29
 * @since 1.8
 */
public class Sample {

    @Test
    public void aaa() throws UnsupportedEncodingException {
        String req = "https://note.yekjh3.ltd/?nads=%E7%9A%84%E5%93%88%E5%93%88%E5%A4%A7%E8%8B%8F%E6%89%93%E7%88%B1%E7%9A%84%E7%97%95%E8%BF%B9%E5%93%88%E5%B8%88%E5%A4%A7";
        String decode = URLDecoder.decode(req, "UTF-8");
        System.out.println(decode);
    }

}
