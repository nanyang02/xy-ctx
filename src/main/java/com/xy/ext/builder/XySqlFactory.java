package com.xy.ext.builder;


import com.xy.ext.SysTick.SysTick;
import com.xy.ext.database.h2.H2Database;
import com.xy.ext.database.sqlite3.SQLite3;

/**
 * Class <code>XySqlFactory</code>
 *
 * @author yangnan 2023/1/17 16:51
 * @since 1.8
 */
public class XySqlFactory {

    private XyJdbc jdbc;

    public XySqlFactory(DbType dbType, String db) {
        if (DbType.MySQL.ordinal() == dbType.ordinal()) {
            // TODO
        } else if (DbType.SQLite3.ordinal() == dbType.ordinal()) {
            jdbc = new SQLite3(db);
        } else if (DbType.H2.ordinal() == dbType.ordinal()) {
            jdbc = new H2Database(db);
        } else {
            throw new RuntimeException("Un Supply Database Type");
        }
    }

    public XyJdbc getJdbc() {
        return jdbc;
    }

    public void registerAliveTask(SysTick sysTick) {
        sysTick.registerTask(jdbc.getAliveTask());
    }

    public void enableConnectionKeepAlive() {
        jdbc.enableConnectionKeepAlive();
    }

    public int execSql(String sql) {
        return jdbc.execSql(sql);
    }

    public InsertBuilder insertBuilder(String table) {
        return new InsertBuilder(jdbc, table);
    }

    public DeleteBuilder deleteBuilder(String table) {
        return new DeleteBuilder(jdbc, table);
    }

    public UpdateBuilder updateBuilder(String table) {
        return new UpdateBuilder(jdbc, table);
    }

    public SelectBuilder selectBuilder(String table) {
        return new SelectBuilder(jdbc, table, null);
    }

    public SelectBuilder selectBuilder(String table, String alias) {
        return new SelectBuilder(jdbc, table, alias);
    }

}
