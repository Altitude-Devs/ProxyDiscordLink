package com.alttd.proxydiscordlink.bot.commands;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.DiscordCommand;
import com.alttd.proxydiscordlink.bot.objects.DiscordRole;
import com.alttd.proxydiscordlink.database.Database;
import com.alttd.proxydiscordlink.objects.DiscordLinkPlayer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DiscordUnlink extends DiscordCommand {
    @Override
    public String getCommand() {
        return "unlink";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Unlink your Minecraft and Discord accounts.";
    }

    @Override
    public String getSyntax() {
        return "unlink";
    }

    @Override
    public void handleCommand(Message message, String sender, String command, String[] args) {
        Member member = message.getMember();

        Database database = DiscordLink.getPlugin().getDatabase();
        DiscordLinkPlayer discordLinkPlayer = DiscordLinkPlayer.getDiscordLinkPlayer(member.getIdLong());
        if (discordLinkPlayer == null) {
            message.getChannel().sendMessage("Your accounts aren't linked.").queue();
            return;
        }
        database.removeLinkedAccount(discordLinkPlayer);

        discordLinkPlayer.updateDiscord(
                DiscordRole.getDiscordRoles().stream()
                        .filter(role -> discordLinkPlayer.getRoles().contains(role.getInternalName()))
                        .collect(Collectors.toList()),
                false);
        discordLinkPlayer.updateMinecraft(
                DiscordRole.getDiscordRoles().stream()
                        .filter(role -> discordLinkPlayer.getRoles().contains(role.getInternalName()))
                        .collect(Collectors.toList()),
                false);
        discordLinkPlayer.linkedRole(false);
        message.getChannel().sendMessage("Your Discord and Minecraft accounts have been unlinked.").queue();
    }
}
