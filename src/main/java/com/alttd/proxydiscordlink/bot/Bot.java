package com.alttd.proxydiscordlink.bot;

import com.alttd.proxydiscordlink.bot.listeners.DiscordMessageListener;
import com.alttd.proxydiscordlink.bot.listeners.DiscordRoleListener;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.util.ALogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.Nullable;

import javax.security.auth.login.LoginException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Bot {
    private JDA jda = null;

    public void connect() {
        disconnect();
        try {
            jda = JDABuilder
                    .createDefault(BotConfig.DISCORD.BOT_TOKEN)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .build();
            jda.setAutoReconnect(true);
            jda.addEventListener(new DiscordMessageListener(),
                    new DiscordRoleListener());
            DiscordCommand.loadCommands();
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    public void sendMessageToDiscord(long channelid, String message) {
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

    public void sendEmbedToDiscord(long channelid, EmbedBuilder embedBuilder, long secondsTillDelete) {
        //sendMessageToDiscord(client.getTextChannelById(channel), message, blocking);
        TextChannel channel = jda.getTextChannelById(channelid);
        if (jda == null) {
            ALogger.warn("JDA is NULL");
            return;
        }

        if (channel == null) {
            ALogger.warn("Can't send message to NULL channel (" + channelid + ")");
            return;
        }

        if (embedBuilder == null) {
            ALogger.warn("Tried to send a NULL embed");
            return;
        }

        if (!embedBuilder.isValidLength()) {
            ALogger.warn("Tried to send an invalid embed");
            return;
        }

        if (embedBuilder.isEmpty()) {
            ALogger.warn("Tried to send an empty embed");
            return;
        }

        try {
            if (secondsTillDelete < 0) {
                channel.sendMessageEmbeds(embedBuilder.build()).queue();
            } else {
                channel.sendMessageEmbeds(embedBuilder.build()).queue(message -> message.delete().queueAfter(secondsTillDelete, TimeUnit.SECONDS));
            }
        } catch (Exception e) {
            ALogger.error("caught some exception, " + e);
        }
    }

    public void addRole(long userId, long roleId, long guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            ALogger.warn("Unable to find guild " + guildId);
            return;
        }
        Role role = guild.getRoleById(roleId);
        if (role == null) {
            ALogger.warn("Unable to find role " + roleId);
            return;
        }
        Member member = guild.getMemberById(userId);
        if (member == null) {
            guild.retrieveMemberById(userId).queue(m -> addRole(guild, m, role));
            return;
        }
        addRole(guild, member, role);
    }

    private void addRole(Guild guild, Member member, Role role) {
        if (member == null) {
            ALogger.warn("Unable to find member when adding role");
            return;
        }
        guild.addRoleToMember(member, role).queue();
    }

    public void removeRole(long userId, long roleId, long guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            ALogger.warn("Unable to find guild " + guildId);
            return;
        }
        Role role = guild.getRoleById(roleId);
        if (role == null) {
            ALogger.warn("Unable to find role " + roleId);
            return;
        }
        Member member = guild.getMemberById(userId);
        if (member == null) {
            guild.retrieveMemberById(userId).queue(m -> removeRole(guild, m, role));
            return;
        }
        removeRole(guild, member, role);
    }

    private void removeRole(Guild guild, Member member, Role role) {
        if (member == null) {
            ALogger.warn("Unable to find member when removing role");
            return;
        }
        guild.removeRoleFromMember(member, role).queue();
    }

    public boolean changeNick(long guildId, long userId, String nickname) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null)
            return false;
        Member member = guild.getMemberById(userId);
        if (member == null)
            return false;
        try {
            guild.modifyNickname(member, nickname).queue();
        } catch (HierarchyException ignored) {
            ALogger.warn("I can't modify the nickname of those above me.");
            return false;
        }
        return true;
    }

    public void discordUnban(long guildId, long userId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null)
            return;
        if (!guild.getSelfMember().getPermissions().contains(Permission.BAN_MEMBERS)) {
            ALogger.warn("I can't unban members in " + guild.getName() + ".");
            return;
        }
        guild.retrieveBanList().queue(bans -> {
            Optional<Guild.Ban> first = bans.stream()
                    .filter(ban -> ban.getUser().getIdLong() == userId)
                    .findFirst();
            if (first.isEmpty())
                return;
            Guild.Ban ban = first.get();
            if (ban.getReason() != null && ban.getReason().equals("Auto ban due to Minecraft ban"))
                bans.remove(ban);
        });
    }

    public void discordBan(long guildId, long userId, @Nullable String optionalReason) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null)
            return;
        if (!guild.getSelfMember().getPermissions().contains(Permission.BAN_MEMBERS)) {
            ALogger.warn("I can't ban members in " + guild.getName() + ".");
            return;
        }
        Member member = guild.getMemberById(userId);
        if (member == null)
            guild.retrieveMemberById(userId).queue(member1 -> discordBan(member1, optionalReason));
        else
            discordBan(member, optionalReason);
    }

    private void discordBan(Member member, @Nullable String optionalReason) {
        try {
            if (optionalReason == null)
                member.ban(0, TimeUnit.DAYS).queue();
            else
                member.ban(0, TimeUnit.DAYS).reason(optionalReason).queue();
        } catch (InsufficientPermissionException exception) {
            ALogger.warn("Unable to ban " + member.getAsMention() + " : " + member.getId() + " from Discord they might be above me.");
        }
    }
}
