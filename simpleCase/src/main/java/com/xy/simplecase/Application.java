package com.xy.simplecase;

import com.xy.factory.ApplicationDefaultContext;
import com.xy.stereotype.Controller;
import com.xy.web.annotation.RequestMapping;
import com.xy.web.annotation.RestMapping;
import com.xy.web.annotation.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class Application {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 使用main方法启动容器上下文，提供容器服务支持
     * @param args
     */
    public static void main(String[] args) {

        ApplicationDefaultContext ctx = new ApplicationDefaultContext();
        ctx.scan("com.xy.simplecase");
        ctx.useWeb(8080);
        ctx.webDispatcherJoin();
    }

    static final String home = System.getProperty("user.dir");

    @RequestMapping(value = "/getHome", type = RequestMapping.Type.JSON)
    public String getHomePath() {
        logger.info(home);
        String json = "{\"home\": \""+home+"\"}";
        return json;
    }

    @RequestMapping("/api/sayHi")
    public String sayHi(@Var(value = "name", defVal = "小明") String name) {
        return "Hi, " + name;
    }

    @RestMapping("/api/upload")
    public Object doUploadTest() {

        FileInfo fileInfo = new FileInfo();
        fileInfo.name = "temp.txt";
        fileInfo.suffix = ".txt";
        fileInfo.size = 100;

        return Result.success(fileInfo);
    }


}


class FileInfo {
    public String name, suffix;
    public long size;
}

class Result<T> {

    public String message;
    public int code; // 0-success, 1-fail
    public T data;

    private Result(T t) {
        this.data = t;
    }

    public static <T> Result<T> success(T data) {
        return new Result(data);
    }
}