package com.xy.simplecase;

import com.xy.stereotype.Controller;
import com.xy.web.annotation.RequestMapping;
import com.xy.web.annotation.RestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class FileManageController {

    private static final Logger logger = LoggerFactory.getLogger(FileManageController.class);

    static final String home = System.getProperty("user.dir");

    @RestMapping("/getHome")
    public String getHomePath() {
        logger.info(home);
        return home;
    }

}
