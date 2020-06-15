package com.sys.insertPilepublic;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * 实现对某个类的，某个方法的 监控与改造
 * 不具有普适性，
 * 一个项目中有好多的java 文件，好多的 方法，每个方法的参数也是不一样的，
 * 使用这种形式，相当的复杂。
 *
 * Create by yang_zzu on 2020/6/14 on 18:02
 */
public class AgentMain {
   //javaagent 入口方法

    public static void premain(String arg, Instrumentation instrumentation) {
        System.out.println("hello agent!!!!!");

        // 使用 javassist ,在运行时修改 class 字节码，就是 插桩
        final ClassPool pool = new ClassPool();
        pool.appendSystemPath();


        //打印所有的 IDEA 中类的方法
        /**
         * 添加类加载过滤器进行拦截
         * 拦截的时候使用的是 javaagent
         * 编辑、修改 class 字节码文件的时候使用的是 javassist 技术
         */
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

                /**
                 * 不能通过这种方式获得 UserService 类的路径名称，
                 * 因为这个时候，你使用的是  UserService.class 这个时候 UserService.java 文件已经加载为 .class 文件了
                 */
                //String name = UserService.class.getName();
                //判断 是否是 com.sys.insertPile.UserService 类文件
                if (!"com/sys/insertPile/UserService".equals(className)) {
                    return null;
                }
                try {
                    /**
                     * 在对类文件进行插入的时候，插入的是一个一个的代码块
                     * 一个插入语句，就是一个代码块
                     * {
                     *     int a = 3;
                     * }
                     * {
                     *     System.out.println(a);
                     * }
                     * 这种方式，是获得不到 a 的值的。
                     *
                     */
                    //获得类文件
                    CtClass ctClass = pool.get("com.sys.insertPile.UserService");
                    //获得类中的方法
                    CtMethod sayHello = ctClass.getDeclaredMethod("sayHello");

                    /**
                     * 打印方法执行的时间
                     * 1. 将方法进行复制
                     * 2. 改变原有的方法
                     */
                    //1. 将方法进行复制
                    CtMethod copy = CtNewMethod.copy(sayHello, ctClass, null);
                    //类似于使用动态代理
                    copy.setName("sayHello$agent");
                    //类文件中添加 sayHello$agent 方法
                    ctClass.addMethod(copy);

                    //2. 改变原有的方法,将 原有的 sayHello 方法进行重写操作
                    sayHello.setBody("{ long begin = System.currentTimeMillis();\n" +
                            "        sayHello$agent($1);\n" + // $1 表示 的是第一个参数， $0 代表的是 this
                            "        System.out.println(System.currentTimeMillis() - begin); } ");

                    //在方法中加入一行代码
//                    sayHello.insertBefore("System.out.println(System.currentTimeMillis());");

                    //class 字节码
                    return ctClass.toBytecode();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println(className);

                return null;
            }
        });


    }
}
