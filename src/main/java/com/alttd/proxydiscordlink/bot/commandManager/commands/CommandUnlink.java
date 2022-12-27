package com.alttd.proxydiscordlink.bot.commandManager.commands;

import com.alttd.proxydiscordlink.bot.commandManager.DiscordCommand;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.objects.DiscordLinkPlayer;
import com.alttd.proxydiscordlink.util.Utilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.Optional;

public class CommandUnlink extends DiscordCommand {
    private final CommandData commandData;
    public CommandUnlink(JDA jda) {
        commandData = Commands.slash(getName(), "Unlink your Discord and Altitude Minecraft accounts")
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);

        Utilities.registerCommand(jda, commandData);
    }
    @Override
    public String getName() {
        return "unlink";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) {
            Utilities.commandErrAutoRem("Unable to find you", event);
            return;
        }

        DiscordLinkPlayer discordLinkPlayer = DiscordLinkPlayer.getDiscordLinkPlayer(member.getIdLong());
        if (discordLinkPlayer == null) {
            Optional<Role> linkedRole = member.getRoles().stream().filter(role -> role.getIdLong() == BotConfig.DISCORD.LINKED_ROLE_ID).findAny();
            if (linkedRole.isPresent()) {
                member.getGuild().removeRoleFromMember(member, linkedRole.get()).queue();
                event.replyEmbeds(Utilities.genericSuccessEmbed("Success", "Your Discord and Minecraft accounts have been unlinked."))
                        .setEphemeral(true)
                        .queue();
                return;
            }
            Utilities.commandErrAutoRem("Your accounts aren't linked", event);
            return;
        }
        discordLinkPlayer.unlinkDiscordLinkPlayer();
        event.replyEmbeds(Utilities.genericSuccessEmbed("Success", "Your Discord and Minecraft accounts have been unlinked."))
                .setEphemeral(true)
                .queue();
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }

    @Override
    public long getChannelId() {
        return BotConfig.DISCORD.LINK_CHANNEL;
    }
}
