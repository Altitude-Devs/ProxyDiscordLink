package com.alttd.proxydiscordlink.database;

import com.alttd.proxydiscordlink.objects.DiscordLinkPlayer;
import com.velocitypowered.api.proxy.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

public class Database {

    public void createTables() {
        String linked_accounts = "CREATE TABLE IF NOT EXISTS linked_accounts (" +
                "`player_uuid` VARCHAR(36) NOT NULL, " +
                "`player_name` VARCHAR(16) NOT NULL, " +
                "`discord_username` VARCHAR(256) NOT NULL, " +
                "`discord_id` BIGINT NOT NULL, " +
                "`nickname` BIT DEFAULT b'0', " +
                "UNIQUE(discord_id), " +
                "PRIMARY KEY(player_uuid)" +
                ");";
        String sync_roles = "CREATE TABLE IF NOT EXISTS account_roles (" +
                "`uuid` VARCHAR(36) NOT NULL, " +
                "`role_name` VARCHAR(32) NOT NULL, " +
                "PRIMARY KEY(uuid, role_name)" +
                ");";
        try {
            Statement statement = DatabaseConnection.getConnection().createStatement();
            statement.execute(linked_accounts);
            statement.execute(sync_roles);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void syncPlayer(DiscordLinkPlayer player) {
        try {
            String sql = "INSERT INTO linked_accounts " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "player_uuid = ?, " +
                    "player_name = ?, " +
                    "discord_username = ?, " +
                    "discord_id = ?, " +
                    "nickname = ? ";

            PreparedStatement statement = DatabaseConnection.getConnection().prepareStatement(sql);

            //Insert
            statement.setString(1, player.getUuid().toString());
            statement.setString(2, player.getUsername());
            statement.setString(3, player.getDiscordUsername());
            statement.setLong(4, player.getUserId());
            statement.setInt(5, player.hasNick() ? 1 : 0);
            //Update
            statement.setString(6, player.getUuid().toString());
            statement.setString(7, player.getUsername());
            statement.setString(8, player.getDiscordUsername());
            statement.setLong(9, player.getUserId());
            statement.setInt(10, player.hasNick() ? 1 : 0);

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

    public void removeLinkedAccount(UUID uuid) {
        try {
            PreparedStatement statement = DatabaseConnection.getConnection()
                    .prepareStatement("DELETE FROM linked_accounts WHERE discord_id = ?");
            statement.setString(1, uuid.toString());
            statement.executeUpdate();

            PreparedStatement statement2 = DatabaseConnection.getConnection()
                    .prepareStatement("DELETE FROM account_roles WHERE discord_id = ?");
            statement2.setString(1, uuid.toString());
            statement2.executeUpdate();
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
     *
     * @param user_id gets the player with this user id
     * @return null or the requested DiscordLinkPlayer
     */
    public DiscordLinkPlayer getPlayer(long user_id) {
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
     *
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
            DiscordLinkPlayer discordLinkPlayer = new DiscordLinkPlayer(
                    resultSet.getLong("discord_id"),
                    UUID.fromString(resultSet.getString("player_uuid")),
                    resultSet.getString("player_name"),
                    resultSet.getString("discord_username"),
                    resultSet.getInt("nickname") == 1,
                    new ArrayList<>()
            );
            addRoles(discordLinkPlayer);
            return discordLinkPlayer;
        }
        return null;
    }

    private void addRoles(DiscordLinkPlayer discordLinkPlayer) {
        try {
            PreparedStatement statement = DatabaseConnection.getConnection()
                    .prepareStatement("SELECT * FROM account_roles WHERE uuid = ?");

            statement.setString(1, discordLinkPlayer.getUuid().toString());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                discordLinkPlayer.getRoles().add(resultSet.getString("role_name"));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
