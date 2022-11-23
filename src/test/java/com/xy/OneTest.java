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

        byte[] rnrn = new byte[]{'\r', '\n', '\r', '\n'}; // 刚好形成一个int
        // 4
        System.out.println(isNewPart(rnrn));
    }

    private boolean isNewPart(byte[] rnrn) {
        int dat = 0x0000;
        dat = (dat | rnrn[0] << 12);
        dat = (dat | rnrn[1] << 8);
        dat = (dat | rnrn[2] << 4);
        return (dat | rnrn[3]) == 56026;
    }

    private boolean check(Integer a, Integer b) {
        return b % a != 0;
    }
}
