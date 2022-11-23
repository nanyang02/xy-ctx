package com.xy;

import org.junit.Test;

/**
 * Class <code>OneTest</code>
 *
 * @author yangnan 2022/11/22 17:33
 * @since 1.8
 */
public class OneTest {

    @Test
    public void test() {
        Integer a, b;
        a = 10;
        b = 100;

        // unit test use assert check error or exception
        //assert check(a, b);
    }

    private boolean check(Integer a, Integer b) {
        return b % a != 0;
    }
}
