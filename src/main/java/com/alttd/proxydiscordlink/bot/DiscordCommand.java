package com.alttd.proxydiscordlink.bot;

import com.alttd.proxydiscordlink.bot.commands.*;
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
    public abstract long getChannel();

    public abstract void handleCommand(Message message, String sender, String command, String[] args);

    public static void loadCommands() {
        commands = new ArrayList<>();

        loadCommand(new DiscordLinkCommand(),
                new DiscordUnlink(),
                new DiscordNick(),
                new DiscordStaffList(),
                new DiscordServerList()
               );
    }

    private static void loadCommand(DiscordCommand ... discordCommands) {
        Collections.addAll(commands, discordCommands);
    }

    public static List<DiscordCommand> getCommands() {
        return commands;
    }
}
