package com.alttd.proxydiscordlink.bot.commandManager;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;

public abstract class DiscordCommand {

    public abstract String getName();

    public abstract void execute(SlashCommandInteractionEvent event);

    public void suggest(CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(new ArrayList<>()).queue();
    }

    public abstract CommandData getCommandData();

    public abstract long getChannelId();
}
