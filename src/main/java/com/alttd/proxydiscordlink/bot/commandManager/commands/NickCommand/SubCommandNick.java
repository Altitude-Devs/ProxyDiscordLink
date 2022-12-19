package com.alttd.proxydiscordlink.bot.commandManager.commands.NickCommand;

import com.alttd.proxydiscordlink.bot.commandManager.DiscordCommand;
import com.alttd.proxydiscordlink.bot.commandManager.SubCommand;
import com.alttd.proxydiscordlink.bot.commandManager.SubCommandGroup;
import com.alttd.proxydiscordlink.util.Utilities;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SubCommandNick extends SubCommand {
    protected SubCommandNick(SubCommandGroup parentGroup, DiscordCommand parent) {
        super(parentGroup, parent);
    }

    @Override
    public String getName() {
        return "nickname";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!(getParent() instanceof CommandNick commandNick)) {
            Utilities.commandErrAutoRem("Couldn't find parent command", event);
            return;
        }

        String resultingName = commandNick.setNickname(event, true);
        event.replyEmbeds(Utilities.genericSuccessEmbed("Success",
                        "Your nickname has been set to `" + resultingName + "`."))
                .setEphemeral(true).queue();
    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
