package com.xy.config;

import com.xy.ext.DefaultIntValue;
import com.xy.ext.XyCfg;

/**
 * Class <code>RedisConfig</code>
 *
 * @author yangnan 2022/10/26 20:08
 * @since 1.8
 */
public interface RedisConfig extends XyCfg {

    @DefaultIntValue(6379)
    int getPort();
}
