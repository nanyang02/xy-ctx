package com.xy.sqlite3;

import com.alibaba.fastjson.JSON;

import java.util.LinkedList;

/**
 * 实现更新的简单构建
 */
public class UpdateBuilder extends AbsSqlBuilder {

    public UpdateBuilder printSql() {
        this.logSql = true;
        return this;
    }

    public UpdateBuilder(String tableName) {
        index = 1;
        args = new LinkedList<>();
        sql = new StringBuilder("update").append(" ").append(tableName).append(" ");
        where = new StringBuilder();
    }

    public static UpdateBuilder getInstance(String tableName) {
        return new UpdateBuilder(tableName);
    }

    public UpdateBuilder update(String column, Object arg) {
        doUpdate(column, arg);
        return this;
    }

    public UpdateBuilder updateIgNull(String column, Object arg) {
        if (null == arg) return this;
        return update(column, arg);
    }

    public UpdateBuilder where(String column, Object arg) {
        doWhere(column, arg);
        return this;
    }

    public UpdateBuilder where(String sql) {
        doWhereSingle(sql);
        return this;
    }

    public UpdateBuilder byId(Object arg) {
        return where("id", arg);
    }


    @Override
    public String getPreSql() {
        String psql = sql.toString() + where.toString();
        if (logSql) logger.info("SQLITE::SQL -> {}, args: {}", psql, JSON.toJSONString(getArgsValues()));
        return psql;
    }

    public static void main(String[] args) {

        UpdateBuilder sql = UpdateBuilder.getInstance("user")
                .update("code", "abc")
                .update("name", "xy")
                .where("id", "1")
                .where("id is not null")
                .where("name", "aaa")
                .where("name like '%x%'");

        String preSql = sql.getPreSql();
        System.out.println(preSql);
    }

}
