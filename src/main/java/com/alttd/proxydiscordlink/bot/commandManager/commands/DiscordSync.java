package com.alttd.proxydiscordlink.bot.commandManager.commands;

import com.alttd.proxydiscordlink.bot.commandManager.DiscordCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class DiscordSync extends DiscordCommand {
    @Override
    public String getName() {
        return "sync";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) { //TODO implement

    }

    @Override
    public CommandData getCommandData() { //TODO implement
        return null;
    }

    @Override
    public long getChannelId() { //TODO implement
        return 0;
    }
}
