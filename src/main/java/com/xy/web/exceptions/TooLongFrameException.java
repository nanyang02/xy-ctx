package com.xy.web.exceptions;

/**
 * Class <code>TooLongFrameException</code>
 *
 * @author yangnan 2023/4/7 16:14
 * @since 1.8
 */
public class TooLongFrameException extends RuntimeException {

    private String msg;

    public TooLongFrameException(String msg) {
        super();
        this.msg = msg;
    }

}
