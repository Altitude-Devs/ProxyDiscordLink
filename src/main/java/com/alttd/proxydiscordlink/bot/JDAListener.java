package com.alttd.proxydiscordlink.bot;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.config.BotConfig;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;

public class JDAListener extends ListenerAdapter {

    private DiscordLink plugin;
    private final Bot bot;

    public JDAListener() {
        plugin = DiscordLink.getPlugin();
        bot = plugin.getBot();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor() == event.getJDA().getSelfUser()) {
            return;
        }
        if (event.isWebhookMessage()) {
            return;
        }
        if (event.getMessage().getChannel().getId().equals(BotConfig.COMMAND_CHANNEL)) {
            String content = event.getMessage().getContentRaw();
            if (content.startsWith("!") && content.length() > 1) {
                String[] split = content.split(" ");
                String cmd = split[0].substring(1).toLowerCase();
                String[] args = Arrays.copyOfRange(split, 1, split.length);
                for(DiscordCommand command : DiscordCommand.getCommands()) {
                    if(!command.getCommand().equalsIgnoreCase(cmd)) {
                        continue;
                    }
                    if(command.getPermission() != null) {
                        // TODO permission check? do we need this?
                    }
                    command.handleCommand(event.getMessage(), event.getAuthor().getName(), cmd, args);
                }
            }
        }
    }

}
