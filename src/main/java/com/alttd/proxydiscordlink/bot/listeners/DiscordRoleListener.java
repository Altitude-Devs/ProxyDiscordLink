package com.alttd.proxydiscordlink.bot.listeners;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.Bot;
import com.alttd.proxydiscordlink.bot.objects.DiscordRole;
import com.alttd.proxydiscordlink.objects.DiscordLinkPlayer;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class DiscordRoleListener extends ListenerAdapter {
    private DiscordLink plugin;
    private final Bot bot;
    private final MiniMessage miniMessage;

    public DiscordRoleListener() {
        plugin = DiscordLink.getPlugin();
        bot = plugin.getBot();
        miniMessage = MiniMessage.get();
    }

    /**
     * Checks if the user needs to be updated based on the roles that got added
     * TODO If the user isn't linked maybe ask them to link?
     * Announces that the user got a role assigned if the role has an announcement for it.
     */
    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        List<DiscordRole> added_roles = DiscordRole.getDiscordRoles().stream()
                .filter(discordRole -> event.getRoles().stream()
                        .anyMatch(role -> role.getIdLong() == discordRole.getId()))
                .collect(Collectors.toList());

        if (added_roles.isEmpty())
            return;

        DiscordLinkPlayer player = DiscordLinkPlayer.getDiscordLinkPlayer(event.getUser().getIdLong());

        if (player == null) {
            //TODO mayb ask the player to link to get their in game rank?
            return;
        }

        player.updateMinecraft(added_roles, true);
        added_roles.forEach(discordRole -> {
            if (!discordRole.getAnnouncement().isEmpty()) {
                Component component = miniMessage.parse(
                        discordRole.getAnnouncement(),
                        Template.of("player", player.getUsername()));

                DiscordLink.getPlugin().getProxy().getAllPlayers()
                        .forEach(onlinePlayer -> onlinePlayer.sendMessage(component));
            }
        });
    }

    /**
     * Checks if the user needs to be updated based on the roles that got removed
     */
    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        List<DiscordRole> removed_roles = DiscordRole.getDiscordRoles().stream()
                .filter(discordRole -> event.getRoles().stream()
                        .anyMatch(role -> role.getIdLong() == discordRole.getId()))
                .collect(Collectors.toList());
        if (removed_roles.isEmpty())
            return;

        DiscordLinkPlayer.getDiscordLinkPlayer(event.getUser().getIdLong())
                .updateMinecraft(removed_roles, false);
    }
}
