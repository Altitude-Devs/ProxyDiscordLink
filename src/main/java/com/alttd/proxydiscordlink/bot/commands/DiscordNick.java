package com.alttd.proxydiscordlink.bot.commands;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.DiscordCommand;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.objects.DiscordLinkPlayer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class DiscordNick extends DiscordCommand {
    @Override
    public String getCommand() {
        return "nick";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Change your name between your Minecraft username and Minecraft nickname.";
    }

    @Override
    public String getSyntax() {
        return "nick <username/nickname>";
    }

    @Override
    public void handleCommand(Message message, String sender, String command, String[] args) {
        MessageChannel channel = message.getChannel();
        if (args.length != 1) {
            channel.sendMessage("The correct syntax is: `" +
                    BotConfig.prefixMap.get(message.getGuild().getIdLong()) +
                    getSyntax() + "`.").queue();
            return;
        }

        Member member = message.getMember();
        DiscordLinkPlayer discordLinkPlayer = DiscordLinkPlayer.getDiscordLinkPlayer(member.getIdLong());
        if (discordLinkPlayer == null) {
            channel.sendMessage("You aren't linked, please link before using this command.").queue();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "username" -> {
                member.modifyNickname(discordLinkPlayer.getUsername()).queue();
                channel.sendMessage("Your nickname has been set to `" + discordLinkPlayer.getUsername() + "`.").queue();
                discordLinkPlayer.setNick(false);
                DiscordLink.getPlugin().getDatabase().syncPlayer(discordLinkPlayer);
            }
            case "nickname" -> {
                String nick = DiscordLink.getPlugin().getDatabase().getNick(discordLinkPlayer.getUuid());
                if (nick.isBlank())
                    nick = discordLinkPlayer.getUsername();
                member.modifyNickname(nick).queue();
                channel.sendMessage("Your nickname has been set to `" + nick + "`.").queue();
                discordLinkPlayer.setNick(true);
                DiscordLink.getPlugin().getDatabase().syncPlayer(discordLinkPlayer);
            }
            default -> channel.sendMessage("The correct syntax is: `" +
                    BotConfig.prefixMap.get(message.getGuild().getIdLong()) +
                    getSyntax() + "`.").queue();
        }
    }
}
