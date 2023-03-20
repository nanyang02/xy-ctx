package com.xy.ext.SysTick;


public interface SysTickTask {

    /**
     * 定义一个触发任务的时间间隔
     *
     * @param unit 单位
     * @param num  数量
     * @return
     */
    void setRunClk(SysTickTimeUnit unit, int num);

    /**
     * 走时钟
     *
     * @return
     */
    void runClk();

    /**
     * 状态
     *
     * @param state
     */
    void setState(SysTockTaskState state);

    SysTockTaskState getState();

}
