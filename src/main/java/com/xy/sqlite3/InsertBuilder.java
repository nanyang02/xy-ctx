package com.xy.sqlite3;

import com.alibaba.fastjson.JSON;

import java.util.LinkedList;

/**
 * 实现更新的简单构建
 */
public class InsertBuilder extends AbsSqlBuilder {

    public InsertBuilder(String tableName) {
        index = 1;
        args = new LinkedList<>();
        sql = new StringBuilder("insert into").append(" ").append(tableName).append("(");
        where = new StringBuilder(") values (");
    }

    public InsertBuilder printSql() {
        this.logSql = true;
        return this;
    }

    public static InsertBuilder getInstance(String tableName) {
        return new InsertBuilder(tableName);
    }


    public InsertBuilder insert(String column, Object arg) {
        doInsert(column, arg);
        return this;
    }

    @Override
    public String getPreSql() {
        String psql = sql.toString() + where.toString() + ")";
        if (logSql) logger.info("SQLITE::SQL -> {}, args: {}", psql, JSON.toJSONString(getArgsValues()));
        return psql;
    }

}
