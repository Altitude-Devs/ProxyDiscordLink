package com.alttd.proxydiscordlink.bot;

import com.alttd.proxydiscordlink.bot.commands.DiscordLinkCommand;
import com.alttd.proxydiscordlink.bot.commands.DiscordServerList;
import com.alttd.proxydiscordlink.bot.commands.DiscordStaffList;
import com.alttd.proxydiscordlink.bot.commands.DiscordUnlink;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DiscordCommand {

    private static List<DiscordCommand> commands;

    public abstract String getCommand();
    public abstract String getPermission();// TODO discord and LP permissions
    public abstract String getDescription();
    public abstract String getSyntax();

    public abstract void handleCommand(Message message, String sender, String command, String[] args);

    public static void loadCommands() {
        commands = new ArrayList<>();

        loadCommand(new DiscordStaffList(),
                new DiscordServerList(),
                new DiscordLinkCommand(),
                new DiscordUnlink()
               );
    }

    private static void loadCommand(DiscordCommand ... discordCommands) {
        Collections.addAll(commands, discordCommands);
    }

    public static List<DiscordCommand> getCommands() {
        return commands;
    }
}