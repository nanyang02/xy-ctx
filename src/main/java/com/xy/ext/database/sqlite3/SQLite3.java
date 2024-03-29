package com.xy.ext.database.sqlite3;

import com.xy.ext.SysTick.SysTickTask;
import com.xy.ext.SysTick.SysTickTimeUnit;
import com.xy.ext.SysTick.SysTockTaskState;
import com.xy.ext.builder.DbType;
import com.xy.ext.database.AbsXyJdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Class <code>Sqlite3</code>
 *
 * @author yangnan 2022/12/1 10:26
 * @since 1.8
 */
public class SQLite3 extends AbsXyJdbc {

    private String db = "local.db";

    private Connection conn;
    private SqliteConnectionSysTickTask task;

    public SQLite3(String db) {
        try {
            Class.forName("org.sqlite.JDBC");

            if (null != db && !"".equals(db))
                this.db = db;

            task = new SqliteConnectionSysTickTask(getConn());
            task.setRunClk(SysTickTimeUnit.Second, 60);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized Connection getConn() throws SQLException {
        if (null == conn) {
            conn = DriverManager.getConnection("jdbc:sqlite:" + db);
        }
        return conn;
    }

    @Override
    public DbType getDbType() {
        return DbType.SQLite3;
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
