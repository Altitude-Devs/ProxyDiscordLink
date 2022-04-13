package com.alttd.proxydiscordlink.bot.commands;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.Bot;
import com.alttd.proxydiscordlink.bot.DiscordCommand;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.util.Utilities;
import net.dv8tion.jda.api.entities.Message;

public class DiscordBroadCast extends DiscordCommand {

    private DiscordLink plugin;
    private final Bot bot;

    public DiscordBroadCast() {
        plugin = DiscordLink.getPlugin();
        bot = plugin.getBot();
    }

    @Override
    public String getCommand() {
        return "broadcast";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Broadcast a message to all online players";
    }

    @Override
    public String getSyntax() {
        return "broadcast";
    }

    @Override
    public long getChannel() {
        return 0;
    }

    @Override
    public void handleCommand(Message message, String sender, String command, String[] args) {
        //TODO also send this to the bot channel, optional command args for color and decoration?
        String msg = String.join(" ", args);
        bot.sendMessageToDiscord(BotConfig.COMMAND_CHANNEL, msg);
        Utilities.broadcast(msg);
    }
}
