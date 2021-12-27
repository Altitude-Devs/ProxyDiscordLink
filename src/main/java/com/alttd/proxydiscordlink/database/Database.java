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
                "`active` BIT DEFAULT b'1', " +
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
        //Remove any accounts with the players uuid/id
        removeLinkedAccount(player);
        try {
            String sql = "INSERT INTO linked_accounts " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "player_uuid = ?, " +
                    "discord_id = ?";

            PreparedStatement statement = DatabaseConnection.getConnection().prepareStatement(sql);

            //Insert
            statement.setString(1, player.getUuid().toString());
            statement.setString(2, player.getUsername());
            statement.setString(3, player.getDiscordUsername());
            statement.setLong(4, player.getUserId());
            statement.setInt(5, player.hasNick() ? 1 : 0);
            statement.setInt(6, player.isActive() ? 1 : 0);
            //Update
            statement.setString(7, player.getUuid().toString());
            statement.setLong(8, player.getUserId());

            statement.execute();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void syncRoles(DiscordLinkPlayer player) {
        //Delete all roles
        try {
            String sql = "DELETE FROM account_roles WHERE uuid = ?";

            PreparedStatement statement = DatabaseConnection.getConnection().prepareStatement(sql);
            statement.setString(1, player.getUuid().toString());
            statement.execute();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        //Add all roles back
        try {
            String sql = "INSERT INTO account_roles " +
                    "VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "uuid = ?, " +
                    "role_name = ?";

            PreparedStatement statement = DatabaseConnection.getConnection().prepareStatement(sql);

            statement.setString(1, player.getUuid().toString());
            statement.setString(3, player.getUuid().toString());

            for (String role : player.getRoles()) {
                statement.setString(2, role);
                statement.setString(4, role);
                statement.addBatch();
            }

            statement.executeBatch();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public boolean playerIsLinked(Player player) { //TODO maybe this can be using the discord api instead? (or a cache idk)
        try {
            PreparedStatement statement = DatabaseConnection.getConnection()
                    .prepareStatement("SELECT * FROM linked_accounts WHERE player_uuid = ?");
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return true;
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return false;
    }

    public boolean playerIsLinked(long id) { //TODO maybe this can be using the discord api instead? (or a cache idk)
        try {
            PreparedStatement statement = DatabaseConnection.getConnection()
                    .prepareStatement("SELECT * FROM linked_accounts WHERE discord_id = ?");
            statement.setLong(1, id);
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
                    .prepareStatement("DELETE FROM linked_accounts WHERE discord_id = ? OR uuid = ?");
            statement.setLong(1, player.getUserId());
            statement.setString(2, player.getUuid().toString());
            statement.executeUpdate();
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
                    resultSet.getInt("active") == 1,
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

    public void setActive(UUID uuid, boolean active) {
        String sql = "UPDATE linked_accounts SET active = ? WHERE player_uuid = ?";
        try {
            PreparedStatement statement = DatabaseConnection.getConnection().prepareStatement(sql);
            statement.setInt(1, active ? 1 : 0);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
