package com.xy.web;

import java.nio.charset.StandardCharsets;

/**
 * Class <code>WebUtil</code>
 *
 * @author yangnan 2022/11/25 21:00
 * @since 1.8
 */
public class WebUtil {

    /**
     * 完成一行的字符串转换成UTF-8
     *
     * @param line 数据行内容
     * @return 转码后的数据
     */
    public static String contentUseUtf8(String line) {
        byte[] arr = getCharsBytes(line);
        line = new String(arr, StandardCharsets.UTF_8);
        return line;
    }

    /**
     * 字符串转换成字符数组后转换成字节数组
     *
     * @param line 数据行内容
     * @return 字节数据
     */
    public static byte[] getCharsBytes(String line) {
        char[] chars = line.toCharArray();
        byte[] arr = new byte[chars.length];
        for (int k = 0; k < chars.length; k++) {
            arr[k] = (byte) chars[k];
        }
        return arr;
    }

}
