package com.sys.mybatis.teacher;

import com.sys.mybatis.teacher.bean.User;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.SimpleStatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author tommy
 * @title: StatementHandlerTest
 * @projectName coderead-mybatis
 * @description: SQL处理器测试
 * @date 2020/5/293:03 PM
 */
public class StatementHandlerTest {
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


    //  预处理SQL 动态参数
    @Test
    public void prepareStatementTest() throws SQLException {
        Executor executor = new SimpleExecutor(configuration, jdbcTransaction);
        MappedStatement mappedStatement = configuration
                .getMappedStatement("org.coderead.mybatis.UserMapper.selectByid");
        Object parameter = 10;
        RowBounds rowBounds = RowBounds.DEFAULT;
        ResultHandler resultHandler = Executor.NO_RESULT_HANDLER;
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);

        PreparedStatementHandler handler = new PreparedStatementHandler(executor, mappedStatement,
                parameter, rowBounds, resultHandler, boundSql);
        // 初始化
        Statement statement = handler.prepare(jdbcTransaction.getConnection(), 2);
        // 设置参数
        handler.parameterize(statement);
        // 执行查询
        List<Object> query = handler.query(statement, resultHandler);
        System.out.println(query.get(0));
    }

    // 静态SQL 参数固定
    @Test
    public void SimpleStatementTest() throws SQLException {
        Executor executor = new SimpleExecutor(configuration, jdbcTransaction);
        MappedStatement mappedStatement = configuration
                .getMappedStatement("org.coderead.mybatis.UserMapper.selectByid2");
        Object parameter = 10;
        RowBounds rowBounds = RowBounds.DEFAULT;
        ResultHandler resultHandler = Executor.NO_RESULT_HANDLER;
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        SimpleStatementHandler handler = new SimpleStatementHandler(executor, mappedStatement,
                parameter, rowBounds, resultHandler, boundSql);
        // 初始化
        Statement statement = handler.prepare(jdbcTransaction.getConnection(), 2);
        // 执行查询
        List<Object> query = handler.query(statement, resultHandler);
        System.out.println(query.get(0));
    }
    //SQL注入


    //参数设置测试
    @Test
    public void parameterTest() throws SQLException {
        MappedStatement mappedStatement = configuration
                .getMappedStatement("org.coderead.mybatis.UserMapper.selectByUser");
        User user = new User();
        user.setAge("18");
        user.setName("鲁班大叔");
        BoundSql boundSql = mappedStatement.getBoundSql(user);
        ParameterHandler parameterHandler
                = configuration.newParameterHandler(mappedStatement, user, boundSql);
        PreparedStatement ps = jdbcTransaction.getConnection().prepareStatement(boundSql.getSql());
        parameterHandler.setParameters(ps);
        System.out.println(ps);
    }


}
