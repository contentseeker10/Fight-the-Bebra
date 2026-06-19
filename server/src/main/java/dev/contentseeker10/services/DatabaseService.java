package dev.contentseeker10.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseService {

    private static final DatabaseService INSTANCE = new DatabaseService();
    private DatabaseService() { initializeDatabase(); }
    public static DatabaseService getInstance() { return INSTANCE; }

    private static final String dbUrl = "jdbc:sqlite:database.db";

    private void initializeDatabase() {
        try (Connection connection = getConnection(); Statement st = connection.createStatement()) {
            st.execute("""
                        CREATE TABLE IF NOT EXISTS users (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT UNIQUE NOT NULL,
                            password_hash TEXT NOT NULL,
                            record_score INTEGER DEFAULT 0 NOT NULL,
                            death_count INTEGER DEFAULT 0 NOT NULL
                        );
                        """);
            System.out.println("[SERVER] Database on SQLite successfully initialized");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

}
