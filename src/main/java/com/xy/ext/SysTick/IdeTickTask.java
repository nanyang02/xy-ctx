package com.xy.ext.SysTick;

/**
 * Class <code>IdeTickTask</code>
 *
 * @author yangnan 2023/3/20 21:03
 * @since 1.8
 */
public class IdeTickTask implements SysTickTask {

    private SysTockTaskState sysTockTaskState = SysTockTaskState.Active;

    public SysTockTaskState getState() {
        return sysTockTaskState;
    }

    @Override
    public void setRunClk(SysTickTimeUnit unit, int num) {
    }

    @Override
    public void runClk() {
    }

    @Override
    public void setState(SysTockTaskState state) {

    }
}
