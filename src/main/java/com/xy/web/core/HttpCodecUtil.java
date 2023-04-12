package com.xy.web.core;

import java.nio.charset.Charset;

public class HttpCodecUtil {
    //space ' '
    public static final byte SP = 32;

    //tab ' '
    public static final byte HT = 9;

    /**
     * Carriage return '\r'
     */
    public static final byte CR = 13;

    /**
     * Equals '='
     */
    public static final byte EQUALS = 61;

    /**
     * Line feed character  '\n'
     */
    public static final byte LF = 10;

    /**
     * carriage return line feed
     */
    public static final byte[] CRLF = new byte[]{CR, LF};

    /**
     * Colon ':'
     */
    public static final byte COLON = 58;

    /**
     * Semicolon ';'
     */
    public static final byte SEMICOLON = 59;

    /**
     * comma ','
     */
    public static final byte COMMA = 44;

    public static final byte DOUBLE_QUOTE = '"';

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    public HttpCodecUtil() {
        super();
    }

}