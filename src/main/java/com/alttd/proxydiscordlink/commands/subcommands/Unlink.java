package com.alttd.proxydiscordlink.commands.subcommands;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.commands.SubCommand;
import com.alttd.proxydiscordlink.config.Config;
import com.alttd.proxydiscordlink.database.Database;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

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

        database.removeLinkedAccount(player);
        player.sendMessage(miniMessage.parse(Config.UNLINKED_ACCOUNTS));
    }

    @Override
    public List<String> suggest(String[] args) {
        return null;
    }

    @Override
    public String getHelpMessage() {
        return Config.HELP_UNLINK;
    }
}
