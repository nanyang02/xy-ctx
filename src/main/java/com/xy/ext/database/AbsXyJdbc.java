package com.xy.ext.database;

import com.xy.ext.builder.RsSingleType;
import com.xy.ext.builder.XyJdbc;
import com.xy.ext.builder.dto.ConvertResultSetToEntity;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.function.Consumer;

/**
 * Class <code>AbsXyJdbc</code>
 *
 * @author yangnan 2023/3/20 21:30
 * @since 1.8
 */
public abstract class AbsXyJdbc implements XyJdbc {

    public abstract Connection getConn() throws SQLException;

    @Override
    public int execSql(String sql) {
        int effect = 0;
        try {
            Statement statement = getConn().createStatement();
            effect = statement.executeUpdate(sql);
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return effect;
    }

    @Override
    public void querySql(String sql, Consumer<ResultSet> rsConsumer) {
        try (Statement statement = getConn().createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(sql)) {
                rsConsumer.accept(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Object querySingleVal(String sql, Consumer<PreparedStatement> pst, RsSingleType singleType) {
        Object t = null;
        try {
            PreparedStatement statement = getConn().prepareStatement(sql);
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
    public java.util.Date queryDate(String sql, Consumer<PreparedStatement> pst) {
        return (java.util.Date) querySingleVal(sql, pst, RsSingleType.DATE);
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
        T t = null;
        try {
            PreparedStatement statement = getConn().prepareStatement(sql);
            pst.accept(statement);

            ResultSet resultSet = statement.executeQuery();
            // 转换成对象
            t = ConvertResultSetToEntity.parseObjectFromResultSet(resultSet, tClass);
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    private <T> List<T> toObjectList(ResultSet resultSet, Class<T> tClass) throws Exception {

        if (tClass.isAssignableFrom(Map.class)) {
            return ConvertResultSetToEntity.parseDataToMapList(resultSet);
        }
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
        List<T> t = new ArrayList<>();
        try {
            PreparedStatement statement = getConn().prepareStatement(sql);
            pst.accept(statement);
            ResultSet resultSet = statement.executeQuery();
            // 转换成对象
            t = toObjectList(resultSet, tClass);
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    @Override
    public int executeSql(String sql, Consumer<PreparedStatement> pst) {
        int effect = 0;
        try {
            PreparedStatement statement = getConn().prepareStatement(sql);
            pst.accept(statement);

            effect = statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return effect;
    }

}
