package chill.script.runtime;

import chill.utils.TypedMap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Sqlite {
    public static final TypedMap.Key<Sqlite> RT_KEY = new TypedMap.Key<>();

    public Sqlite() {
    }

    public Connection getConnection(String database) throws SQLException {
        var connection = DriverManager.getConnection("jdbc:h2:./" + database);
//        try (var statement = connection.createStatement()) {
//            statement.execute("PRAGMA foreign_keys = ON");
//            statement.execute("PRAGMA journal_mode = WAL");
//        }
        return connection;
    }
}
