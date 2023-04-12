package com.xy.controller;

import com.xy.dto.IdNameBo;
import com.xy.dto.UserInfo;
import com.xy.stereotype.Controller;
import com.xy.web.UploadFile;
import com.xy.web.annotation.*;

import java.util.Base64;
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

    @Api(label = "测试", args = "id:str;name:str:zhangsan")
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

    @Api(label = "停服务", desc = "用于通过Api访问的方式关闭服务")
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

    @Mapping("/uploadImagAndShowHtml")
    public String imageToHtml(@Var("image") UploadFile uploadFile, @Var("alias") String alias) {
        String image = "";
        String fileName = "";
        String mime = "";
        if (null != uploadFile) {
            image = Base64.getMimeEncoder().encodeToString(uploadFile.getBytes());
            System.out.println(image);
            fileName = uploadFile.getName();
            mime = uploadFile.getMime();
        }
        return "<html>" +
                "<body> <p> alias : " + alias + "</p>" +
                "<label>" + fileName + "</label>" +
                "<img src=\"data:" + mime + ";base64," + image + "\">" +
                "</body>" +
                "</html>";

    }
}
