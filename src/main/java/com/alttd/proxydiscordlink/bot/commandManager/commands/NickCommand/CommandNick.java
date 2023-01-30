package com.alttd.proxydiscordlink.bot.commandManager.commands.NickCommand;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.commandManager.DiscordCommand;
import com.alttd.proxydiscordlink.bot.commandManager.SubOption;
import com.alttd.proxydiscordlink.objects.DiscordLinkPlayer;
import com.alttd.proxydiscordlink.util.ALogger;
import com.alttd.proxydiscordlink.util.Utilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.HashMap;

public class CommandNick extends DiscordCommand {
    CommandData commandData;
    private final HashMap<String, SubOption> subOptionsMap = new HashMap<>();
    public CommandNick(JDA jda) {
        commandData = Commands.slash(getName(), "Change your nickname to be your mc name/nickname")
                .addSubcommands(new SubcommandData("username", "Change your name to your Minecraft username"),
                        new SubcommandData("nickname", "Change your name to your Minecraft nickname")
                )
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
        Utilities.registerSubOptions(subOptionsMap,
                new SubCommandUserName(null,this),
                new SubCommandNick(null,this)
        );
        Utilities.registerCommand(jda, commandData);
    }

    @Override
    public String getName() {
        return "nick";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null || event.getMember() == null) {
            Utilities.commandErrAutoRem("This command can only be used within a guild: " + getName(), event);
            return;
        }

        String subcommandName = event.getInteraction().getSubcommandGroup();
        subcommandName = subcommandName == null ? event.getInteraction().getSubcommandName() : subcommandName;
        if (subcommandName == null) {
            ALogger.error("No subcommand found for " + getName());
            return;
        }

        SubOption subOption = subOptionsMap.get(subcommandName);
        if (subOption == null) {
            event.replyEmbeds(Utilities.invalidSubcommand(subcommandName))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        subOption.execute(event);
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }

    @Override
    public long getChannelId() {
        return 0;
    }

    public String setNickname(SlashCommandInteractionEvent event, boolean hasNick) {
        Member member = event.getMember();
        if (member == null) {
            Utilities.commandErrAutoRem("This command can only be run in a guild.", event);
            return null;
        }
        DiscordLinkPlayer discordLinkPlayer = DiscordLinkPlayer.getDiscordLinkPlayer(member.getIdLong());
        if (discordLinkPlayer == null) {
            Utilities.commandErrAutoRem("You aren't linked, please link before using this command.", event);
            return null;
        }

        String nick;
        if (hasNick) {
            nick = DiscordLink.getPlugin().getDatabase().getNick(discordLinkPlayer.getUuid());
            if (nick == null || nick.isBlank())
                nick = discordLinkPlayer.getUsername();
        } else {
            nick = discordLinkPlayer.getUsername();
        }
        member.modifyNickname(nick).queue();
        discordLinkPlayer.setNick(hasNick);
        DiscordLink.getPlugin().getDatabase().syncPlayer(discordLinkPlayer);
        return nick;
    }
}
