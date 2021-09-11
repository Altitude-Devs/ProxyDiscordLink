package com.alttd.proxydiscordlink.bot.objects;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscordRole {

    private final long id;
    private final String luckpermsName;
    private final String displayName;
    private final boolean updateToMinecraft;
    private final String announcement;

    public DiscordRole(long id, String luckpermsName, String displayName, boolean updateToMinecraft, String announcement) {
        this.id = id;
        this.luckpermsName = luckpermsName;
        this.displayName = displayName;
        this.updateToMinecraft = updateToMinecraft;
        this.announcement = announcement;
    }

    public long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLuckpermsName() {
        return luckpermsName;
    }

    public boolean isUpdateToMinecraft() {
        return updateToMinecraft;
    }

    public String getAnnouncement() {
        return announcement;
    }

    //Static stuff

    private static final List<DiscordRole> discordRoles = new ArrayList<>();

    public static List<DiscordRole> getDiscordRoles() {
        return Collections.unmodifiableList(discordRoles);
    }

    public static void addDiscordRole(DiscordRole discordRole) {
        if (!discordRoles.contains(discordRole))
            discordRoles.add(discordRole);
    }

    public static void removeDiscordRole(DiscordRole discordRole) {
        discordRoles.remove(discordRole);
    }

    public static void cleanDiscordRoles() {
        discordRoles.clear();
    }
}
