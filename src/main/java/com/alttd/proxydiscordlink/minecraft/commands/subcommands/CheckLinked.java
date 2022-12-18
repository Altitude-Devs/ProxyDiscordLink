package com.alttd.proxydiscordlink.minecraft.commands.subcommands;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.config.Config;
import com.alttd.proxydiscordlink.minecraft.commands.SubCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class CheckLinked implements SubCommand {

    private final String name;
    private final String permission;
    private final MiniMessage miniMessage;

    public CheckLinked() {
        name = "checklinked";
        permission = "discordlink.checklinked";
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
        if (!source.hasPermission(getPermission())) {
            source.sendMessage(miniMessage.deserialize(Config.MESSAGES.NO_PERMISSION));
            return;
        }
        if (args.length != 2 || !args[1].matches("\\w{3,16}")) {
            source.sendMessage(miniMessage.deserialize(getHelpMessage()));
            return;
        }

        Optional<Player> optionalPlayer = DiscordLink.getPlugin().getProxy().getPlayer(args[1]);
        if (optionalPlayer.isEmpty())
        {
            String uuidFromName = DiscordLink.getPlugin().getDatabase().uuidFromName(args[1]);
            if (uuidFromName != null)
                optionalPlayer = DiscordLink.getPlugin().getProxy()
                        .getPlayer(UUID.fromString(uuidFromName));
            if (optionalPlayer.isEmpty())
            {
                source.sendMessage(miniMessage.deserialize(Config.MESSAGES.INVALID_PLAYER, Placeholder.unparsed("player", args[1])));
                return;
            }
        }

        isLinked(source, optionalPlayer.get());
    }

    private void isLinked(CommandSource source, Player player) {
        TagResolver tagResolver = TagResolver.resolver(
                Placeholder.unparsed("linked_status", DiscordLink.getPlugin().getDatabase()
                        .playerIsLinked(player.getUniqueId()) ? "linked" : "not linked"),
                Placeholder.unparsed("player", player.getUsername()));

        source.sendMessage(miniMessage.deserialize(Config.MESSAGES.IS_LINKED, tagResolver));
    }

    @Override
    public List<String> suggest(String[] args) {
        return DiscordLink.getPlugin().getProxy().getAllPlayers().stream().map(Player::getUsername).collect(Collectors.toList());
    }

    @Override
    public String getHelpMessage() {
        return Config.MESSAGES.HELP_CHECK_LINKED;
    }
}
