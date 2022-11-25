package com.xy.web;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class <code>RequestParams</code>
 *
 * @author yangnan 2022/11/21 12:50
 * @since 1.8
 */
@Data
@Accessors(chain = true)
public class RequestParams {

    public static RequestParams getInstace() {
        return new RequestParams();
    }

    public enum ContentType {
        JSON, FORM_URLENCODED, FORMDATA
    }

    private String path, method, contentType, bodyJson, varSplit;
    private Map<String, Object> params = new HashMap<>();
    private List<UploadFile> files = new ArrayList<>();
    private ContentType type = ContentType.FORM_URLENCODED;

}

