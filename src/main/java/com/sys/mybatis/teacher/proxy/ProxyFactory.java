package com.sys.mybatis.teacher.proxy;

import javassist.*;
import sun.misc.ProxyGenerator;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
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
        // classloader
        classPool.appendSystemPath();

        // 1.创建一个类
        CtClass class1 = classPool.makeClass("TestServiceImpl");
        class1.addInterface(classPool.get(TestService.class.getName()));

        // 2.创建一个方法
        CtMethod sayHello = CtNewMethod.make(CtClass.voidType,
                "sayHello", new CtClass[]{classPool.get(String.class.getName())}
                , new CtClass[0], "{System.out.println(\"hello:\"+$1);}",
                class1);
        class1.addMethod(sayHello);
        ;

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

    public static <T> T createProxy3(Class<T> cla, InvocationHandler handler) throws Exception {
        // 1.创建一个类
        // 2.添加属性 handler 对象；
        // 3.创建这个接口下的所有方法实现，

        // 3.实例化

        // 1.创建一个类
        ClassPool classPool = new ClassPool();
        classPool.appendSystemPath();
        CtClass impl = classPool.makeClass("$proxy" + count++);
        impl.addInterface(classPool.get(cla.getName()));
        // 2.添加属性 handler ；
        CtField fie = CtField.make(
                "public com.sys.mybatis.teacher.proxy.ProxyFactory.InvocationHandler handler=null;", impl);
        impl.addField(fie);
        String src = "return ($r)this.handler.invoke(\"%s\",$args);";
        String voidSrc = "this.handler.invoke(\"%s\",$args);";
        for (Method method : cla.getMethods()) {
            CtClass returnType = classPool.get(method.getReturnType().getName());
            String name = method.getName();
            CtClass[] parameters = toCtClass(classPool, method.getParameterTypes());
            CtClass[] errors = toCtClass(classPool, method.getExceptionTypes());
            String srcImpl = "";
            if (method.getReturnType().equals(Void.class)) {
                srcImpl = voidSrc;
            } else {
                srcImpl = src;
            }
            CtMethod newMethod = CtNewMethod.make(returnType, name, parameters
                    , errors, String.format(srcImpl, method.getName()), impl);
            impl.addMethod(newMethod);
        }

        // 生成字节码
        // class 字节码
        byte[] bytes = impl.toBytecode();
        Files.write(Paths.get(System.getProperty("user.dir") + "/target/" + impl.getName() + ".class"), bytes);

        // 类加载到当前 ClassLoader中
        Class aClass = classPool.toClass(impl);
        Object o = aClass.newInstance();
        aClass.getField("handler").set(o, handler);
        return (T) o;
    }

    private static CtClass[] toCtClass(ClassPool pool, Class[] classes) {
        return Arrays.stream(classes).map(c -> {
            try {
                return pool.get(c.getName());
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList()).toArray(new CtClass[0]);
    }


    public static void main(String[] args) throws Exception {
        TestService2 proxy3 = createProxy3(TestService2.class, new InvocationHandler() {
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
        });
        proxy3.sayHello1("同学们好", 18);
//        jdkProxyTest();
//        JdkProxy();
    }


    public interface InvocationHandler {
        Object invoke(String methodName, Object args[]);
    }


    public class InvocationHandlerImpl implements InvocationHandler {
        @Override
        public Object invoke(String methodName, Object args[]) {
            System.out.println("hello");
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


    public static void JdkProxy() throws IOException {
        // 定制版的动态代理 工具
        String s = "$proxy1.class";
        byte[] bytes = ProxyGenerator.generateProxyClass(
                s, new Class[]{TestService2.class});

        Files.write(Paths.get(System.getProperty("user.dir") + "/target/" + s), bytes);
    }

    public static void jdkProxyTest() {
        ClassLoader loader = ProxyFactory.class.getClassLoader();
        Class<?>[] interfaces = new Class[]{
                TestService2.class
        };
        java.lang.reflect.InvocationHandler handler = (proxy, method, args) -> {
            return null;
        };
        TestService2 o = (TestService2) Proxy.newProxyInstance(loader, interfaces, handler);
        o.sayHello1("ddd", 1);
    }


}
