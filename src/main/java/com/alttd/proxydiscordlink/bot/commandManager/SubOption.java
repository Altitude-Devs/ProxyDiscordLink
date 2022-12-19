package com.alttd.proxydiscordlink.bot.commandManager;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class SubOption {
    private final DiscordCommand parent;

    protected SubOption(DiscordCommand parent) {
        this.parent = parent;
    }

    public DiscordCommand getParent() {
        return parent;
    }

    public abstract String getName();

    public abstract void execute(SlashCommandInteractionEvent event);

    public abstract String getHelpMessage();
}
