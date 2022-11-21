package com.xy.controller;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Class <code>IdNameBo</code>
 *
 * @author yangnan 2022/11/21 16:50
 * @since 1.8
 */
@Data
@Accessors(chain = true)
public class IdNameBo {

    private String id,name;

}
