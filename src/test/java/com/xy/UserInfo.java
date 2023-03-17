package com.xy;

import com.xy.context.annotation.Scope;
import kz.greetgo.stereotype.Component;
import lombok.Data;

import static com.xy.context.annotation.ScopeType.prototype;

/**
 * Class <code>UserInfo</code>
 *
 * @author yangnan 2022/10/18 14:43
 * @since 1.8
 */
@Data
@Component
@Scope(prototype)
public class UserInfo {

    private String username = "admin";
    private String password = "pw123456";

}
