package com.xy;

import com.xy.context.annotation.Autowired;
import com.xy.ext.MyBatisAppContext;
import com.xy.mappper.UserDao;
import com.xy.service.MyService;
import com.xy.service.MyService2;
import com.xy.stereotype.ComponentScan;
import org.junit.Test;

/**
 * Class <code>com.xy.MyAppTest</code>
 *
 * @author yangnan 2022/10/18 14:39
 * @since 1.8
 */
@ComponentScan("com.xy")
public class ServiceAnnoTest {

//    @Autowired
//    UserDao userDao;

    @Autowired
    MyService service;

    @Autowired
    MyService2 myService;

    @Test
    public void x() {

        try (MyBatisAppContext ctx = new MyBatisAppContext(
                ServiceAnnoTest.class,
                new String[]{"jdbc", "mybatis.xml"}
        )) {

            // 完成测试类的属性注入
            ctx.beanPropInitial(this);

            // 调用测试类
            myService.eatApple();

            // 测试多例对象
            UserInfo ui2 = ctx.getBean(UserInfo.class);

            // 输出测试
            System.out.println(ui2.toString());
            // System.out.println(userDao.nowDate());
        }
    }

}
