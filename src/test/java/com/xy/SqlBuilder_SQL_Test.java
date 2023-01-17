package com.xy;

import com.xy.builder.DbType;
import com.xy.builder.XySqlFactory;
import org.junit.Test;

/**
 * Class <code>SqlBuilderTest</code>
 *
 * @author yangnan 2023/1/17 16:36
 * @since 1.8
 */
public class SqlBuilder_SQL_Test {

    XySqlFactory factory = new XySqlFactory(DbType.sqlite3, "db.db");

    @Test
    public void insertTest() {
        factory.insertBuilder("dcms_aux.gcoll_user")
                .insert("id", 1)
                .insert("name", "zhangsan")
                .insert("age", 18)
                .printSql()
                .getPreSql();
    }

    @Test
    public void updateTest() {
        factory.updateBuilder("user")
                .update("code", "abc")
                .update("name", "xy")
                .where("id", "1")
                .where("id is not null")
                .where("name", "aaa")
                .where("name like '%x%'")
                .printSql()
                .getPreSql();
    }

    @Test
    public void selectTest() {
        factory.selectBuilder("user", "t")
//                .select("code")
//                .allColumn()
                .select("name")
                .select("name2,addr")
                .select("keys")
                .join("left join role r on r.userId=t.id")
                .ljoin("role", "id", "userId", "rr")
//                .where("id is not null")
                .where("name", "aaa")
                .where("name2", "bbb")
                .llike("name", "na")
                .blike("name", "y")
                .where("t.name like '%x%'")
//                .orderAsc("name")
//                .orderAsc("name2")
                .limit(0, 2)
                .printSql()
                .getPreSql();
    }



}
