package com.xy.ext.SysTick;

import com.xy.context.ApplicationContext;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class <code>SysTick</code>
 *
 * @author yangnan 2023/3/20 20:29
 * @since 1.8
 */
public class SysTick {

    private Timer timer;
    ApplicationContext ctx;
    private Queue<SysTickTask> queue = new ArrayDeque<>(100);

    /**
     * Register a task
     *
     * @param task
     */
    public void registerTask(SysTickTask task) {
        queue.add(task);
    }

    public SysTick(ApplicationContext appCtx) {
        ctx = appCtx;
        ctx.regSingleton(this);
        timer = new Timer("SysTickTimer");
        queue.add(new IdeTickTask());
        TimerTask task = new TimerTask() {
            public void run() {
                try {
                    while (true) {
                        SysTickTask task = queue.poll();
                        if (null != task) {
                            if (task.getState().ordinal() == SysTockTaskState.Active.ordinal())
                                task.runClk();
                            queue.add(task);
                            if (task.getClass() == IdeTickTask.class) {
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(task, 1000L, 1000L);
    }


}
