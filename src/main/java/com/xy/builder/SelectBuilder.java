package com.xy.builder;

import com.alibaba.fastjson.JSON;
import com.xy.builder.dto.ParIndexBo;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * 实现更新的简单构建
 */
public class SelectBuilder extends AbsSqlBuilder {

    private XyJdbc jdbc;

    private String from, limit, column_prefix;

    @Override
    protected XyJdbc getJdbc() {
        return jdbc;
    }

    public SelectBuilder(XyJdbc jdbc, String tableName, String alias) {
        if (null == jdbc)
            throw new RuntimeException("null value of jdbc");
        getSql().append("select ");
        from = " from " + tableName;
        column_prefix = limit = "";
        this.jdbc = jdbc;

        if (null != alias) {
            from = from + " " + alias;
            column_prefix = alias + ".";
        }
    }

    /**
     * main table prefix
     *
     * @param alias
     * @return
     */
    public SelectBuilder mAlias(String alias) {
        if (overStep(0)) {
            throw new RuntimeException("main table prefix must as first position");
        }
        column_prefix = alias + ".";
        return this;
    }

    public SelectBuilder printSql() {
        usePrintSql();
        return this;
    }

    public static SelectBuilder getInstance(XyJdbc jdbc, String tableName) {
        return new SelectBuilder(jdbc, tableName, null);
    }

    public SelectBuilder select(String column) {
        if (column.contains(",")) {
            column = column.replaceAll(",", ", " + column_prefix);
        }
        doSelect(column_prefix + column);
        return this;
    }

    public SelectBuilder select(boolean ifOk, String column) {
        if (ifOk) select(column);
        return this;
    }

    public SelectBuilder count() {
        if (gStep > 0) throw new RuntimeException("Sql构建出错，select count(1) 之前不能有其他查询");
        doSelect("count(1)");
        // 升级到where，不允许再添加select
        overStep(STEP_WHERE);
        return this;
    }

    public SelectBuilder allColumn() {
        if (gStep > 0) throw new RuntimeException("Sql构建出错，select * 之前不能有其他查询");
        doSelect(column_prefix + "*");
        // 升级到where，不允许再添加select
        overStep(STEP_WHERE);
        return this;
    }

    public SelectBuilder orderAsc(String column) {
        doOrder(column_prefix + column, true, false);
        return this;
    }

    public SelectBuilder orderAsc(boolean ifOk, String column) {
        if (ifOk) orderAsc(column);
        return this;
    }

    public SelectBuilder orderDesc(String column) {
        doOrder(column_prefix + column, false, true);
        return this;
    }

    public SelectBuilder orderDesc(boolean ifOk, String column) {
        if (ifOk) orderDesc(column);
        return this;
    }

    public SelectBuilder order(String column) {
        doOrder(column_prefix + column, false, false);
        return this;
    }

    public SelectBuilder order(boolean ifOk, String column) {
        if (ifOk) order(column);
        return this;
    }

    public SelectBuilder join(boolean ifOk, String sql) {
        if (ifOk) doJoin(sql);
        return this;
    }

    public SelectBuilder join(String sql) {
        doJoin(sql);
        return this;
    }

    public SelectBuilder ljoin(String tb, String tbColumn, String relateColumn, String alias) {
        doJoin("left join " + tb + " " + alias + " on " + alias + "." + tbColumn + " = " + column_prefix + relateColumn);
        return this;
    }

    public SelectBuilder ljoin(boolean ifOk, String tb, String tbColumn, String relateColumn, String alias) {
        if (ifOk) ljoin(tb, tbColumn, relateColumn, alias);
        return this;
    }

    public SelectBuilder limit(int offset, int length) {
        doLimit(offset, length);
        return this;
    }

    @Override
    public String getPreSql() {
        String psql = getSql() + from + getJoin().toString() + getWhere().toString() + getOrder().toString() + limit;
        if (isLogSql())
            logger.info(jdbc.getDbType().name().toUpperCase() + "::SQL -> {}, args: {}", psql, JSON.toJSONString(getArgsValues()));
        return psql;
    }

    public Integer queryInt() {
        return jdbc.queryInt(getPreSql(), preparedStatement -> {
            try {
                for (ParIndexBo arg : getArgs()) {
                    arg.fill(preparedStatement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public String queryStr() {
        return jdbc.queryStr(getPreSql(), preparedStatement -> {
            try {
                for (ParIndexBo arg : getArgs()) {
                    arg.fill(preparedStatement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public Date queryDate() {
        return jdbc.queryDate(getPreSql(), preparedStatement -> {
            try {
                for (ParIndexBo arg : getArgs()) {
                    arg.fill(preparedStatement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public Boolean queryBool() {
        return jdbc.queryBool(getPreSql(), preparedStatement -> {
            try {
                for (ParIndexBo arg : getArgs()) {
                    arg.fill(preparedStatement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public Double queryDouble() {
        return jdbc.queryDouble(getPreSql(), preparedStatement -> {
            try {
                for (ParIndexBo arg : getArgs()) {
                    arg.fill(preparedStatement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public <T> T queryObject(Class<T> tClass) {
        return jdbc.queryObject(getPreSql(), preparedStatement -> {
            try {
                for (ParIndexBo arg : getArgs()) {
                    arg.fill(preparedStatement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, tClass);
    }

    public <T> List<T> queryObjectList(Class<T> tClass) {
        return jdbc.queryList(getPreSql(), preparedStatement -> {
            try {
                for (ParIndexBo arg : getArgs()) {
                    arg.fill(preparedStatement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, tClass);
    }

    public SelectBuilder where(String sql) {
        doWhereSingle(sql);
        return this;
    }

    public SelectBuilder where(String column, Object arg) {
        doWhere(column_prefix + column, arg);
        return this;
    }

    public SelectBuilder where(boolean ifOk, String sql) {
        if (ifOk) where(sql);
        return this;
    }

    public SelectBuilder where(boolean ifOk, String column, Object arg) {
        if (ifOk) where(column, arg);
        return this;
    }

    public SelectBuilder byId(Object arg) {
        return where(column_prefix + "id", arg);
    }

    public SelectBuilder llike(String column, String keyword) {
        doWhereSingle(column_prefix + column + " like '%" + keyword + "'");
        return this;
    }

    public SelectBuilder llike(boolean ifOk, String column, String keyword) {
        if (ifOk) llike(column, keyword);
        return this;
    }

    public SelectBuilder rlike(String column, String keyword) {
        doWhereSingle(column_prefix + column + " like '" + keyword + "%'");
        return this;
    }

    public SelectBuilder rlike(boolean ifOk, String column, String keyword) {
        if (ifOk) rlike(column, keyword);
        return this;
    }

    public SelectBuilder blike(String column, String keyword) {
        doWhereSingle(column_prefix + column + " like '%" + keyword + "%'");
        return this;
    }

    public SelectBuilder blike(boolean ifOk, String column, String keyword) {
        if (ifOk) blike(column, keyword);
        return this;
    }
}
