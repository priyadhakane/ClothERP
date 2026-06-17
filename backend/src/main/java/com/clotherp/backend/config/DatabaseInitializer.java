package com.clotherp.backend.config;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseInitializer {

    public static void initialize() {
        try {
            String ymlContent = "";
            String userDir = System.getProperty("user.dir");
            java.nio.file.Path path = Paths.get(userDir, "src", "main", "resources", "application.yml");
            if (Files.exists(path)) {
                ymlContent = Files.readString(path);
            } else {
                try (InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream("application.yml")) {
                    if (is != null) {
                        ymlContent = new String(is.readAllBytes());
                    }
                }
            }

            if (ymlContent.isEmpty()) {
                System.err.println("Could not find application.yml to initialize database.");
                return;
            }

            String url = getValue(ymlContent, "url");
            String username = getValue(ymlContent, "username");
            String password = getValue(ymlContent, "password");

            if (url == null || url.isEmpty()) {
                System.err.println("No datasource url found in application.yml");
                return;
            }

            Pattern pattern = Pattern.compile("jdbc:postgresql://([^:/]+):?(\\d*)/([^?\\s]+)");
            Matcher matcher = pattern.matcher(url);
            if (!matcher.find()) {
                System.err.println("Invalid PostgreSQL JDBC URL: " + url);
                return;
            }

            String host = matcher.group(1);
            String port = matcher.group(2);
            String dbName = matcher.group(3);

            if (port == null || port.isEmpty()) {
                port = "5432";
            }

            String serverUrl = "jdbc:postgresql://" + host + ":" + port + "/postgres";
            System.out.println("Connecting to PostgreSQL server: " + serverUrl);

            try (Connection conn = DriverManager.getConnection(serverUrl, username, password);
                 Statement stmt = conn.createStatement()) {

                var rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'");
                if (!rs.next()) {
                    System.out.println("Database '" + dbName + "' does not exist. Creating database...");
                    stmt.executeUpdate("CREATE DATABASE " + dbName);
                    System.out.println("Database '" + dbName + "' created successfully.");
                } else {
                    System.out.println("Database '" + dbName + "' already exists.");
                }
            }

            String targetUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
            System.out.println("Connecting to database '" + dbName + "' to ensure schema exists: " + targetUrl);
            try (Connection conn = DriverManager.getConnection(targetUrl, username, password);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE SCHEMA IF NOT EXISTS core");
                System.out.println("Schema 'core' verified/created successfully.");
            }

        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getValue(String content, String key) {
        Pattern p = Pattern.compile("^\\s*" + key + "\\s*:\\s*[\"']?([^\"'\\r\\n]+)[\"']?", Pattern.MULTILINE);
        Matcher m = p.matcher(content);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }
}
