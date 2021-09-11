package com.alttd.proxydiscordlink.objects;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.objects.DiscordRole;
import com.alttd.proxydiscordlink.database.Database;
import com.alttd.proxydiscordlink.util.ALogger;
import com.alttd.proxydiscordlink.util.Utilities;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeBuilder;
import net.luckperms.api.node.types.PermissionNode;

import java.util.*;

public class DiscordLinkPlayer {
    private final long userId;
    private final UUID uuid;
    private final String username;
    private final String discordUsername;
    private boolean isDonor;
    private boolean isNitro;

    public DiscordLinkPlayer(long userId, UUID uuid, String username, String discordUsername, boolean isDonor, boolean isNitro) {
        this.userId = userId;
        this.uuid = uuid;
        this.username = username;
        this.isDonor = isDonor;
        this.isNitro = isNitro;
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

    public boolean isDonor() {
        return isDonor;
    }

    public boolean isNitro() {
        return isNitro;
    }

    public String getDiscordUsername() {
        return discordUsername;
    }

    public void update(List<DiscordRole> roles, boolean added) {
        User user = Utilities.getLuckPerms().getUserManager().getUser(getUuid());
        if (user == null)
        {
            ALogger.error("Tried updating a user luckperms couldn't find: " + getUuid());
            return;
        }

        for (DiscordRole role : roles) {
            if (role.getDisplayName().equalsIgnoreCase("nitro"))
                isNitro = added; //FIXME this should be a list instead of a bool (separate table for roles they have)
            if (List.of("viceroy", "count", "duke", "archduke").contains(role.getDisplayName().toLowerCase()))
                isDonor = added; //FIXME this should be a list instead of a bool (separate table for roles they have)
            if (!role.isUpdateToMinecraft())
                continue;
            PermissionNode group = PermissionNode.builder("group." + role.getLuckpermsName()).build();
            if (!user.getNodes().contains(group))
                if (added)
                    user.getNodes().add(group);
                else
                    user.getNodes().remove(group);
        }
        DiscordLink.getPlugin().getDatabase().syncPlayer(this);
        //TODO implement
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
                .orElse(null);
    }

    public static DiscordLinkPlayer getDiscordLinkPlayer(UUID uuid) {
        return discordLinkPlayers.stream()
                .filter(o1 -> o1.getUuid().equals(uuid))
                .findFirst()
                .orElseGet(() -> DiscordLink.getPlugin().getDatabase().getPlayer(uuid));
    }
}
