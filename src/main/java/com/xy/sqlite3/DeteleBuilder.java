package com.xy.sqlite3;

import com.alibaba.fastjson.JSON;

import java.util.LinkedList;

/**
 * 实现更新的简单构建
 */
public class DeteleBuilder extends AbsSqlBuilder {

    public DeteleBuilder(String tableName) {
        index = 1;
        args = new LinkedList<>();
        sql = new StringBuilder("delete from").append(" ").append(tableName).append(" ");
        where = new StringBuilder();
    }

    public DeteleBuilder printSql() {
        this.logSql = true;
        return this;
    }

    public static DeteleBuilder getInstance(String tableName) {
        return new DeteleBuilder(tableName);
    }


    public DeteleBuilder where(String column, Object arg) {
        doWhere(column, arg);
        return this;
    }

    public DeteleBuilder where(String sql) {
        doWhereSingle(sql);
        return this;
    }

    public DeteleBuilder byId(Object arg) {
        return where("id", arg);
    }


    @Override
    public String getPreSql() {
        String psql = sql.toString() + where.toString();
        if (logSql) logger.info("SQLITE::SQL -> {}, args: {}", psql, JSON.toJSONString(getArgsValues()));
        return psql;
    }
}
