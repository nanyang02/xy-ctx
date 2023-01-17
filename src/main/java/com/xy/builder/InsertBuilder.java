package com.xy.builder;

import com.alibaba.fastjson.JSON;

/**
 * 实现更新的简单构建
 */
public class InsertBuilder extends AbsSqlBuilder {

    private XyJdbc jdbc;

    private DbType dbType;

    @Override
    protected XyJdbc getJdbc() {
        return jdbc;
    }

    public InsertBuilder(XyJdbc jdbc, String tableName) {
        getSql().append("insert into ").append(tableName).append("(");
        getWhere().append(") values (");
        this.jdbc = jdbc;
    }

    public InsertBuilder printSql() {
        usePrintSql();
        return this;
    }

    public static InsertBuilder getInstance(XyJdbc jdbc, String tableName) {
        return new InsertBuilder(jdbc, tableName);
    }


    public InsertBuilder insert(String column, Object arg) {
        doInsert(column, arg);
        return this;
    }

    @Override
    public String getPreSql() {
        String psql = getSql().toString() + getWhere().toString() + ")";
        if (isLogSql())
            logger.info(jdbc.getDbType().name().toUpperCase() + "::SQL -> {}, args: {}", psql, JSON.toJSONString(getArgsValues()));
        return psql;
    }

}
