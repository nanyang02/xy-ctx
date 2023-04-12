package com.xy.ext.SysTick;

/**
 * Class <code>SysTickTimeUnit</code>
 *
 * @author yangnan 2023/3/20 20:41
 * @since 1.8
 */
public enum SysTickTimeUnit {
    Second, Minutes, Hour;

    // by time unit computed it represent how many seconds.
    public int computedToSecond(int num) {
        if (Second.ordinal() == this.ordinal()) {
            return num;
        } else if (Minutes.ordinal() == this.ordinal()) {
            return 60 * num;
        } else if (Hour.ordinal() == this.ordinal()) {
            return 60 * 60 * num;
        } else {
            return 0;
        }
    }
}
