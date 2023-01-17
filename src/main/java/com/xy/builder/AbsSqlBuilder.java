package com.xy.builder;

import com.xy.builder.dto.ParIndexBo;
import com.xy.builder.v0.InsertBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbsSqlBuilder {

    // 按照不同的组成部分来构建
    private StringBuilder sql = new StringBuilder(), join = new StringBuilder(), where = new StringBuilder(), order = new StringBuilder();

    // 参数位置坐标索引（也用于进行PrepareStatement按照顺序来设置变量参数）
    private int index = 0;

    // 参数有序列表
    private LinkedList<ParIndexBo> args = new LinkedList<>();

    // 是否打印
    boolean logSql = false, unUse = true, unuseWhere = true, unUseOrder = true;

    // 当前构建的步骤的编号
    public int gStep = 0;

    public StringBuilder getSql() {
        return sql;
    }

    public StringBuilder getWhere() {
        return where;
    }

    public StringBuilder getOrder() {
        return order;
    }

    public LinkedList<ParIndexBo> getArgs() {
        return args;
    }

    public StringBuilder getJoin() {
        return join;
    }

    /**
     * 必须提供实现，用于获取Sqlit3的功能支持
     *
     * @return
     */
    protected abstract XyJdbc getJdbc();

    // 定义构建步骤的码值
    public static final int STEP_SELECT = 1, STEP_INSERT = 2, STEP_UPDATE = 2, STEP_JOIN = 3, STEP_WHERE = 4, STEP_ORDER = 5, STEPT_LIMIT = 6;

    // 应用码值设置
    public boolean overStep(int step) {
        // 如果给定的步骤于
        if (step >= gStep) {
            // 单向递增
            if (step > gStep) gStep = step;

            // 允许同级及以上
            return false;
        }
        return true;
    }

    public Logger logger = LoggerFactory.getLogger(getClass());

    public abstract String getPreSql();

    public List<?> getArgsValues() {
        return args.stream().map(ParIndexBo::get).collect(Collectors.toList());
    }


    public void doSelect(String column) {
        if (overStep(STEP_SELECT)) throw new RuntimeException("Sql构建出错，请按照 select from join where order limit 的循序设置sql");
        sql.append(unUse ? "" : ", ").append(column);
        if (unUse) unUse = false;
    }

    public void doJoin(String sql) {
        if (overStep(STEP_JOIN)) throw new RuntimeException("Sql构建出错，请按照 select from join where order limit 的循序设置sql");
        if (null != sql && !"".equals(sql))
            join.append(" ").append(sql).append(" ");
    }

    public void doUpdate(String column, Object arg) {
        if (overStep(STEP_UPDATE)) throw new RuntimeException("Sql构建出错，请按照 set where 的循序设置sql");
        sql.append(unUse ? "set " : ", ").append(column).append(" = ?");
        if (unUse) unUse = false;
        args.add(new ParIndexBo().setIndex(index++).setData(arg));
    }

    public void doInsert(String column, Object arg) {
        if (overStep(STEP_INSERT)) throw new RuntimeException("Sql构建出错，请按照 select value 的循序设置sql");
        sql.append(unUse ? "" : ", ").append(column);
        where.append(unUse ? "?" : ", ?");
        if (unUse) unUse = false;
        args.add(new ParIndexBo().setIndex(index++).setData(arg));
    }

    public void doWhere(String column, Object arg) {
        if (overStep(STEP_WHERE)) throw new RuntimeException("Sql构建出错 where 不能出现在 order by 和 limit 后面，请调整sql构建的顺序");
        where.append(unuseWhere ? " where " : " and ").append(column).append(" = ?");
        if (unuseWhere) unuseWhere = false;
        args.add(new ParIndexBo().setIndex(index++).setData(arg));
    }

    public void doWhereSingle(String sql) {
        if (overStep(STEP_WHERE)) throw new RuntimeException("Sql构建出错 where 不能出现在 order by 和 limit 后面，请调整sql构建的顺序");
        where.append(unuseWhere ? " where " : " and ").append(sql);
        if (unuseWhere) unuseWhere = false;
    }

    public void doOrder(String column, boolean isAsc, boolean isDesc) {
        if (overStep(STEP_ORDER)) throw new RuntimeException("Sql构建出错 where 不能出现在 order by 和 limit 后面，请调整sql构建的顺序");
        order.append(unUseOrder ? " order by " : ", ").append(column).append(isAsc ? " asc" : isDesc ? " desc" : "");
        if (unUseOrder) unUseOrder = false;
        index++;
    }

    public void doLimit(int offset, int length) {
        if (overStep(STEPT_LIMIT)) throw new RuntimeException("Sql构建出错 where 不能出现在 order by 和 limit 后面，请调整sql构建的顺序");
        // 放在 order 里面就好
        order.append(" limit ").append(offset).append(", ").append(length);
        args.add(new ParIndexBo().setIndex(index++).setData(offset));
        args.add(new ParIndexBo().setIndex(index++).setData(length));
    }

    public int execute() {
        return getJdbc().executeSql(getPreSql(), preparedStatement -> {
            try {
                for (ParIndexBo arg : args) {
                    arg.fill(preparedStatement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void usePrintSql() {
        logSql = true;
    }

    public boolean isLogSql() {
        return logSql;
    }
}
