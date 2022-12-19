package com.alttd.proxydiscordlink.bot.commandManager;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import java.util.ArrayList;

public abstract class SubCommand extends SubOption{

    private final SubCommandGroup parentGroup;
    private final boolean inSubGroup;

    protected SubCommand(SubCommandGroup parentGroup, DiscordCommand parent) {
        super(parent);
        this.parentGroup = parentGroup;
        this.inSubGroup = parentGroup != null;
    }

    public SubCommandGroup getParentGroup() {
        return parentGroup;
    }

    public boolean isInSubGroup() {
        return inSubGroup;
    }

    public void suggest(CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(new ArrayList<>()).queue();
    }
}
