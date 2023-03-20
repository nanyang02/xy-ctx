package com.xy.ext.database.sqlite3;

import com.xy.ext.SysTick.SysTickTask;
import com.xy.ext.SysTick.SysTickTimeUnit;
import com.xy.ext.SysTick.SysTockTaskState;
import com.xy.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class <code>H2ConnectionSysTickTask</code>
 *
 * @author yangnan 2023/3/20 20:49
 * @since 1.8
 */
public class SqliteConnectionSysTickTask implements SysTickTask {
    private int counter = -1, setDefaut = 0;
    private SysTockTaskState state = SysTockTaskState.Disabled;
    private Connection conn;

    public SqliteConnectionSysTickTask(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void setRunClk(SysTickTimeUnit unit, int num) {
        setDefaut = unit.computedToSecond(num);
    }

    void reset() {
        counter = setDefaut;
    }

    @Override
    public void runClk() {
        counter--;
        if (counter <= 0) {
            reset();
            try {
                // 每5分钟，触发一次数据库的连接，避免connection掉线，失活
                conn.createStatement().executeQuery("select 1");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setState(SysTockTaskState state) {
        this.state = state;
    }

    @Override
    public SysTockTaskState getState() {
        return state;
    }

}
