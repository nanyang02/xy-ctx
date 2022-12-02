package com.xy.sqlite3;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertResultSetToEntity {

    /**
     * 实现结果集到实体对象/值对象/持久化对象转换
     *
     * @param rsResult    ResultSet
     * @param classEntity Class
     * @return Object[]
     * @throws Exception
     */
    public static <T> List<T> parseDataEntityBeans(ResultSet rsResult, Class<T> classEntity) throws Exception {

        DataTableEntity dataTable = getDataTableEntity(rsResult);

        // 处理ResultSet数据信息
        List<T> listResult = new ArrayList();
        while (rsResult.next()) {
            // 调用方法，根据字段名在hsMethods中查找对应的set方法
            T objResult = parseObjectFromResultSet(rsResult, dataTable, classEntity);

            listResult.add(objResult);
        }

        // 以数组方式返回
        // Object data = Array.newInstance(classEntity, listResult.size());
        // listResult.toArray((Object[]) data);
        return listResult;
    }

    /**
     * 获取到注入方法的映射map
     *
     * @param classEntity
     * @param <T>
     * @return
     */
    private static <T> Map<String, MethodEntity> getEntityMethodsMapping(Class<T> classEntity) {
        // 获取实体中定义的方法
        Map<String, MethodEntity> methods = new HashMap<>();

        searchMethodsFromClass(classEntity, methods);

        // 获取父类的方法，用于进行处理
        Class aClass = (Class) classEntity.getGenericSuperclass();
        if (!aClass.isInterface() && !aClass.getName().equals(Object.class.getName())) {
            searchMethodsFromClass(aClass, methods);
        }

        return methods;
    }

    private static <T> void searchMethodsFromClass(Class<T> classEntity, Map<String, MethodEntity> methods) {
        for (int i = 0; i < classEntity.getDeclaredMethods().length; i++) {
            MethodEntity methodEntity = new MethodEntity();
            // 方法的名称
            String methodName = classEntity.getDeclaredMethods()[i].getName();
            String methodKey = methodName.toUpperCase();
            // 方法的参数
            Class[] paramTypes = classEntity.getDeclaredMethods()[i].getParameterTypes();
            methodEntity.setMethodName(methodName);
            methodEntity.setMethodParamTypes(paramTypes);
            // 处理方法重载
            if (methods.containsKey(methodKey)) {
                methodEntity.setRepeatMethodNum(methodEntity.getRepeatMethodNum() + 1);
                methodEntity.setRepeatMethodsParamTypes(paramTypes);
            } else {
                methods.put(methodKey, methodEntity);
            }
        }
    }

    /**
     * 准备实体数据的表数据的信息元数据
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    private static DataTableEntity getDataTableEntity(ResultSet rs) throws SQLException {
        DataTableEntity dataTable = null;
        if (rs != null) {
            ResultSetMetaData rsMetaData = rs.getMetaData();
            int columnCount = rsMetaData.getColumnCount();
            dataTable = new DataTableEntity(columnCount);

            // 获取字段名称，类型
            for (int i = 0; i < columnCount; i++) {
                String columnName = rsMetaData.getColumnName(i + 1);
                int columnType = rsMetaData.getColumnType(i + 1);
                dataTable.setColumnName(columnName, i);
                dataTable.setColumnType(columnType, i);
            }
        }
        return dataTable;
    }

    /**
     * 从Resultset中解析出单行记录对象，存储在实体对象中
     */
    private static <T> T parseObjectFromResultSet(ResultSet rs, DataTableEntity dataTable, Class<T> classEntity) throws Exception {

        Map<String, MethodEntity> methods = getEntityMethodsMapping(classEntity);

        T objEntity = classEntity.newInstance();
        int nColumnCount = dataTable.getColumnCount();
        String[] strColumnNames = dataTable.getColumnNames();
        for (int i = 0; i < nColumnCount; i++) {
            // 获取字段值
            Object objColumnValue = rs.getObject(strColumnNames[i]);

            // HashMap中的方法名key值
            String strMethodKey = null;

            // 获取set方法名
            if (strColumnNames[i] != null) {
                strMethodKey = "SET" + strColumnNames[i].toUpperCase();
            }

            // 值和方法都不为空,这里方法名不为空即可,值可以为空的
            if (strMethodKey != null) {
                // 判断字段的类型,方法名，参数类型
                try {

                    MethodEntity methodEntity = methods.get(strMethodKey);
                    String methodName = methodEntity.getMethodName();
                    int repeatMethodNum = methodEntity.getRepeatMethodNum();
                    Class[] paramTypes = methodEntity.getMethodParamTypes();
                    Method method = classEntity.getMethod(methodName, paramTypes);

                    // 如果重载方法数 >
                    // 1，则判断是否有java.lang.IllegalArgumentException异常，循环处理
                    try {
                        // 设置参数,实体对象，实体对象方法参数
                        method.invoke(objEntity, new Object[]{objColumnValue});

                    } catch (java.lang.IllegalArgumentException e) {
                        // 处理重载方法
                        for (int j = 1; j < repeatMethodNum; j++) {
                            try {
                                Class[] repeatParamTypes = methodEntity.getRepeatMethodsParamTypes(j - 1);
                                method = classEntity.getMethod(methodName, repeatParamTypes);
                                method.invoke(objEntity, new Object[]{objColumnValue});
                                break;
                            } catch (java.lang.IllegalArgumentException ex) {
                                continue;
                            }
                        }
                    }
                } catch (NoSuchMethodException e) {
                    throw new NoSuchMethodException();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }
        return objEntity;
    }

    /**
     * 从Resultset中解析出单行记录对象，存储在实体对象中
     */
    public static <T> T parseObjectFromResultSet(ResultSet rs, Class<T> tClass) throws Exception {
        DataTableEntity dataTable = getDataTableEntity(rs);
        return parseObjectFromResultSet(rs, dataTable, tClass);
    }
}