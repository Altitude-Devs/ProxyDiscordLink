package com.alttd.proxydiscordlink.bot.commandManager;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class DiscordCommand {

    public abstract String getName();

    public abstract void execute(SlashCommandInteractionEvent event);

    public abstract void suggest(CommandAutoCompleteInteractionEvent event);

    public abstract CommandData getCommandData();

    public abstract long getChannelId();
}
