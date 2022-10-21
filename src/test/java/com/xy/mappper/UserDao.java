package com.xy.mappper;

import org.apache.ibatis.annotations.Select;

import java.util.Date;

/**
 * Class <code>UserDao</code>
 *
 * @author yangnan 2022/10/18 14:44
 * @since 1.8
 */
public interface UserDao {

    @Select("select now()")
    Date nowDate();
}
