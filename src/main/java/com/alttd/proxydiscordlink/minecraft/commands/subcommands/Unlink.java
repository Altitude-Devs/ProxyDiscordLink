package com.alttd.proxydiscordlink.minecraft.commands.subcommands;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.objects.DiscordRole;
import com.alttd.proxydiscordlink.config.Config;
import com.alttd.proxydiscordlink.database.Database;
import com.alttd.proxydiscordlink.minecraft.commands.SubCommand;
import com.alttd.proxydiscordlink.objects.DiscordLinkPlayer;
import com.alttd.proxydiscordlink.util.Utilities;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
        if (args.length > 1) {
            unlinkOther(args, source);
        }
        if (!(source instanceof Player player)) {
            source.sendMessage(miniMessage.parse(Config.NO_CONSOLE));
            return;
        }
        if (!player.hasPermission(getPermission())) {
            source.sendMessage(miniMessage.parse(Config.NO_PERMISSION));
            return;
        }

        player.sendMessage(unlinkAccounts(player.getUniqueId()));
    }

    private void unlinkOther(String[] args, CommandSource source) {
        if (!source.hasPermission(getPermission() + ".other")) {
            source.sendMessage(miniMessage.parse(Config.NO_PERMISSION));
            return;
        }
        User user = Utilities.getLuckPerms().getUserManager().getUser(args[1]);
        if (user == null) {
            source.sendMessage(miniMessage.parse(Config.INVALID_PLAYER));
            return;
        }

        unlinkAccounts(user.getUniqueId());
    }

    private Component unlinkAccounts(UUID uuid) {
        Database database = DiscordLink.getPlugin().getDatabase();

        if (!database.playerIsLinked(uuid)) {
            return miniMessage.parse(Config.ACCOUNTS_NOT_LINKED);
        }

        DiscordLinkPlayer discordLinkPlayer = DiscordLinkPlayer.getDiscordLinkPlayer(uuid);

        discordLinkPlayer.setActive(false);
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
        return miniMessage.parse(Config.UNLINKED_ACCOUNTS);
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
