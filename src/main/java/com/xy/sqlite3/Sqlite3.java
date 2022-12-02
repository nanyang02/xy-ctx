package com.xy.sqlite3;

import java.sql.*;
import java.util.function.Consumer;

/**
 * Class <code>Sqlite3</code>
 *
 * @author yangnan 2022/12/1 10:26
 * @since 1.8
 */
public class Sqlite3 {

    private static String db = "cfg.db";

    public static void setDbName(String name) {
        db = name;
    }

    public static int execSql(String sql) {
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

    public static void querySql(String sql, Consumer<ResultSet> rsConsumer) {
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

    public static int executeSql(String sql, Consumer<PreparedStatement> pst) {
        Connection c = null;
        int effect = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            // if not exists will create db
            c = DriverManager.getConnection("jdbc:sqlite:cfg.db");

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
