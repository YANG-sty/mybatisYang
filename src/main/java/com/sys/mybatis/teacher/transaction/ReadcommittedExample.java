package com.sys.mybatis.teacher.transaction;
/**
 * Created by tommy
 */

import java.sql.*;



/**
 * Connection.TRANSACTION_READ_COMMITTED
 * 允许读取已提交事物
 *
 * @author Tommy
 * Created by Tommy on 2018/8/27
 **/
public class ReadcommittedExample {
    public static final String URL = "jdbc:mysql://47.99.214.246:3899/blog";
    public static final String USERNAME = "luban";
    public static final String PASSWORD = "coderead";

    public static Connection openConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);

        return conn;
    }
    static {
        try {
            openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Object lock = new Object();



    public static void insert(String accountName, String name, int money) {
        try {
            Connection conn = openConnection();
            conn.setAutoCommit(true);
            PreparedStatement prepare = conn.
                    prepareStatement("insert INTO account (accountName,user,money) VALUES (?,?,?)");
            prepare.setString(1, accountName);
            prepare.setString(2, name);
            prepare.setInt(3, money);
            prepare.executeUpdate();
            System.out.println("执行插入成功");
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void select(String name, Connection conn) {
        try {
            PreparedStatement prepare = conn.
                    prepareStatement("SELECT * from account where user=?");
            prepare.setString(1, name);
            ResultSet resultSet = prepare.executeQuery();
            System.out.println("执行查询");
            while (resultSet.next()) {
                for (int i = 1; i <= 4; i++) {
                    System.out.print(resultSet.getString(i) + ",");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Thread run(Runnable runnable) {
        Thread thread1 = new Thread(runnable);
        thread1.start();
        return thread1;
    }



    public static void main(String[] args) {
        Thread t1 = run(() -> {
            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            insert("1111", "luban", 10000);
        });

        Thread t2 = run(() -> {
            try {
                Connection conn = openConnection();
                // 第一次读取不到
                select("luban", conn);
                synchronized (lock) {
                    lock.notify();
                }
                // 第二次读取到(数据不一至)
                Thread.sleep(500);
                select("luban", conn);
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            t1.join();
            t2.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
