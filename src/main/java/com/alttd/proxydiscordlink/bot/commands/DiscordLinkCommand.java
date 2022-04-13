package com.alttd.proxydiscordlink.bot.commands;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.DiscordCommand;
import com.alttd.proxydiscordlink.bot.objects.DiscordRole;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.config.Config;
import com.alttd.proxydiscordlink.objects.DiscordLinkPlayer;
import com.alttd.proxydiscordlink.util.Utilities;
import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.luckperms.api.model.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DiscordLinkCommand extends DiscordCommand {
    @Override
    public String getCommand() {
        return "link";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Link your Minecraft and Discord accounts.";
    }

    @Override
    public String getSyntax() {
        return "link <code>";
    }

    @Override
    public long getChannel() {
        return BotConfig.LINK_CHANNEL;
    }

    @Override
    public void handleCommand(Message message, String sender, String command, String[] args) {
        Member member = message.getMember();
        if (member == null)
            return;

        UUID uuid;
        if ((uuid = getUUID(message.getTextChannel(), args.length == 1 ? args[0] : "")) == null)
            return;

        List<DiscordRole> discordRoles = Utilities.getDiscordRolesForUser(uuid, member);

        DiscordLinkPlayer discordLinkPlayer = new DiscordLinkPlayer(
                member.getIdLong(),
                uuid,
                getUsername(uuid),
                member.getUser().getName(),
                false,
                true,
                discordRoles.stream()
                        .map(DiscordRole::getInternalName)
                        .collect(Collectors.toList()));

        linkAccount(discordLinkPlayer, message);
    }

    private void linkAccount(DiscordLinkPlayer discordLinkPlayer, Message message) {
        discordLinkPlayer.updateDiscord(
                DiscordRole.getDiscordRoles().stream()
                        .filter(role -> discordLinkPlayer.getRoles().contains(role.getInternalName()))
                        .collect(Collectors.toList()),
                true);
        discordLinkPlayer.updateMinecraft(
                DiscordRole.getDiscordRoles().stream()
                        .filter(role -> discordLinkPlayer.getRoles().contains(role.getInternalName()))
                        .collect(Collectors.toList()),
                true);

        discordLinkPlayer.linkedRole(true);
        Player player = DiscordLink.getPlugin().getProxy().getPlayer(discordLinkPlayer.getUuid()).orElse(null);
        User user = Utilities.getLuckPerms().getUserManager().getUser(discordLinkPlayer.getUuid());
        if (player != null || user != null)
            DiscordLink.getPlugin().getBot().changeNick(
                    message.getGuild().getIdLong(),
                    message.getMember().getIdLong(),
                    player == null ?
                            user.getUsername() :
                            player.getUsername());
        else
            DiscordLink.getPlugin().getBot().changeNick(
                    message.getGuild().getIdLong(),
                    message.getMember().getIdLong(),
                    discordLinkPlayer.getUsername());

        message.getChannel().sendMessage("You have successfully linked " +
                discordLinkPlayer.getUsername() + " with " +
                discordLinkPlayer.getDiscordUsername() + "!")
                .queue(message1 -> message1.delete().queueAfter(5, TimeUnit.SECONDS));

        DiscordLinkPlayer.addDiscordLinkPlayer(discordLinkPlayer);
        DiscordLink.getPlugin().getDatabase().syncPlayer(discordLinkPlayer);
        DiscordLink.getPlugin().getDatabase().syncRoles(discordLinkPlayer);
        DiscordLink.getPlugin().getCache().removeCachedPlayer(discordLinkPlayer.getUuid());
    }

    private String getUsername(UUID uuid) {
        Optional<Player> player = DiscordLink.getPlugin().getProxy().getPlayer(uuid);
        if (player.isPresent())
            return player.get().getUsername();

        User user = Utilities.getLuckPerms().getUserManager().getUser(uuid);
        if (user != null)
            return user.getUsername();

        return "No User";
    }

    private UUID getUUID(TextChannel channel, String code) {
        UUID uuid;
        if (code.matches("[0-9]{6}")) {
            if ((uuid = DiscordLink.getPlugin().getCache().getUUID(code)) != null)
                return uuid;
        }

        channel.sendMessage("Please use `&link ######` where the #'s are the code you received in-game.")
                .queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS, null, error -> {
                }));
        return null;
    }
}
