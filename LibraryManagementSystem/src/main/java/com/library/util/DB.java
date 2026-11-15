package com.library.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public final class DB {
    private static final Properties PROPS = new Properties();
    static {
        try (InputStream in = DB.class.getResourceAsStream("/db.properties")) {
            if (in == null) throw new IllegalStateException("db.properties not found");
            PROPS.load(in);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    private DB() {}
    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(PROPS.getProperty("db.url"),
            PROPS.getProperty("db.username"), PROPS.getProperty("db.password"));
    }
}
