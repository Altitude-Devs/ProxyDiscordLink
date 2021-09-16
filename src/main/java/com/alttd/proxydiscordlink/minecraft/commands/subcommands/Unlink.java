package com.alttd.proxydiscordlink.minecraft.commands.subcommands;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.objects.DiscordRole;
import com.alttd.proxydiscordlink.minecraft.commands.SubCommand;
import com.alttd.proxydiscordlink.config.Config;
import com.alttd.proxydiscordlink.database.Database;
import com.alttd.proxydiscordlink.objects.DiscordLinkPlayer;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Unlink implements SubCommand {

    private final String name;
    private final String permission;
    private final MiniMessage miniMessage;

    public Unlink() {
        name = "unlink";
        permission = "discordlink.unlink";
        miniMessage = MiniMessage.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public void execute(String[] args, CommandSource source) {
        if (!(source instanceof Player player)) {
            source.sendMessage(miniMessage.parse(Config.NO_CONSOLE));
            return;
        }
        if (!player.hasPermission(getPermission())) {
            source.sendMessage(miniMessage.parse(Config.NO_PERMISSION));
            return;
        }

        unlinkAccounts(player);
    }

    private void unlinkAccounts(Player player) {
        Database database = DiscordLink.getPlugin().getDatabase();

        if (!database.playerIsLinked(player)) {
            player.sendMessage(miniMessage.parse(Config.ACCOUNTS_NOT_LINKED));
            return;
        }

        DiscordLinkPlayer discordLinkPlayer = DiscordLinkPlayer.getDiscordLinkPlayer(player.getUniqueId());
        database.removeLinkedAccount(discordLinkPlayer.getUuid());

        discordLinkPlayer.updateDiscord(
                DiscordRole.getDiscordRoles().stream()
                        .filter(role -> discordLinkPlayer.getRoles().contains(role.getInternalName()))
                        .collect(Collectors.toList()),
                false);
        discordLinkPlayer.updateMinecraft(
                DiscordRole.getDiscordRoles().stream()
                        .filter(role -> discordLinkPlayer.getRoles().contains(role.getInternalName()))
                        .collect(Collectors.toList()),
                false);
        player.sendMessage(miniMessage.parse(Config.UNLINKED_ACCOUNTS));
    }

    @Override
    public List<String> suggest(String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Config.HELP_UNLINK;
    }
}
