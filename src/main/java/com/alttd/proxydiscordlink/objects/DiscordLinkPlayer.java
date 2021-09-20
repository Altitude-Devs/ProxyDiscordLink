package com.alttd.proxydiscordlink.objects;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.objects.DiscordRole;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.util.Utilities;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.InheritanceNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DiscordLinkPlayer {
    private final long userId;
    private final UUID uuid;
    private String username;
    private String discordUsername;
    private boolean nick;
    private final List<String> roleNames;

    public DiscordLinkPlayer(long userId, UUID uuid, String username, String discordUsername, boolean nick, List<String> roleNames) {//TODO what is nick used for? and where is it stored
        this.userId = userId;
        this.uuid = uuid;
        this.username = username;
        this.roleNames = roleNames;
        this.discordUsername = discordUsername;
        this.nick = nick;
    }

    public long getUserId() {
        return userId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roleNames;
    }

    public void addRole(String role) {
        if (!roleNames.contains(role))
            roleNames.add(role);
    }

    public void removeRole(String role) {
        roleNames.remove(role);
    }

    public String getDiscordUsername() {
        return discordUsername;
    }

    public void setDiscordUsername(String discordUsername) {
        this.discordUsername = discordUsername;
    }

    public boolean hasNick() {
        return nick;
    }

    public void setNick(boolean nick) {
        this.nick = nick;
    }

    public void updateDiscord(List<DiscordRole> roles, boolean added) {
        if (added)
            roles.stream().filter(DiscordRole::isUpdateToDiscord).forEach(role -> DiscordLink.getPlugin().getBot().addRole(userId, role.getId(), BotConfig.GUILD_ID));
        else
            roles.stream().filter(DiscordRole::isUpdateToDiscord).forEach(role -> DiscordLink.getPlugin().getBot().removeRole(userId, role.getId(), BotConfig.GUILD_ID));
    }

    public void updateMinecraft(List<DiscordRole> roles, boolean added) {
        UserManager userManager = Utilities.getLuckPerms().getUserManager();

        roles.stream().filter(DiscordRole::isUpdateToMinecraft).forEach(role -> {
            InheritanceNode group = InheritanceNode.builder(role.getLuckpermsName()).build();

            userManager.modifyUser(getUuid(), user -> {
                if (added && !user.getNodes().contains(group))
                    user.data().add(group);
                else if (!added)
                    user.data().remove(group);
            });
        });
    }

    public void linkedRole(boolean add) {
        if (add)
            DiscordLink.getPlugin().getBot().addRole(userId, BotConfig.LINKED_ROLE_ID, BotConfig.GUILD_ID);
        else
            DiscordLink.getPlugin().getBot().removeRole(userId, BotConfig.LINKED_ROLE_ID, BotConfig.GUILD_ID);
    }

    //Static stuff

    private static final List<DiscordLinkPlayer> discordLinkPlayers = new ArrayList<>();

    public static List<DiscordLinkPlayer> getDiscordLinkPlayers() {
        return Collections.unmodifiableList(discordLinkPlayers);
    }

    public static void addDiscordLinkPlayer(DiscordLinkPlayer discordLinkPlayer) {
        if (!discordLinkPlayers.contains(discordLinkPlayer))
            discordLinkPlayers.add(discordLinkPlayer);
    }

    public static DiscordLinkPlayer getDiscordLinkPlayer(long userId) {
        return discordLinkPlayers.stream()
                .filter(discordLinkPlayer -> discordLinkPlayer.getUserId() == userId)
                .findFirst()
                .orElseGet(() -> {
                    DiscordLinkPlayer player = DiscordLink.getPlugin().getDatabase().getPlayer(userId);
                    DiscordLinkPlayer.addDiscordLinkPlayer(player);
                    return player;
                });
    }

    public static DiscordLinkPlayer getDiscordLinkPlayer(UUID uuid) {
        return discordLinkPlayers.stream()
                .filter(o1 -> o1.getUuid().equals(uuid))
                .findFirst()
                .orElseGet(() -> {
                    DiscordLinkPlayer player = DiscordLink.getPlugin().getDatabase().getPlayer(uuid);
                    DiscordLinkPlayer.addDiscordLinkPlayer(player);
                    return player;
                });
    }
}
