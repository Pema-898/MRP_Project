package at.technikum.application.common.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    private final String url;
    private final String user;
    private final String password;

    public ConnectionFactory() {
        this(
                System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5433/mrp"),
                System.getenv().getOrDefault("DB_USER", "mrp"),
                System.getenv().getOrDefault("DB_PASSWORD", "mrp")
        );
    }

    public ConnectionFactory(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection open() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
