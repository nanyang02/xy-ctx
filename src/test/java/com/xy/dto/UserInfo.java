package com.xy.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Class <code>UserInfo</code>
 *
 * @author yangnan 2022/11/21 16:50
 * @since 1.8
 */
@Data
@Accessors(chain = true)
public class UserInfo {

    private String username, password, type, accountName, email, phone;

}
