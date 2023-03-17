package com.xy.ext.sqlite3;

import com.xy.ext.builder.dto.ConvertResultSetToEntity;
import com.xy.ext.builder.RsSingleType;
import com.xy.ext.builder.XyJdbc;
import com.xy.ext.builder.DbType;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * Class <code>Sqlite3</code>
 *
 * @author yangnan 2022/12/1 10:26
 * @since 1.8
 */
public class Sqlite3 implements XyJdbc {

    private String db = "local.db";

    public Sqlite3(String db) {
        if (null != db && !"".equals(db))
            this.db = db;
    }

    @Override
    public DbType getDbType() {
        return DbType.sqlite3;
    }

    @Override
    public int execSql(String sql) {
        Connection c = null;
        int effect = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            // if not exists will create db
            c = DriverManager.getConnection("jdbc:sqlite:" + db);
            Statement statement = c.createStatement();

            effect = statement.executeUpdate(sql);

            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != c) c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return effect;
    }

    @Override
    public void querySql(String sql, Consumer<ResultSet> rsConsumer) {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            // if not exists will create db
            c = DriverManager.getConnection("jdbc:sqlite:" + db);
            try (Statement statement = c.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    rsConsumer.accept(resultSet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != c) c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Object querySingleVal(String sql, Consumer<PreparedStatement> pst, RsSingleType singleType) {
        Connection c = null;
        Object t = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + db);
            PreparedStatement statement = c.prepareStatement(sql);
            pst.accept(statement);
            ResultSet resultSet = statement.executeQuery();
            if (singleType.name().equals(RsSingleType.INT.name())) {
                t = resultSet.getInt(1);
            } else if (singleType.name().equals(RsSingleType.BOOL.name())) {
                t = resultSet.getBoolean(1);
            } else if (singleType.name().equals(RsSingleType.DATE.name())) {
                t = resultSet.getDate(1);
            } else if (singleType.name().equals(RsSingleType.DOUBLE.name())) {
                t = resultSet.getDouble(1);
            } else {
                t = resultSet.getString(1);
            }
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != c) c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return t;
    }

    @Override
    public Integer queryInt(String sql, Consumer<PreparedStatement> pst) {
        return (Integer) querySingleVal(sql, pst, RsSingleType.INT);
    }

    @Override
    public String queryStr(String sql, Consumer<PreparedStatement> pst) {
        return (String) querySingleVal(sql, pst, RsSingleType.STR);
    }

    @Override
    public Date queryDate(String sql, Consumer<PreparedStatement> pst) {
        return (Date) querySingleVal(sql, pst, RsSingleType.DATE);
    }

    @Override
    public Boolean queryBool(String sql, Consumer<PreparedStatement> pst) {
        return (Boolean) querySingleVal(sql, pst, RsSingleType.BOOL);
    }

    @Override
    public Double queryDouble(String sql, Consumer<PreparedStatement> pst) {
        return (Double) querySingleVal(sql, pst, RsSingleType.DOUBLE);
    }

    @Override
    public <T> T queryObject(String sql, Consumer<PreparedStatement> pst, Class<T> tClass) {
        Connection c = null;
        T t = null;
        try {
            Class.forName("org.sqlite.JDBC");
            // if not exists will create db
            c = DriverManager.getConnection("jdbc:sqlite:" + db);
            PreparedStatement statement = c.prepareStatement(sql);
            pst.accept(statement);

            ResultSet resultSet = statement.executeQuery();
            // 转换成对象
            t = ConvertResultSetToEntity.parseObjectFromResultSet(resultSet, tClass);
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != c) c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return t;
    }

    private <T> List<T> toObjectList(ResultSet resultSet, Class<T> tClass) throws Exception {

        if (tClass == String.class) {
            return getObjectFromRs(resultSet);
        }
        if (tClass == Date.class) {
            return getObjectFromRs(resultSet);
        }
        if (tClass == Integer.class) {
            return getObjectFromRs(resultSet);
        }
        if (tClass == Float.class || tClass == Double.class) {
            return getObjectFromRs(resultSet);
        }
        if (tClass == Boolean.class) {
            return getObjectFromRs(resultSet);
        }

        return ConvertResultSetToEntity.parseDataEntityBeans(resultSet, tClass);
    }

    private <T> List<T> getObjectFromRs(ResultSet resultSet) throws SQLException {
        List<T> listResult = new ArrayList();
        while (resultSet.next()) {
            listResult.add((T) resultSet.getString(1));
        }
        return listResult;
    }

    @Override
    public <T> List<T> queryList(String sql, Consumer<PreparedStatement> pst, Class<T> tClass) {
        Connection c = null;
        List<T> t = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            // if not exists will create db
            c = DriverManager.getConnection("jdbc:sqlite:" + db);
            PreparedStatement statement = c.prepareStatement(sql);
            pst.accept(statement);
            ResultSet resultSet = statement.executeQuery();
            // 转换成对象
            t = toObjectList(resultSet, tClass);
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != c) c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return t;
    }

    @Override
    public int executeSql(String sql, Consumer<PreparedStatement> pst) {
        Connection c = null;
        int effect = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            // if not exists will create db
            c = DriverManager.getConnection("jdbc:sqlite:" + db);

            PreparedStatement statement = c.prepareStatement(sql);
            pst.accept(statement);

            effect = statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != c) c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return effect;
    }

}
