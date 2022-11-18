package com.xy.controller;

import com.xy.stereotype.Controller;
import com.xy.web.annotation.RequestMapping;
import com.xy.web.annotation.RestMapping;

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

    @RestMapping("/api/test1")
    public Object testJson() {

        Map<String, Object> map = new HashMap<>();

        map.put("name", "test case.");
        map.put("addr", "China.Wuhan");
        map.put("date", new Date());
        map.put("ts", System.currentTimeMillis());
        map.put("author", "xy");

        return map;
    }

    @RestMapping("/api/shutdown")
    public String showShutDownCmd() {
        return "use '/SHUTDOWN' stop server";
    }

}
