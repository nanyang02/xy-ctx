package com.xy.ext.builder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * jdbc handle interface use for builder
 */
public interface XyJdbc {

    /**
     * exec a sql and return effect row count.
     *
     * @param sql sql
     * @return effect row count
     */
    int execSql(String sql);

    /**
     * query and supply consumer for deal resultSet
     *
     * @param sql        sql
     * @param rsConsumer resultSet consume
     */
    void querySql(String sql, Consumer<ResultSet> rsConsumer);

    /**
     * query a num of int
     *
     * @param sql sql
     * @param pst PreparedStatement
     * @return
     */
    Integer queryInt(String sql, Consumer<PreparedStatement> pst);

    String queryStr(String sql, Consumer<PreparedStatement> pst);

    Date queryDate(String sql, Consumer<PreparedStatement> pst);

    Boolean queryBool(String sql, Consumer<PreparedStatement> pst);

    Double queryDouble(String sql, Consumer<PreparedStatement> pst);

    /**
     * query one row data and return object
     *
     * @param sql    sql
     * @param pst    PreparedStatement
     * @param tClass target dto or bean class
     * @param <T>    target dto or bean type
     * @return Target Object
     */
    <T> T queryObject(String sql, Consumer<PreparedStatement> pst, Class<T> tClass);

    /**
     * query mutil row data and return list
     *
     * @param sql    sql
     * @param pst    PreparedStatement
     * @param tClass target dto or bean class
     * @param <T>    target dto or bean type
     * @return Target Object list
     */
    <T> List<T> queryList(String sql, Consumer<PreparedStatement> pst, Class<T> tClass);

    /**
     * execute sql
     *
     * @param sql sql
     * @param pst for args set consume
     * @return effect row count
     */
    int executeSql(String sql, Consumer<PreparedStatement> pst);

    DbType getDbType();
}
