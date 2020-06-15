package com.sys.insertPile;

/**
 *
 * 字节码插桩
 * 运行的时候需要附着于其他的jar 包，运行
 * java -jar xxx.jar -javaagent:xxxx.jar
 *
 * Create by yang_zzu on 2020/6/14 on 17:35
 */
public class UserService {

    public void sayHello(String s) throws InterruptedException {
        Thread.sleep(50);
        System.out.println("hello world!!! " + s);
    }


    public Integer sayHelloReturn(String s, Integer age) throws InterruptedException {
        Thread.sleep(100);
        System.out.println("hello world!!! " + s + "age = " + age);
        return age;
    }

    public String sayHelloReturnEvery(String name, Integer age, String phone) throws InterruptedException {
        Thread.sleep(200);
        return (name + " 的年龄是 " + age + " 电话是 " + phone);
    }


}
