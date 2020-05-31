package com.sys.mybatis.my.proxy;

import javassist.*;
import sun.misc.ProxyGenerator;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author tommy
 * @title: ProxyFactory
 * @projectName design-patterns
 * @description: 动态代理工厂
 * @date 2020/5/264:06 PM
 */


public class ProxyFactory {
    // javassist  工具类
    public static TestService createProxy() throws Exception {
        ClassPool classPool = new ClassPool(); // javassist -->ASM  -->编辑JVM指令码
        // classloader （类是通过 classload 进行寻找的）
        classPool.appendSystemPath();

        // 1.创建一个类
        CtClass class1 = classPool.makeClass("TestServiceImpl");
        //添加要实现的接口
        class1.addInterface(classPool.get(TestService.class.getName()));

        // 2.创建一个方法
        CtMethod sayHello = CtNewMethod.make(
                CtClass.voidType, //返回值类型
                "sayHello", //方法名称
                new CtClass[]{classPool.get(String.class.getName())}, //参数类型
                new CtClass[0], //接口异常
                "{System.out.println(\"hello:\"+$1);}", //接口实现
                class1 //方法属于哪个类
        );
        //将方法 添加到 class1 类里面
        class1.addMethod(sayHello);

        //3.实例化这个对象
        Class aClass = classPool.toClass(class1);
        // 强制转换
        return (TestService) aClass.newInstance();
    }

    public static <T> T createProxy2(Class<T> claInterface, String src) throws Exception {
        ClassPool classPool = new ClassPool(); // javassist -->ASM  -->编辑JVM指令码
        // classloader
        classPool.appendSystemPath();

        // 1.创建一个类
        CtClass class1 = classPool.makeClass("TestServiceImpl");
        class1.addInterface(classPool.get(claInterface.getName()));

        // 2.创建一个方法
        CtMethod sayHello = CtNewMethod.make(CtClass.voidType,
                "sayHello", new CtClass[]{classPool.get(String.class.getName())}
                , new CtClass[0], src,
                class1);
        class1.addMethod(sayHello);
        ;

        //3.实例化这个对象
        Class aClass = classPool.toClass(class1);
        // 强制转换
        return (T) aClass.newInstance();
    }


    // 1.支持所有接口代理
    // 2.按常规的方式传递实现

    static int count = 0;

    /**
     * 动态代理
     * @param cla 接口类的的Class名
     * @param handler 对接口的实现
     * @param <T> 接口类的名称
     * @return
     * @throws Exception
     */
    public static <T> T createProxy3(Class<T> cla, InvocationHandler handler) throws Exception {
        // 1.创建一个类
        // 2.添加属性 handler 对象；
        // 3.创建这个接口下的所有方法实现，

        // 3.实例化

        // 1.创建一个类
        ClassPool classPool = new ClassPool();
        classPool.appendSystemPath();
        CtClass impl = classPool.makeClass("$proxy" + count++);
        // 类中添加接口
        impl.addInterface(classPool.get(cla.getName()));
        // 2.添加属性 handler ；
        CtField fie = CtField.make(
                "public com.sys.mybatis.my.proxy.ProxyFactory.InvocationHandler handler=null;", impl);
        // 将属性 添加到 类里面
        impl.addField(fie);
        //有返回值类型的代码（接口）
        // $args 将所有传递过来的参数，封装成一个数组 Object args[]
        String src = "return ($r)this.handler.invoke(\"%s\",$args);";
        //无法返回值类型的代码（接口）
        String voidSrc = "this.handler.invoke(\"%s\",$args);";
        for (Method method : cla.getMethods()) {
            CtClass returnType = classPool.get(method.getReturnType().getName());
            String name = method.getName();

            CtClass[] parameters = toCtClass(classPool, method.getParameterTypes());
            CtClass[] errors = toCtClass(classPool, method.getExceptionTypes());

            //方法名
            String srcImpl = "";
            if (method.getReturnType().equals(Void.class)) {
                srcImpl = voidSrc;
            } else {
                srcImpl = src;
            }
            CtMethod newMethod = CtNewMethod.make(
                    returnType, //返回值类型
                    name, //方法名称
                    parameters, //参数类型
                    errors,  //异常
                    String.format(srcImpl, method.getName()), // 实现代码
                    impl); //实现类
            impl.addMethod(newMethod);
        }

        // 生成字节码
        // class 字节码
        byte[] bytes = impl.toBytecode();
        Files.write(Paths.get(System.getProperty("user.dir") + "/target/" + impl.getName() + ".class"), bytes);


        // 类加载到当前 ClassLoader中， 将 javassist 类转换成一个普通的class 类
        Class aClass = classPool.toClass(impl);
        Object o = aClass.newInstance();
        //"com.sys.mybatis.ProxyFactory.InvocationHandler handler=null;" 给handler 赋值
        aClass.getField("handler").set(o, handler);
        return (T) o;
    }

    /**
     * 常规的数组，装换成 javassist 支持的class 数据组
     * @param pool
     * @param classes
     * @return
     */
    private static CtClass[] toCtClass(ClassPool pool, Class[] classes) {
        return Arrays.stream(classes).map(c -> {
            try {
                return pool.get(c.getName());
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList()).toArray(new CtClass[0]);
    }

   /* public interface TestService{
        void sayHello(String name);
    }*/
    public static void main(String[] args) throws Exception {

        /*TestService testService = createProxy();
        testService.sayHello("hahah");*/


        /*TestService2 proxy3 = createProxy3(TestService2.class, new InvocationHandler() {
            @Override
            public Object invoke(String methodName, Object[] args) {
                if (methodName.equals("sayHello")) {
                    System.out.println("hello:" + args[0]);
                }
                if (methodName.equals("sayHello1")) {
                    System.out.println("hello1:" + args[0] + args[1]);
                }

                return null;
            }
        });*/

        /*TestService2 proxy3 = createProxy3(TestService2.class, new InvocationHandlerImpl());

        proxy3.sayHello("hohohohoho");
        proxy3.sayHello1("同学们好", 18);
        */

        JdkProxy();

        jdkProxyTest();
    }


    public interface InvocationHandler {
        Object invoke(String methodName, Object args[]);
    }


    /**
     * 使用 invocationHandler 实现接口的方法，
     */
    public static class InvocationHandlerImpl implements InvocationHandler {
        /*@Override
        public Object invoke(String methodName, Object args[]) {
            System.out.println("hello");
            return null;
        }*/
        @Override
        public Object invoke(String methodName, Object[] args) {
            //判断是接口中的那个方法，然后进行相应的实现操作
            if (methodName.equals("sayHello")) {
                System.out.println("hello:" + args[0]);
            }
            if (methodName.equals("sayHello1")) {
                System.out.println("hello1:" + args[0] + args[1]);
            }

            return null;
        }
    }


    public interface TestService {
        String sayHello(String name);

        void sayHello2(String name);

        void sayHello3(String name, int i);

        void sayHelloN(String name);
    }

    public interface TestService2 {
        String sayHello(String name);

        void sayHello1(String name, int i);

    }


    /**
     * jdk 动态代理生成 .calss 文件
     * @throws IOException
     */
    public static void JdkProxy() throws IOException {
        // 定制版的动态代理 工具
        String s = "$proxy1.class";
        byte[] bytes = ProxyGenerator.generateProxyClass(
                s, new Class[]{TestService2.class});

        Files.write(Paths.get(System.getProperty("user.dir") + "/target/" + s), bytes);
    }

    /**
     * 使用 jdk 的动态代理
     */
    public static void jdkProxyTest() {
        ClassLoader loader = ProxyFactory.class.getClassLoader();
        Class<?>[] interfaces = new Class[]{
                TestService2.class
        };
        java.lang.reflect.InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("sayHello")) {
                System.out.println("AAAAAAA" + args[0]);
            } else if (method.getName().equals("sayHello1")){
                System.out.println("BBBBBB" + args[0] + "......." + args[1]);
            }
            return null;
        };
        TestService2 o = (TestService2) Proxy.newProxyInstance(loader, interfaces, handler);
        o.sayHello1("ddd", 1);
        o.sayHello("666666");
    }


}
