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

    public DiscordLinkPlayer(long userId, UUID uuid, String username, String discordUsername, boolean nick, List<String> roleNames) {
        this.userId = userId;
        this.uuid = uuid;
        this.username = username;
        this.roleNames = roleNames;
        this.discordUsername = discordUsername;
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
            roles.stream().filter(DiscordRole::isUpdateToDiscord).forEach(role -> DiscordLink.getPlugin().getBot().addRole(userId, role.getId(), BotConfig.GUILD_ID)); //TODO test
        else
            roles.stream().filter(DiscordRole::isUpdateToDiscord).forEach(role -> DiscordLink.getPlugin().getBot().removeRole(userId, role.getId(), BotConfig.GUILD_ID)); //TODO test

        DiscordLink.getPlugin().getDatabase().syncPlayer(this);
        //TODO implement
        //TODO SYNC ROLES TO DATABASE
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
        DiscordLink.getPlugin().getDatabase().syncPlayer(this);
        //TODO implement
        //TODO SYNC ROLES TO DATABASE
    }

    public void linkedRole(boolean add) {
        if (add)
            DiscordLink.getPlugin().getBot().addRole(userId, BotConfig.LINKED_ROLE_ID, BotConfig.GUILD_ID); //TODO test
        else
            DiscordLink.getPlugin().getBot().removeRole(userId, BotConfig.LINKED_ROLE_ID, BotConfig.GUILD_ID); //TODO test
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

    public static void removeDiscordLinkPlayer(DiscordLinkPlayer discordLinkPlayer) {
        discordLinkPlayers.remove(discordLinkPlayer);
    }

    public static DiscordLinkPlayer getDiscordLinkPlayer(long userId) {
        return discordLinkPlayers.stream()
                .filter(discordLinkPlayer -> discordLinkPlayer.getUserId() == userId)
                .findFirst()
                .orElseGet(() -> DiscordLink.getPlugin().getDatabase().getPlayer(userId));
    }

    public static DiscordLinkPlayer getDiscordLinkPlayer(UUID uuid) {
        return discordLinkPlayers.stream()
                .filter(o1 -> o1.getUuid().equals(uuid))
                .findFirst()
                .orElseGet(() -> DiscordLink.getPlugin().getDatabase().getPlayer(uuid));
    }
}
