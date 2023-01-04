package com.alttd.proxydiscordlink.bot.commandManager;

import com.alttd.proxydiscordlink.bot.commandManager.commands.*;
import com.alttd.proxydiscordlink.bot.commandManager.commands.NickCommand.CommandNick;
import com.alttd.proxydiscordlink.util.ALogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class CommandManager extends ListenerAdapter {

    private final List<DiscordCommand> commands;

    public CommandManager(JDA jda) {
        ALogger.info("Loading commands...");
        commands = List.of(
                new CommandLink(jda),
                new CommandUnlink(jda),
                new CommandNick(jda),
                new CommandServerList(jda),
                new CommandStaffList(jda),
                new CommandBroadcast(jda)
        );
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        Optional<DiscordCommand> first = commands.stream()
                .filter(discordCommand -> discordCommand.getName().equalsIgnoreCase(commandName))
                .findFirst();
        if (first.isEmpty()) {
            return;
        }
        first.get().execute(event);
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        Optional<DiscordCommand> first = commands.stream()
                .filter(discordCommand -> discordCommand.getName().equalsIgnoreCase(event.getName()))
                .findFirst();
        if (first.isEmpty())
            return;
        first.get().suggest(event);
    }

    public List<DiscordCommand> getCommands() {
        return commands;
    }
}
