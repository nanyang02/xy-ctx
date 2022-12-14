package com.xy.sqlite3;

import com.alibaba.fastjson.JSON;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 实现更新的简单构建
 */
public class SelectBuilder extends AbsSqlBuilder {

    private String from, limit;

    public SelectBuilder(String tableName) {
        index = 1;
        args = new LinkedList<>();
        sql = new StringBuilder("select").append(" ");
        from = " from " + tableName;
        limit = "";
        where = new StringBuilder();
        order = new StringBuilder();
    }

    public SelectBuilder printSql() {
        this.logSql = true;
        return this;
    }

    public static SelectBuilder getInstance(String tableName) {
        return new SelectBuilder(tableName);
    }

    public SelectBuilder select(String column) {
        doSelect(column);
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
        doSelect("*");
        // 升级到where，不允许再添加select
        overStep(STEP_WHERE);
        return this;
    }

    public SelectBuilder orderAsc(String column) {
        doOrder(column, true, false);
        return this;
    }

    public SelectBuilder orderDesc(String column) {
        doOrder(column, false, true);
        return this;
    }

    public SelectBuilder order(String column) {
        doOrder(column, false, false);
        return this;
    }


    public SelectBuilder limit(int offset, int length) {
        doLimit(offset, length);
        return this;
    }

    @Override
    public String getPreSql() {
        String psql = sql.toString() + from + where.toString() + order.toString() + limit;
        if (logSql) logger.info("SQLITE::SQL -> {}, args: {}", psql, JSON.toJSONString(getArgsValues()));
        return psql;
    }

    public Integer queryInt() {
        return Sqlite3.queryInt(getPreSql(), preparedStatement -> {
            try {
                for (ParIndexBo arg : args) {
                    arg.fill(preparedStatement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public String queryStr() {
        return Sqlite3.queryStr(getPreSql(), preparedStatement -> {
            try {
                for (ParIndexBo arg : args) {
                    arg.fill(preparedStatement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public Date queryDate() {
        return Sqlite3.queryDate(getPreSql(), preparedStatement -> {
            try {
                for (ParIndexBo arg : args) {
                    arg.fill(preparedStatement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public Boolean queryBool() {
        return Sqlite3.queryBool(getPreSql(), preparedStatement -> {
            try {
                for (ParIndexBo arg : args) {
                    arg.fill(preparedStatement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public Double queryDouble() {
        return Sqlite3.queryDouble(getPreSql(), preparedStatement -> {
            try {
                for (ParIndexBo arg : args) {
                    arg.fill(preparedStatement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public <T> T queryObject(Class<T> tClass) {
        return Sqlite3.queryObject(getPreSql(), preparedStatement -> {
            try {
                for (ParIndexBo arg : args) {
                    arg.fill(preparedStatement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, tClass);
    }

    public <T> List<T> queryObjectList(Class<T> tClass) {
        return Sqlite3.queryList(getPreSql(), preparedStatement -> {
            try {
                for (ParIndexBo arg : args) {
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
        doWhere(column, arg);
        return this;
    }

    public SelectBuilder byId(Object arg) {
        return where("id", arg);
    }

    public SelectBuilder llike(String column, String keyword) {
        doWhereSingle(column + " like '%" + keyword + "'");
        return this;
    }

    public SelectBuilder rlike(String column, String keyword) {
        doWhereSingle(column + " like '" + keyword + "%'");
        return this;
    }

    public SelectBuilder blike(String column, String keyword) {
        doWhereSingle(column + " like '%" + keyword + "%'");
        return this;
    }

    public static void main(String[] args) {
        SelectBuilder sql = SelectBuilder.getInstance("user")
//                .select("code")
                .allColumn()
                .select("name")
                .select("name2,addr")
                .select("keys")
//                .where("id is not null")
                .where("name", "aaa")
                .where("name2", "bbb")
                .llike("name", "na")
                .where("name like '%x%'")
//                .orderAsc("name")
//                .orderAsc("name2")
                .limit(0, 2);

        String preSql = sql.getPreSql();
        System.out.println(preSql);
        System.out.println(sql.getArgsValues());
    }

}
