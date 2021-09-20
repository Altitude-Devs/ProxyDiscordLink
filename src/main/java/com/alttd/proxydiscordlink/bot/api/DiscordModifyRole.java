package com.alttd.proxydiscordlink.bot.api;

import com.alttd.proxydiscordlink.DiscordLink;

public class DiscordModifyRole {

    public static void discordAddRole(long userId, long roleId, long guildId) {
        DiscordLink.getPlugin().getBot().addRole(userId, roleId, guildId);
    }

    public static void discordRemoveRole(long userId, long roleId, long guildId) {
        DiscordLink.getPlugin().getBot().removeRole(userId, roleId, guildId);
    }

}
