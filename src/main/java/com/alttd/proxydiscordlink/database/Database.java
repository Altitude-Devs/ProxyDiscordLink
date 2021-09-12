package com.alttd.proxydiscordlink.database;

import com.alttd.proxydiscordlink.objects.DiscordLinkPlayer;
import com.alttd.proxydiscordlink.util.ALogger;
import com.alttd.proxydiscordlink.util.Utilities;
import com.velocitypowered.api.proxy.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class Database {

    public void createTables() {
        String linked_accounts = "CREATE TABLE IF NOT EXISTS linked_accounts (" +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "player_nickname VARCHAR(16), " +
                "player_rank VARCHAR(256), " +
                "player_isdonor BIT NOT NULL, " +
                "player_isnitro BIT NOT NULL, " +
                "discord_username VARCHAR(256) NOT NULL, " +
                "discord_id VARCHAR(256) NOT NULL, " +
                "PRIMARY KEY(player_uuid)" +
                ");";
        String cache = "CREATE TABLE IF NOT EXISTS cache (" +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "player_nickname VARCHAR(16), " +
                "player_rank VARCHAR(256), " +
                "player_isdonor BIT NOT NULL, " +
                "code VARCHAR(6) NOT NULL, " +
                "PRIMARY KEY(player_uuid)" +
                ");";
        String updates = "CREATE TABLE IF NOT EXISTS `updates` (" +
                "`player_uuid` varchar(36) NOT NULL, " +
                "`player_name` varchar(16) NOT NULL, " +
                "`player_nickname` varchar(16) DEFAULT NULL, " +
                "`player_rank` varchar(256) DEFAULT NULL, " +
                "`player_isdonor` bit(1) DEFAULT b'0', " +
                "`player_isnitro` bit(1) DEFAULT b'0', " +
                "`discord_username` varchar(256) DEFAULT NULL, " +
                "`discord_id` varchar(256) DEFAULT NULL, " +
                "`discord_update` bit(2) NOT NULL DEFAULT b'0', " +
                "`minecraft_update` bit(2) NOT NULL DEFAULT b'0', " +
                "PRIMARY KEY (`player_uuid`)" +
                ")";
        try {
            Statement statement = DatabaseConnection.getConnection().createStatement();
            statement.execute(linked_accounts);
            statement.execute(cache);
            statement.execute(updates);
        } catch (SQLException var3) {
            var3.printStackTrace();
        }
    }

    public void syncPlayer(DiscordLinkPlayer player) {
        try {
            String playerNickname = getNick(player.getUuid());
            String sql = "INSERT INTO updates " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, 1) " +
                    "ON DUPLICATE KEY UPDATE player_uuid = ?";

            PreparedStatement statement = DatabaseConnection.getConnection().prepareStatement(sql);

            String uuid = player.getUuid().toString();

            statement.setString(1, player.getUuid().toString());
            statement.setString(2, player.getUsername());
            statement.setString(3, playerNickname);
            statement.setString(4, Utilities.getRankName(player.getUuid()));
            statement.setInt(5, player.isDonor() ? 1 : 0);
            statement.setInt(6, player.isNitro() ? 1 : 0);
            statement.setString(7, player.getDiscordUsername());
            statement.setLong(8, player.getUserId());
            statement.setString(9, uuid);

            statement.execute();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public boolean playerIsLinked(Player player) { //TODO maybe this can be using the discord api instead? (or a cache idk)
        try {
            PreparedStatement statement = DatabaseConnection.getConnection()
                    .prepareStatement("SELECT * FROM linked_accounts WHERE player_uuid = '" + player.getUniqueId().toString() + "'");
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return true;
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return false;
    }

    public void removeLinkedAccount(DiscordLinkPlayer player) {
        try {
            PreparedStatement statement = DatabaseConnection.getConnection()
                    .prepareStatement("DELETE FROM linked_accounts WHERE player_uuid = ?");
            statement.setString(1, player.getUuid().toString());
            statement.execute();

            statement = DatabaseConnection.getConnection()
                    .prepareStatement("DELETE FROM name_type WHERE discord_id = ?");
            statement.setLong(1, player.getUserId());
            statement.execute();

            statement.close();
        } catch (SQLException var2) {
            var2.printStackTrace();
        }

    }

    public String uuidFromName(String playerName) {
        try {
            PreparedStatement statement = DatabaseConnection.getConnection()
                    .prepareStatement("SELECT player_uuid FROM linked_accounts WHERE player_name = '" + playerName + "'");
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("player_uuid");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public String getNick(UUID uuid) {
        try {
            PreparedStatement statement = DatabaseConnection.getConnection()
                    .prepareStatement("SELECT * FROM nicknames WHERE uuid = ?");
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("nickname")
                        .replaceAll("\\{#[<>0-9a-fA-F]{6,8}}", "")
                        .replaceAll("[&§].", "");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * Should only be used internally
     * @param user_id gets the player with this user id
     * @return null or the requested DiscordLinkPlayer
     */
    public DiscordLinkPlayer getPlayer(long user_id) {
        DiscordLinkPlayer discordLinkPlayer = DiscordLinkPlayer.getDiscordLinkPlayer(user_id);

        if (discordLinkPlayer != null)
            return discordLinkPlayer;
        try {
            PreparedStatement statement = DatabaseConnection.getConnection()
                    .prepareStatement("SELECT * FROM linked_accounts WHERE discord_id = ?");

            statement.setLong(1, user_id);
            return getPlayer(statement.executeQuery());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * Should only be used internally
     * @param uuid gets the player with this uuid
     * @return null or the requested DiscordLinkPlayer
     */
    public DiscordLinkPlayer getPlayer(UUID uuid) {
        try {
            PreparedStatement statement = DatabaseConnection.getConnection()
                    .prepareStatement("SELECT * FROM linked_accounts WHERE player_uuid = ?");

            statement.setString(1, uuid.toString());
            return getPlayer(statement.executeQuery());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private DiscordLinkPlayer getPlayer(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return new DiscordLinkPlayer(
                    resultSet.getLong("discord_id"),
                    UUID.fromString(resultSet.getString("player_uuid")),
                    resultSet.getString("player_name"),
                    resultSet.getString("discord_username"),
                    resultSet.getInt("player_isdonor") == 1,
                    resultSet.getInt("player_isnitro") == 1
            );
        }
        return null;
    }
}
