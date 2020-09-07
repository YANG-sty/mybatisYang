package com.sys.mybatis.teacher.caches;

import com.sys.mybatis.teacher.BlogMapper;
import com.sys.mybatis.teacher.Mock;
import com.sys.mybatis.teacher.StatementHandlerTest;
import com.sys.mybatis.teacher.UserMapper;
import com.sys.mybatis.teacher.bean.User;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Property;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

/**
 * @author tommy
 * @title: SecondCache
 * @projectName coderead-mybatis
 * @description: TODO
 * @date 2020/6/612:31 AM
 */
public class SecondCacheTest {

    private Configuration configuration;
    private SqlSessionFactory factory;

    @Before
    public void init() throws SQLException {
        SqlSessionFactoryBuilder factoryBuilder = new SqlSessionFactoryBuilder();
        factory = factoryBuilder.build(StatementHandlerTest.class.getResourceAsStream("/mybatis-config.xml"));
        configuration = factory.getConfiguration();
    }

    @Test
    public void cacheTest1() {
        Cache cache = configuration.getCache("com.sys.mybatis.teacher.UserMapper");
        User user = Mock.newUser();
        cache.putObject("luban", user);// 设置缓存
        cache.getObject("luban");
    }

    /**
     * 缓存实现扩展
     * 在 com.sys.mybatis.teacher.UserMapper 添加该配置
     * @CacheNamespace(implementation = DiskCache.class,properties = {@Property(name = "cachePath", value ="/Users/tommy/git/coderead-mybatis/target/cache" )})
     */
    @Test
    public void cacheTest2() {
        Cache cache = configuration.getCache("com.sys.mybatis.teacher.UserMapper");
        User user = Mock.newUser();
        cache.putObject("luban", user);// 设置缓存
        Object luban = cache.getObject("luban");
        System.out.println(luban.toString());
    }

    /**
     * 溢出淘汰 FIFO
     * 在 com.sys.mybatis.teacher.UserMapper 添加该配置
     * @CacheNamespace(eviction = FifoCache.class, size = 10)
     */
    @Test
    public void cacheTest3() {
        Cache cache = configuration.getCache("com.sys.mybatis.teacher.UserMapper");
        User user = Mock.newUser();
        for (int i = 0; i < 12; i++) {
            cache.putObject("luban:" + i, user);// 设置缓存
        }
        Object luban2 = cache.getObject("luban:2");
        Object luban9 = cache.getObject("luban:9");
        cache.putObject("luban:100", user);
        System.out.println(cache);
    }

    /**
     * @CacheNamespace(readWrite =false) true 序列化 false 非序列化
     * false 线程不安全的，速度快效率高
     * ture (序列化耗时，线程安全)
     */
    @Test
    public void cacheTest4() {
        Cache cache = configuration.getCache("com.sys.mybatis.teacher.UserMapper");
        User user = Mock.newUser();
        cache.putObject("luban", user);// 设置缓存
        // 线程1
        Object luban = cache.getObject("luban");
        // 线程2
        Object luban1 = cache.getObject("luban");
        System.out.println(luban==luban1);
    }

    /**
     * 命中条件：
     * 1.必须提交
     */
    @Test
    public void cacheTest5() {
        // 查询1
        SqlSession session1 = factory.openSession(true);
        UserMapper mapper1 = session1.getMapper(UserMapper.class);
        mapper1.selectByid3(10);// 重用执行器
      /*  // flush cache清空
        User user = mapper1.selectByid(10); //清空了，提交
        */
        session1.commit();
        // 查询2
        SqlSession session2 = factory.openSession();
        UserMapper mapper2 = session2.getMapper(UserMapper.class);
        User use2 = mapper2.selectByid3(10);
    }

    /**
     * 命中条件：
     * 1.必须提交
     */
    @Test
    public void cacheTest6() {
        // 查询1
        SqlSession session1 = factory.openSession(true);
        session1.getMapper(UserMapper.class).selectByid(1);
        session1.getMapper(BlogMapper.class).selectById(1);
        System.out.println(session1);
    }
}
