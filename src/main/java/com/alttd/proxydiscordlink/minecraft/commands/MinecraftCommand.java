package com.alttd.proxydiscordlink.minecraft.commands;

import com.alttd.proxydiscordlink.minecraft.commands.subcommands.CheckLinked;
import com.alttd.proxydiscordlink.minecraft.commands.subcommands.Link;
import com.alttd.proxydiscordlink.minecraft.commands.subcommands.Reload;
import com.alttd.proxydiscordlink.minecraft.commands.subcommands.Unlink;
import com.alttd.proxydiscordlink.config.Config;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MinecraftCommand implements SimpleCommand {

    private final List<SubCommand> subCommands;
    private final MiniMessage miniMessage;

    public MinecraftCommand() {
        subCommands = Arrays.asList(new CheckLinked(), new Link(), new Unlink(), new Reload());
        miniMessage = MiniMessage.get();
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        CommandSource source = invocation.source();

        if (args.length < 1) {
            if (!source.hasPermission("discordlink.link"))
                source.sendMessage(miniMessage.parse(Config.NO_PERMISSION));
            else if (source instanceof Player)
                source.sendMessage(miniMessage.parse(Config.DISCORD_LINK));
            else
                source.sendMessage(miniMessage.parse(Config.NO_CONSOLE));
            return;
        }

        subCommands.stream()
                .filter(subCommand -> subCommand.getName().equalsIgnoreCase(args[0]))
                .findFirst()
                .ifPresentOrElse(subCommand -> subCommand.execute(args, source)
                        , () -> source.sendMessage(getHelpMessage(source)));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> suggest = new ArrayList<>();

        if (args.length == 0) {
            subCommands.stream()
                    .filter(subCommand -> invocation.source().hasPermission(subCommand.getPermission()))
                    .forEach(subCommand -> suggest.add(subCommand.getName()));
        } else if (args.length <= 1) {
            subCommands.stream()
                    .filter(subCommand -> invocation.source().hasPermission(subCommand.getPermission()))
                    .filter(subCommand -> subCommand.getName().startsWith(args[0].toLowerCase()))
                    .forEach(subCommand -> suggest.add(subCommand.getName()));
        } else {
            subCommands.stream()
                    .filter(subCommand -> invocation.source().hasPermission(subCommand.getPermission()))
                    .filter(subCommand -> subCommand.getName().equalsIgnoreCase(args[0]))
                    .findFirst()
                    .ifPresent(subCommand -> suggest.addAll(subCommand.suggest(args)));
        }

        if (args.length == 0)
            return suggest;
        else
            return finalizeSuggest(suggest, args[args.length - 1]);
    }

    public List<String> finalizeSuggest(List<String> possibleValues, String remaining) {
        List<String> finalValues = new ArrayList<>();

        for (String str : possibleValues) {
            if (str.toLowerCase().startsWith(remaining)) {
                finalValues.add(StringArgumentType.escapeIfRequired(str));
            }
        }

        return finalValues;
    }

    private Component getHelpMessage(CommandSource source) {
        StringBuilder stringBuilder = new StringBuilder();

        subCommands.stream()
                .filter(subCommand -> source.hasPermission(subCommand.getPermission()))
                .forEach(subCommand -> stringBuilder.append(subCommand.getHelpMessage()).append("\n"));
        if (stringBuilder.length() != 0)
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "");

        return miniMessage.parse(Config.HELP_MESSAGE, Template.of("commands", stringBuilder.toString()));
    }
}
