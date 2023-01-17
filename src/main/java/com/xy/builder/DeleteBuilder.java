package com.xy.builder;

import com.alibaba.fastjson.JSON;

/**
 * 实现更新的简单构建
 */
public class DeleteBuilder extends AbsSqlBuilder {

    private XyJdbc jdbc;

    @Override
    protected XyJdbc getJdbc() {
        return jdbc;
    }

    public DeleteBuilder(XyJdbc jdbc, String tableName) {
        this.jdbc = jdbc;
        getSql().append("delete from ").append(tableName).append(" ");
    }

    public DeleteBuilder printSql() {
        usePrintSql();
        return this;
    }

    public static DeleteBuilder getInstance(XyJdbc jdbc, String tableName) {
        return new DeleteBuilder(jdbc, tableName);
    }

    public DeleteBuilder where(String column, Object arg) {
        doWhere(column, arg);
        return this;
    }

    public DeleteBuilder where(String sql) {
        doWhereSingle(sql);
        return this;
    }

    public DeleteBuilder byId(Object arg) {
        return where("id", arg);
    }


    @Override
    public String getPreSql() {
        String psql = getSql().toString() + getWhere().toString();
        if (isLogSql())
            logger.info(jdbc.getDbType().name().toUpperCase() + "::SQL -> {}, args: {}", psql, JSON.toJSONString(getArgsValues()));
        return psql;
    }
}
