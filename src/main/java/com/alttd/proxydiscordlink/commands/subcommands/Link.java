package com.alttd.proxydiscordlink.commands.subcommands;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.commands.SubCommand;
import com.alttd.proxydiscordlink.config.Config;
import com.alttd.proxydiscordlink.database.Database;
import com.alttd.proxydiscordlink.util.Utilities;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;

import java.util.List;

public class Link implements SubCommand {

    private final String name;
    private final String permission;
    private final MiniMessage miniMessage;

    public Link() {
        name = "link";
        permission = "discordlink.link";
        miniMessage = MiniMessage.get();
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    @Override
    public void execute(String[] args, CommandSource source) {
        if (!(source instanceof Player player)) {
            source.sendMessage(miniMessage.parse(Config.CONSOLE));
            return;
        }
        if (!player.hasPermission(getPermission())) {
            source.sendMessage(miniMessage.parse(Config.NO_PERMISSION));
            return;
        }

        linkAccounts(player);
    }

    private void linkAccounts(Player player) {
        Database database = DiscordLink.getPlugin().getDatabase();

        if (database.playerIsLinked(player)) {
            player.sendMessage(miniMessage.parse(Config.ALREADY_LINKED_ACCOUNTS));
            return;
        }
        if (database.isInCache(player)) {
            player.sendMessage(miniMessage.parse(Config.ALREADY_GOT_CODE));
        }

        String authCode = Utilities.getAuthKey();

        player.sendMessage(miniMessage.parse(Config.GIVE_CODE, Template.of("code", authCode)));
        DiscordLink.getPlugin().getCache()
                .cacheCode(player.getUniqueId(), authCode);
        database.storeDataInCache(player, authCode, Utilities.getRankName(player), Utilities.isDonor(player));
    }

    @Override
    public List<String> suggest(String[] args) {
        return null;
    }

    @Override
    public String getHelpMessage() {
        return Config.HELP_LINK;
    }
}
