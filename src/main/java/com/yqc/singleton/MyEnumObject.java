package com.yqc.singleton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import util.concurrent.Executors;

public class MyEnumObject {
    private static Connection connection;

    private MyEnumObject() {
        try {
            System.out.println("调用MyEnumObject的构造!");
            String url = "";
            String username = "ds";
            String password = "";
            String driveName = "";
            Class.forName(driveName);
            Thread.sleep(1000);
            connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void main(String[] args) {
        Runnable runnable = new MyRunnable3();
        ExecutorService exec = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            exec.execute(runnable);
        }
        exec.shutdown();
    }
}

class MyRunnable3 implements Runnable {

    @Override
    public void run() {
        System.out.println(MyEnumObject.getConnection());
    }
}