package com.xy.ext;

/**
 * Class <code>Maths</code>
 *
 * @author yangnan 2023/2/17 10:19
 * @since 1.8
 */
public class Maths {

    public static enum Parity {
        /**
         * 奇数
         */
        ODD,
        /**
         * 偶数
         */
        EVEN;

        /**
         * 是否匹配
         *
         * @param parity
         * @return
         */
        public boolean match(Parity parity) {
            return this.ordinal() == parity.ordinal();
        }
    }

}
