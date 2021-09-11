package com.alttd.proxydiscordlink.minecraft.commands.subcommands;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.minecraft.commands.SubCommand;
import com.alttd.proxydiscordlink.config.Config;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

public class Reload implements SubCommand {

    private final String name;
    private final String permission;
    private final MiniMessage miniMessage;

    public Reload() {
        name = "reload";
        permission = "discordlink.reload";
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
        DiscordLink.getPlugin().reloadConfig();
        source.sendMessage(miniMessage.parse(Config.RELOAD_CONFIG));
    }

    @Override
    public List<String> suggest(String[] args) {
        return null;
    }

    @Override
    public String getHelpMessage() {
        return Config.HELP_RELOAD;
    }
}
