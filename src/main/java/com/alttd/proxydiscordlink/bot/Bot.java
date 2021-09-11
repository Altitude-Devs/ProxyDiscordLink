package com.alttd.proxydiscordlink.bot;

import com.alttd.proxydiscordlink.bot.listeners.DiscordMessageListener;
import com.alttd.proxydiscordlink.bot.listeners.DiscordRoleListener;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.util.ALogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.util.concurrent.TimeUnit;

public class Bot {
    private JDA jda = null;

    public void connect() {
        disconnect();
        try {
            jda = JDABuilder
                    .createDefault(BotConfig.BOT_TOKEN)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .build();
            jda.setAutoReconnect(true);
            jda.addEventListener(new DiscordMessageListener(),
                    new DiscordRoleListener());
            DiscordCommand.loadCommands();
        } catch (LoginException e) {
            jda = null;
        }
    }

    public void disconnect() {
        if (jda != null) {
            JDA tmp = jda;
            jda = null;
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignore) {
                }
                tmp.shutdownNow();
            }).start();
        }
    }

    public void sendMessageToDiscord(String channelid, String message) {
        //sendMessageToDiscord(client.getTextChannelById(channel), message, blocking);
        TextChannel channel = jda.getTextChannelById(channelid);
        if (jda == null) return;

        if (channel == null) return;

        if (message == null) return;

        // is this even used/needed?
        //message = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message));

        if (message.isEmpty()) return;

        try {
            channel.sendMessage(message).queue();
        } catch (Exception e) {
            ALogger.error("caught some exception, " + e);
        }
    }

    public void sendEmbedToDiscord(String channelid, EmbedBuilder embedBuilder, long secondsTillDelete) {
        //sendMessageToDiscord(client.getTextChannelById(channel), message, blocking);
        TextChannel channel = jda.getTextChannelById(channelid);
        if (jda == null) return;

        if (channel == null) return;

        if (embedBuilder == null) return;

        if (!embedBuilder.isValidLength()) return;

        if (embedBuilder.isEmpty()) return;
        try {
            if (secondsTillDelete < 0){
                channel.sendMessage(embedBuilder.build()).queue();
            } else {
                channel.sendMessage(embedBuilder.build()).queue(message -> message.delete().queueAfter(secondsTillDelete, TimeUnit.SECONDS));
            }
        } catch (Exception e) {
            ALogger.error("caught some exception, " + e);
        }
    }

    public boolean addRole(long userId, long roleId, long guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null)
            return false;
        Role role = guild.getRoleById(roleId);
        if (role == null)
            return false;
        Member member = guild.getMemberById(userId);
        if (member == null)
            return false;
        guild.addRoleToMember(member, role).queue();
        return true;
    }

    public boolean removeRole(long userId, long roleId, long guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null)
            return false;
        Role role = guild.getRoleById(roleId);
        if (role == null)
            return false;
        Member member = guild.getMemberById(userId);
        if (member == null)
            return false;
        guild.removeRoleFromMember(member, role).queue();
        return true;
    }
}
