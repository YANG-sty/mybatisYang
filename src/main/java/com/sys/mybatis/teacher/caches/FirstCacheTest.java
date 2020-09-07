package com.sys.mybatis.teacher.caches;

import com.sys.mybatis.teacher.ExecutorTest;
import com.sys.mybatis.teacher.UserMapper;
import com.sys.mybatis.teacher.bean.User;
import org.apache.ibatis.session.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;
import java.util.List;

/**
 * @author tommy
 * @title: FirstCache
 * @projectName coderead-mybatis
 * @description: 一级缓存测试
 * @date 2020/5/318:54 AM
 */
public class FirstCacheTest {
    private SqlSessionFactory factory;
    private SqlSession sqlSession;

    @Before
    public void init() throws SQLException {
        // 获取构建器
        SqlSessionFactoryBuilder factoryBuilder = new SqlSessionFactoryBuilder();
        // 解析XML 并构造会话工厂
        factory = factoryBuilder.build(ExecutorTest.class.getResourceAsStream("/mybatis-config.xml"));
        sqlSession = factory.openSession();
    }

    // 1.sql 和参数必须相同
    // 2.必须是相同的statementID（方法路径）
    // 3.sqlSession必须一样 （会话级缓存）
    // 4.RowBounds 返回行范围必须相同
    @Test
    public void test1(){
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        // com.sys.mybatis.teacher.UserMapper.selectByid
        User user = mapper.selectByid(10);
        RowBounds rowbound=RowBounds.DEFAULT;
        //从第0条记录开始，返回十条数据
        RowBounds rowBounds = new RowBounds(0, 10);
        List user1 = sqlSession.selectList(
                "com.sys.mybatis.teacher.UserMapper.selectByid", 10,rowbound);
        System.out.println(user == user1.get(0));
    }

    // 1.未手清空 sqlSession.clearCache();
    // 2.未调用 flushCache=true的查询 ====> @Options(flushCache = Options.FlushCachePolicy.TRUE)
    // 3.未执行Update ==>  @Update （不管里面执行的是修改还是查询操作都一样会清空一级缓存）<==> sqlSession.commit(); <==> sqlSession.rollback();
    // 4.缓存作用域不是 STATEMENT  ===> localCacheScope=STATEMENT (一级缓存的作用域是 STATEMENT (在子查询或者嵌套查询的时候一级缓存会命中)) 修改为 STATEMENT 降低了一级缓存的作用域

    @Test
    public void test2(){
        // 会话生命周期是短暂的
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        User user = mapper.selectByid3(10);
        //多次查询只要中间有一次清空缓存的操作，就会将之前所有存在的一级缓存清除
//        sqlSession.clearCache();  // <==> @Options(flushCache = Options.FlushCachePolicy.TRUE)
        sqlSession.commit(); // clearCache()
        sqlSession.rollback(); // clearCache()
//        mapper.setName(11,"道友永存");// clearCache()
        User user1 = mapper.selectByid3(10);// 数据一至性问题
        System.out.println(user == user1);

        /**
         * 在进行 updata 的操作的时候，为了避免产生数据不一致的问题，会将一级缓存全部进行清空
         * 将 HashMap 中的数据全部清空，
         * 至于为什么不通过key值清空指定的修改过的数据记录：
         * 1.一级缓存是会话级别的缓存，会话存在的时间本身就非常的短，（会话结束后就不存在了，没有必要去做多余的操作）
         * 2. 通过id 去清空指定的缓存，会导致框架变得复杂，得不偿失
         * 优化：.也有一定的使用场景（如果是非常大的数据话而且使用到连表查询的情况下，有缓存的话，可以直接使用id,去查询数据）
         */
    }

    @Test
    public void test3(){
        //通过会话构建 map
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        User user = mapper.selectByid(10);
        User user1 =mapper.selectByid(10);
        System.out.println(user == user1);
    }

    /**
     * mybatis + spring 集成
     * 每次在执行的时候都会新建一个会话，
     * 一个会话 sqlSession 对应一个 executor 对应 n 个statementHandler
     * 一级缓存是基于会话的，所以在集成的时候，不会命中一级缓存
     *
     * 将查询操作放到一个事务里面，则会命中一级缓存
     * 开启事务后，在执行到 getSqlSession 的时候，会判断是否是同一个 SqlSessionHolder
     * 如果是同一个 SqlSessionHolder 则会命中缓存，如果 SqlSessionHolder 为空则会创建一个新的 SqlSessionHolder
     *
     * 先进入 mybatis 中 MapperProxy (动态代理) 然后
     * SqlSession（mybatis中的） 会被替换为 SqlSessionTemplate（spring中的）
     * 再进入 spring 中的 SqlSessionTemplate （会话模板） 进行spring的动态代理
     * 再进入 SqlSessionFactory
     * 通过两次动态代理去实现，主要是想保证在使用spring的时候，事务是通过spring来进行控制
     *
     * mybatis 中的事务指的是，connection.   commit(),有没有手动提交，或者自动提交，提交之后则代表该事务结束。
     *
     */
    @Test
    public void testBySpring(){
        //通过spring ioc  构建map
        ClassPathXmlApplicationContext context=new ClassPathXmlApplicationContext("spring.xml");
        UserMapper mapper = context.getBean(UserMapper.class);
     //   动太代理                          动太代理                   MyBatis
    // mapper ->SqlSessionTemplate --> SqlSessionInterceptor-->SqlSessionFactory

        DataSourceTransactionManager transactionManager =
                (DataSourceTransactionManager) context.getBean("txManager");
        // 手动开启事物
        TransactionStatus status = transactionManager
                .getTransaction(new DefaultTransactionDefinition());

        User user = mapper.selectByid(10); // 每次都会构造一个新会话 发起调用

        User user1 =mapper.selectByid(10);// 每次都会构造一个新会话 发起调用

        System.out.println(user == user1);

        //提交事务，关闭事务
        transactionManager.commit(status);


    }

}
