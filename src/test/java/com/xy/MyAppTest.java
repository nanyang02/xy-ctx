package com.xy;

import com.xy.stereotype.ComponentScan;

/**
 * Class <code>com.xy.MyAppTest</code>
 *
 * @author yangnan 2022/10/18 14:39
 * @since 1.8
 */
@ComponentScan("com.xy")
public class MyAppTest {

//    static MyBatisAppContext ctx = new MyBatisAppContext(MyAppTest.class);
//
//    @Before
//    public void before() {
//        ctx.registDs(new String[]{"jdbc", "mybatis_sample.xml"});
//    }
//
//    @After
//    public void after() {
//        ctx.close();
//    }
//
//    @Autowired
//    UserDao userDao;
//
//    @Autowired
//    MyService myService;
//
//    private BeanGetter<UserInfo> userInfo2;
//
//    @Test
//    public void x() {
//        // autowired dependency beanGetter 会自动注入
//        ctx.beanPropInitial(this);
//
//        Date i = userDao.nowDate();
//
//        String username1 = ctx.getBean(UserInfo.class).getUsername();
//        System.out.println(username1);
//
//        String username = userInfo2.get().getUsername();
//
//        System.out.println(i + "" + username);
//
//
//        myService.sayhi();
//
//        UserInfo ui2 = ctx.getBean(UserInfo.class);
//        System.out.println(ui2.toString());
//    }

}
