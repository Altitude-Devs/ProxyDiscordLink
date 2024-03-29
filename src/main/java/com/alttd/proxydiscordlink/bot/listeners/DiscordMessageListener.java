package com.alttd.proxydiscordlink.bot.listeners;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.Bot;
import com.alttd.proxydiscordlink.bot.DiscordCommand;
import com.alttd.proxydiscordlink.config.BotConfig;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;

public class DiscordMessageListener extends ListenerAdapter {

    private final DiscordLink plugin;
    private final Bot bot;

    public DiscordMessageListener() {
        plugin = DiscordLink.getPlugin();
        bot = plugin.getBot();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild())
            return;
        if (event.getAuthor() == event.getJDA().getSelfUser())
            return;
        if (event.isWebhookMessage())
            return;
        /*
        if (event.getMessage().getChannel().getIdLong() == BotConfig.COMMAND_CHANNEL) {
            String content = event.getMessage().getContentRaw();
            if (content.startsWith(BotConfig.prefixMap.get(event.getGuild().getIdLong())) && content.length() > 1) {
                String[] split = content.split(" ");
                String cmd = split[0].substring(1).toLowerCase();
                String[] args = Arrays.copyOfRange(split, 1, split.length);
                for(DiscordCommand command : DiscordCommand.getCommands()) {
                    if(!command.getCommand().equalsIgnoreCase(cmd))
                        continue;
                    if(command.getPermission() != null) {
                        // TODO permission check? do we need this?
                    }
                    command.handleCommand(event.getMessage(), event.getAuthor().getName(), cmd, args);
                }
            }
        } else if (event.getMessage().getChannel().getIdLong() == BotConfig.LINK_CHANNEL) {
            String content = event.getMessage().getContentRaw();
            String[] split = content.split(" ");
            String cmd = split[0].substring(1).toLowerCase();
            String[] args = Arrays.copyOfRange(split, 1, split.length);
            if (cmd.equalsIgnoreCase("link"))
                DiscordCommand.getCommands().stream()
                        .filter(discordCommand -> discordCommand.getCommand().equals("link"))
                        .findFirst()
                        .ifPresent(discordCommand -> discordCommand.handleCommand(event.getMessage(), event.getAuthor().getName(), cmd, args));
        }
         */
        String content = event.getMessage().getContentRaw();
        if (!BotConfig.prefixMap.containsKey(event.getGuild().getIdLong())) return; // early return
        if (content.startsWith(BotConfig.prefixMap.get(event.getGuild().getIdLong())) && content.length() > 1) {
            String[] split = content.split(" ");
            String cmd = split[0].substring(1).toLowerCase();
            String[] args = Arrays.copyOfRange(split, 1, split.length);
            for (DiscordCommand command : DiscordCommand.getCommands()) {
                if (!command.getCommand().equalsIgnoreCase(cmd))
                    continue;
                if (!(event.getMessage().getChannel().getIdLong() == command.getChannel()))
                    continue;
                if (command.getPermission() != null) {
                    // TODO permission check? do we need this?
                }
                command.handleCommand(event.getMessage(), event.getAuthor().getName(), cmd, args);
            }
        }
    }

}
