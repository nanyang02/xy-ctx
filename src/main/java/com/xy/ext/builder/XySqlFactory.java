package com.xy.ext.builder;

import com.xy.ext.sqlite3.Sqlite3;

/**
 * Class <code>XySqlFactory</code>
 *
 * @author yangnan 2023/1/17 16:51
 * @since 1.8
 */
public class XySqlFactory {

    private XyJdbc jdbc;

    public XySqlFactory(DbType dbType, String db) {

        if (DbType.mysql.ordinal() == dbType.ordinal()) {
            // TODO
        } else if (DbType.sqlite3.ordinal() == dbType.ordinal()) {
            jdbc = new Sqlite3(db);
        } else {
            throw new RuntimeException("Un Supply Database Type");
        }

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
