package com.xy.controller;

import com.xy.stereotype.Controller;
import com.xy.web.annotation.Json;
import com.xy.web.annotation.RequestMapping;
import com.xy.web.annotation.RestMapping;
import com.xy.web.annotation.Var;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Class <code>MyTestController</code>
 *
 * @author yangnan 2022/11/18 10:42
 * @since 1.8
 */
@Controller
public class MyTestController {

    @RequestMapping("/api/test")
    public String testPlain() {
        return "Hi, Tom";
    }

    @RestMapping("/api/test1") // http://localhost:8080/api/test1
    public Object testJson(@Var("id") String id, @Var("name") String name) {

        Map<String, Object> map = new HashMap<>();

        map.put("desc", "test case.");
        map.put("addr", "China.Wuhan");
        map.put("date", new Date());
        map.put("ts", System.currentTimeMillis());
        map.put("author", "xy");
        map.put("id", id);
        map.put("name", name);

        return map;
    }

    @RestMapping("/api/shutdown")
    public String showShutDownCmd() {
        return "use '/SHUTDOWN' stop server";
    }


    @RestMapping("/api/getUserInfo")
    public UserInfo getUserInfo(@Json UserInfo userInfo) {
        return userInfo;
    }

    @RestMapping("/api/getIdName")
    public IdNameBo getIdName(@Var("id") String id, @Var("name") String name) {
        return new IdNameBo().setId(id).setName(name);
    }
}
