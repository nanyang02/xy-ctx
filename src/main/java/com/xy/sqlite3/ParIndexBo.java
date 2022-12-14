package com.xy.sqlite3;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Class <code>ParIndexBo</code>
 *
 * @author yangnan 2022/12/1 21:47
 * @since 1.8
 */
public class ParIndexBo {

    private Integer index;
    private String typeName;
    private Object data;

    public <T> T get() {
        return (T) data;
    }

    public ParIndexBo setData(Object data) {
        if (null != data) {
            typeName = data.getClass().getName();
            this.data = data;
        }
        return this;
    }

    public ParIndexBo setIndex(Integer index) {
        this.index = index;
        return this;
    }

    public void fill(PreparedStatement preparedStatement) throws SQLException {
        if (String.class.getName().equals(typeName)) {
            preparedStatement.setString(index, get());
        } else if (Integer.class.getName().equals(typeName)) {
            preparedStatement.setInt(index, get());
        }
    }

    @Override
    public String toString() {
        return "ParIndexBo{" +
                "index=" + index +
                ", typeName='" + typeName + '\'' +
                ", data=" + data +
                '}';
    }
}
