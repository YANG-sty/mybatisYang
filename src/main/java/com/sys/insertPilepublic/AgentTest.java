package com.sys.insertPilepublic;

import com.sys.insertPile.UserService;
import com.sys.insertPile.UserService2;

/**
 * Create by yang_zzu on 2020/6/14 on 17:58
 */
public class AgentTest {

    public static void main(String[] args) {
        UserService userService = new UserService();

        UserService2 userService2 = new UserService2();
        try {
            userService.sayHello("你好世界@@@@");
            System.out.println("----------------------------------");
            System.out.println("年龄" + userService.sayHelloReturn("萨瓦迪卡", 18));
            System.out.println("----------------------------------");
            System.out.println(userService.sayHelloReturnEvery("小当家", 13, "110110110"));

            System.out.println("+++++++++++++++++++++++++++++++++++");

            userService2.sayHello("你好我的朋友！！！！！");
            System.out.println("----------------------------------");

            System.out.println(userService2.sayHelloReturn("xiaohua", 21));
            System.out.println("----------------------------------");
            System.out.println(userService2.sayHelloReturnEvery("haha", 10, "120110"));
            System.out.println("----------------------------------");


        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
