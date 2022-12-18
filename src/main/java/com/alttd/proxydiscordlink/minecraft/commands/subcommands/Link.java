package com.alttd.proxydiscordlink.minecraft.commands.subcommands;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.minecraft.commands.SubCommand;
import com.alttd.proxydiscordlink.config.Config;
import com.alttd.proxydiscordlink.database.Database;
import com.alttd.proxydiscordlink.util.Utilities;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.ArrayList;
import java.util.List;

public class Link implements SubCommand {

    private final String name;
    private final String permission;
    private final MiniMessage miniMessage;

    public Link() {
        name = "link";
        permission = "discordlink.link";
        miniMessage = MiniMessage.miniMessage();
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
            source.sendMessage(miniMessage.deserialize(Config.MESSAGES.NO_CONSOLE));
            return;
        }
        if (!player.hasPermission(getPermission())) {
            source.sendMessage(miniMessage.deserialize(Config.MESSAGES.NO_PERMISSION));
            return;
        }

        startLinkAccounts(player);
    }

    private void startLinkAccounts(Player player) {
        Database database = DiscordLink.getPlugin().getDatabase();

        if (database.playerIsLinked(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize(Config.MESSAGES.ALREADY_LINKED_ACCOUNTS));
            return;
        }
        String authCode = DiscordLink.getPlugin().getCache().getCode(player.getUniqueId());
        if (authCode != null) {
            player.sendMessage(miniMessage.deserialize(Config.MESSAGES.ALREADY_GOT_CODE, Placeholder.unparsed("code", authCode)));
            return;
        }

        authCode = Utilities.getAuthKey();

        player.sendMessage(miniMessage.deserialize(Config.MESSAGES.GIVE_CODE, Placeholder.unparsed("code", authCode)));
        DiscordLink.getPlugin().getCache()
                .cacheCode(player.getUniqueId(), authCode);
    }

    @Override
    public List<String> suggest(String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Config.MESSAGES.HELP_LINK;
    }
}
