package com.alttd.proxydiscordlink.bot.listeners;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.database.Database;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
public class DiscordLeaveListener extends ListenerAdapter {

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        User user = event.getUser();
        Database database = DiscordLink.getPlugin().getDatabase();
        if (DiscordLink.getPlugin().getDatabase().playerIsLinked(user.getIdLong())) {

        }
    }

}
