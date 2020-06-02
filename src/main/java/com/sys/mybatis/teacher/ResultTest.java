package com.sys.mybatis.teacher;

import com.sys.mybatis.teacher.bean.Blog;
import com.sys.mybatis.teacher.bean.User;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tommy
 * @title: ResultTest
 * @projectName coderead-mybatis
 * @description: TODO
 * @date 2020/5/3012:21 AM
 */
public class ResultTest {

    private Configuration configuration;
    private Connection connection;
    private JdbcTransaction jdbcTransaction;
    private MappedStatement ms;
    private SqlSessionFactory factory;

    @Before
    public void init() throws SQLException {
        SqlSessionFactoryBuilder factoryBuilder = new SqlSessionFactoryBuilder();
        factory = factoryBuilder.build(StatementHandlerTest.class.getResourceAsStream("/mybatis-config.xml"));
        configuration = factory.getConfiguration();
        jdbcTransaction = new JdbcTransaction(factory.openSession().getConnection());
    }


    //结果集测试
    @Test
    public void resultTest() throws SQLException {
        User user = new User();
        user.setAge("22");
        user.setName("鲁班大叔");
        Executor executor = new SimpleExecutor(configuration, jdbcTransaction);
        MappedStatement mappedStatement = configuration
                .getMappedStatement("org.coderead.mybatis.UserMapper.selectByUser");
        RowBounds rowBounds = RowBounds.DEFAULT;
        BoundSql boundSql = mappedStatement.getBoundSql(user);

        ParameterHandler parameterHandler
                = configuration.newParameterHandler(mappedStatement, user, boundSql);
        ResultHandler resultHandler = Executor.NO_RESULT_HANDLER;
        PreparedStatement ps = jdbcTransaction.getConnection().prepareStatement(boundSql.getSql());
        parameterHandler.setParameters(ps);
        ps.executeQuery();

        // 处理结果集----开始
        ResultSetHandler resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement,
                rowBounds, parameterHandler, resultHandler, boundSql);
        List<Object> list = resultSetHandler.handleResultSets(ps);
        // 处理结果集----结束
        System.out.println(list.get(0));
    }

    @Test
    public void metaObjectTest() {
        Blog blog = new Blog();
        MetaObject metaObject = configuration.newMetaObject(blog);
        metaObject.setValue("author.name", "鲁班大叔");
        MetaObject metaObject1 = configuration.newMetaObject(new ArrayList<>());
        metaObject.setValue("comments", 1);
        System.out.println(blog.getAuthor().getName());
    }
}
