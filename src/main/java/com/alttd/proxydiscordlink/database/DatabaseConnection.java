package com.alttd.proxydiscordlink.database;

import com.alttd.proxydiscordlink.config.Config;
import com.alttd.proxydiscordlink.DiscordLink;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    public DatabaseConnection() throws SQLException {
        instance = this;
        instance.openConnection();
        DiscordLink.getPlugin().getDatabase().createTables();
    }

    public void openConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            synchronized(this) {
                if (this.connection == null || this.connection.isClosed()) {
                    this.connection = DriverManager.getConnection("jdbc:"
                            + Config.DRIVERS + "://"
                            + Config.IP + ":"
                            + Config.PORT + "/"
                            + Config.DATABASE_NAME
                            + "?autoReconnect=true&useSSL=false", Config.USERNAME, Config.PASSWORD);
                }
            }
        }
    }

    public static Connection getConnection() {
        try {
            instance.openConnection();
        } catch (SQLException var1) {
            var1.printStackTrace();
        }

        return instance.connection;
    }

    public static void initialize() throws SQLException {
        instance = new DatabaseConnection();
    }
}
