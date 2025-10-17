package com.varun.wayfinder;

import java.sql.*;

public class DatabaseChecker {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:E:/CODES/pbl/wayfinder_new/data/users.db";
        
        try (Connection conn = DriverManager.getConnection(url)) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"});
            
            System.out.println("Tables in database:");
            while (tables.next()) {
                System.out.println(tables.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
