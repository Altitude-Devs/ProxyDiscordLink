package com.alttd.proxydiscordlink.bot.commandManager.commands;

import com.alttd.proxydiscordlink.bot.commandManager.DiscordCommand;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.util.Utilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandBroadcast extends DiscordCommand {

    CommandData commandData;
    public CommandBroadcast(JDA jda) {
        commandData = Commands.slash(getName(), "Broadcast a message to all online players")
                .addOption(OptionType.STRING, "text", "Text to broadcast", true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
        Utilities.registerCommand(jda, commandData);
    }
    @Override
    public String getName() {
        return "broadcast";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String msg = event.getOption("text", OptionMapping::getAsString);
        if (msg == null) {
            Utilities.commandErrAutoRem("Couldn't find text", event);
            return;
        }
        event.replyEmbeds(Utilities.genericSuccessEmbed("Success", "Broadcast the following message:\n" + msg))
                .setEphemeral(true).queue();
        Utilities.broadcast(msg);
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public long getChannelId() {
        return BotConfig.DISCORD.STAFF_COMMAND_CHANNEL;
    }
}
