package com.sys.insertPilepublic;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * 监控所有的方法类
 *
 * Create by yang_zzu on 2020/6/14 on 20:35
 */
public class PublicAgentMain {

    //javaagent 入口方法

    // 以 arg 为前缀的类才会进行插桩处理 -javaagent:xxx.jar=com.sys.insertPile
    public static void premain(String arg, Instrumentation instrumentation) {

        System.out.println("hello agent!!!!!");

        final String config = arg;

        // 使用 javassist ,在运行时修改 class 字节码，就是 插桩
        final ClassPool pool = new ClassPool();
        pool.appendSystemPath();

        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

                if (className == null || !className.replaceAll("/",".").startsWith(config)) {
                    return null;
                }

                try {
                    className = className.replaceAll("/", ".");
                    CtClass ctClass = pool.get(className);
                    // 获得类中的所有方法
                    for (CtMethod declaredMethod : ctClass.getDeclaredMethods()) {
                        newMethod(declaredMethod);
                    }
                    return ctClass.toBytecode();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        });


    }

    //复制原有的方法（类似于使用 agent ）
    private static CtMethod newMethod(CtMethod oldMethod) {
        CtMethod copy = null;
        try {
            //1. 将方法进行复制
            copy = CtNewMethod.copy(oldMethod, oldMethod.getDeclaringClass(), null);
            //类似于使用动态代理
            copy.setName(oldMethod.getName() + "$agent");
            //类文件中添加 sayHello$agent 方法
            oldMethod.getDeclaringClass().addMethod(copy);

            //2. 改变原有的方法,将 原有的 sayHello 方法进行重写操作
            if (oldMethod.getReturnType().equals(CtClass.voidType)) {
                oldMethod.setBody(String.format(voidSource, oldMethod.getName()));
            } else {
                oldMethod.setBody(String.format(source, oldMethod.getName()));
            }
        } catch (CannotCompileException | NotFoundException e) {
            e.printStackTrace();
        }
        return copy;

    }

    /**
     * 参数的封装
     * $$ ======》 arg1, arg2, arg3
     * $1 ======》 arg1
     * $2 ======》 arg2
     * $3 ======》 arg3
     * $args ======》 Object[]
     */
    //有返回值得方法
    final static String source = "{ long begin = System.currentTimeMillis();\n" +
            "        Object result;\n" +
            "        try {\n" +
            "            result = ($w)%s$agent($$);\n" + //s% 将参数传递到下一个方法，然后使用 s% 传递的参数进行替换操作, $w 表示的是在进行return的时候会强制的进行类型转换
            "        } finally {\n" +
            "            long end = System.currentTimeMillis();\n" +
            "            System.out.println(end - begin);\n" +
            "        }\n" +
            "        return ($r) result;}";

    //没有返回值的方法
    final static String voidSource = "{long begin = System.currentTimeMillis();\n" +
            "        try {\n" +
            "            %s$agent($$);\n" +
            "        } finally {\n" +
            "            long end = System.currentTimeMillis();\n" +
            "            System.out.println(end - begin);\n" +
            "        }}";


}
