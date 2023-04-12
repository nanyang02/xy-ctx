package com.xy.web.core;

public enum ParseStep {

    SKIP_CONTROL_CHARS, READ_INITIAL, READ_HEADER,
    // 定长
    READ_FIXED_LENGTH_CONTENT,
    // 数据块
    READ_CHUNK_SIZE,
    // 变量
    READ_VARIABLE_LENGTH_CONTENT

}
