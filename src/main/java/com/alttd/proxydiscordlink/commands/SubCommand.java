package com.alttd.proxydiscordlink.commands;

import com.velocitypowered.api.command.CommandSource;

import java.util.List;

public interface SubCommand {

    String getName();

    String getPermission();

    void execute(String[] args, CommandSource source);

    List<String> suggest(String[] args);

    String getHelpMessage();

}
