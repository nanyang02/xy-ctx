package com.xy.ext.builder;

import com.alibaba.fastjson.JSON;

import java.util.function.Supplier;

/**
 * 实现更新的简单构建
 */
public class UpdateBuilder extends AbsSqlBuilder {

    private XyJdbc jdbc;

    @Override
    protected XyJdbc getJdbc() {
        return jdbc;
    }

    public UpdateBuilder printSql() {
        usePrintSql();
        return this;
    }

    public UpdateBuilder(XyJdbc jdbc, String tableName) {
        getSql().append("update ").append(tableName).append(" ");
        this.jdbc = jdbc;
    }

    public static UpdateBuilder getInstance(XyJdbc jdbc, String tableName) {
        return new UpdateBuilder(jdbc, tableName);
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

    public UpdateBuilder whereIgNull(String column, Object arg) {
        if (null == arg) return this;
        return where(column, arg);
    }

    public UpdateBuilder where(boolean ifOk, String sql) {
        if (ifOk) where(sql);
        return this;
    }

    public UpdateBuilder where(boolean ifOk, String column, Supplier<Object> arg) {
        if (ifOk) where(column, arg.get());
        return this;
    }

    public UpdateBuilder byId(Object arg) {
        return where("id", arg);
    }


    @Override
    public String getPreSql() {
        String psql = getSql().toString() + getWhere().toString();
        if (isLogSql())
            logger.info(jdbc.getDbType().name().toUpperCase() + "::SQL -> {}, args: {}", psql, JSON.toJSONString(getArgsValues()));
        return psql;
    }

    /*public static void main(String[] args) {

        UpdateBuilder sql = UpdateBuilder.getInstance("user")
                .update("code", "abc")
                .update("name", "xy")
                .where("id", "1")
                .where("id is not null")
                .where("name", "aaa")
                .where("name like '%x%'");

        String preSql = sql.getPreSql();
        System.out.println(preSql);
    }*/

}
