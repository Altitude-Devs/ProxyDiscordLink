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
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Sync implements SubCommand { //TODO implement

    private final String name;
    private final String permission;
    private final MiniMessage miniMessage;

    public Sync() { //TODO finish syncing roles to db and init command
        name = "sync";
        permission = "discordlink.sync";
        miniMessage = MiniMessage.miniMessage();
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
            source.sendMessage(miniMessage.deserialize(Config.NO_CONSOLE));
            return;
        }
        if (!player.hasPermission(getPermission())) {
            source.sendMessage(miniMessage.deserialize(Config.NO_PERMISSION));
            return;
        }
        User user = Utilities.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            source.sendMessage(miniMessage.deserialize(Config.INVALID_PLAYER));
            return;
        }
        player.sendMessage(syncAccounts(user));
    }

    private Component syncAccounts(User user) {
        Database database = DiscordLink.getPlugin().getDatabase();

        if (!database.playerIsLinked(user.getUniqueId())) {
            return miniMessage.deserialize(Config.ACCOUNTS_NOT_LINKED);
        }

        DiscordLinkPlayer discordLinkPlayer = DiscordLinkPlayer.getDiscordLinkPlayer(user.getUniqueId());
        List<DiscordRole> discordRoles = DiscordRole.getDiscordRoles().stream().filter(DiscordRole::isUpdateToDiscord).collect(Collectors.toList());
        Collection<InheritanceNode> nodes = user.getNodes(NodeType.INHERITANCE);

        //TODO reset all roles and add them again, maybe relinking them is easier???
//        return miniMessage.deserialize(Config.SYNCHED_ACCOUNTS);
        return (null);
    }

    @Override
    public List<String> suggest(String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Config.HELP_SYNC;
    }
}
