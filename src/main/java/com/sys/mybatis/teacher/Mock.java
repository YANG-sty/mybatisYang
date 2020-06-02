package com.sys.mybatis.teacher;

import com.sys.mybatis.teacher.bean.User;

/**
 * @author tommy
 * @title: Mock
 * @projectName coderead-mybatis
 * @description: TODO
 * @date 2020/5/309:47 PM
 */
public class Mock {
    public static User newUser() {
        User user = new User();
        String s = System.currentTimeMillis() + "";
        s = s.substring(s.length() - 5, s.length());
        user.setName("mock_" + s);
        user.setAge("19");
        user.setEmail("modk@coderead.cn");
        user.setPhoneNumber("888888");
        user.setSex("男");
        return user;
    }
}
