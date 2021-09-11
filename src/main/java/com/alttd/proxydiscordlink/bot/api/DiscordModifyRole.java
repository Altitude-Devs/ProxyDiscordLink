package com.alttd.proxydiscordlink.bot.api;

import com.alttd.proxydiscordlink.DiscordLink;

public class DiscordModifyRole {

    public static boolean discordAddRole(long userId, long roleId, long guildId) {
        return DiscordLink.getPlugin().getBot().addRole(userId, roleId, guildId);
    }

}
