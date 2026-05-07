package com.eleraky.studentexchange;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseInitializer {

    static void createDatabaseIfNotExists(String datasourceUrl, String datasourceUsername, String datasourcePassword) {
        try {
            // Extract the database name from the URL
            String databaseName = datasourceUrl.substring(datasourceUrl.lastIndexOf("/") + 1);
            System.out.println(databaseName);
            // Create the URL for connecting to the default 'postgres' database
            String postgresUrl = datasourceUrl.replace("/" + databaseName, "/postgres");
            System.out.println(postgresUrl);
            try (Connection conn = DriverManager.getConnection(postgresUrl, datasourceUsername, datasourcePassword);
                 Statement stmt = conn.createStatement()) {

                // Check if database exists
                ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + databaseName + "'");
                if (rs.next()) {
                    System.out.println("Database '" + databaseName + "' already exists.");
                } else {
                    // Create database
                    stmt.execute("CREATE DATABASE " + databaseName);
                    System.out.println("Database '" + databaseName + "' created successfully.");
                }
            }
        } catch (Exception e) {
            System.err.println("Database check/creation failed: " + e.getMessage());
        }
    }
}
