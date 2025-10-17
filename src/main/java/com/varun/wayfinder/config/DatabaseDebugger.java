package com.varun.wayfinder.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Component
public class DatabaseDebugger implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String url = meta.getURL();
            System.out.println("========================================");
            System.out.println("ACTUAL DATABASE URL: " + url);
            System.out.println("========================================");

            // Extract the file path from the URL
            if (url.startsWith("jdbc:sqlite:")) {
                String dbPath = url.substring("jdbc:sqlite:".length());
                System.out.println("DATABASE FILE PATH: " + dbPath);
                System.out.println("========================================");
            }
        }
    }
}