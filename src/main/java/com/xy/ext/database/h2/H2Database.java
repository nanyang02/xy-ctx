package com.xy.ext.database.h2;

import com.xy.ext.SysTick.SysTickTask;
import com.xy.ext.SysTick.SysTickTimeUnit;
import com.xy.ext.SysTick.SysTockTaskState;
import com.xy.ext.builder.DbType;
import com.xy.ext.database.AbsXyJdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class <code>H2Db</code>
 *
 * @author yangnan 2023/3/19 23:16
 * @since 1.8
 */
public class H2Database extends AbsXyJdbc {

    private String dbName;
    private String username;
    private String password;
    private Connection conn;
    private H2ConnectionSysTickTask task;

    public H2Database(String db) {
        dbName = db;
        task = new H2ConnectionSysTickTask(getConn());
        task.setRunClk(SysTickTimeUnit.Second, 60);
    }

    public synchronized Connection getConn() {
        try {
            if (null == conn) {
                conn = DriverManager.getConnection("jdbc:h2:" + dbName, "sa", "");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }

    @Override
    public int execSql(String sql) {
        try {
            Statement statement = getConn().createStatement();
            if (statement.execute(sql)) {
                return 1;
            } else {
                return 0;
            }
        } catch (SQLException ignore) {
        }
        return 0;
    }

    @Override
    public DbType getDbType() {
        return DbType.H2;
    }

    @Override
    public void enableConnectionKeepAlive() {
        task.setState(SysTockTaskState.Active);
    }

    @Override
    public SysTickTask getAliveTask() {
        return task;
    }

}
