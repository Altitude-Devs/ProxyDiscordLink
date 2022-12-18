package com.alttd.proxydiscordlink.bot.commandManager.commands;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.commandManager.DiscordCommand;
import com.alttd.proxydiscordlink.bot.objects.DiscordRole;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.objects.DiscordLinkPlayer;
import com.alttd.proxydiscordlink.util.Utilities;
import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.luckperms.api.model.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CommandLink extends DiscordCommand {

    public CommandLink(JDA jda) {
        CommandData commandData = Commands.slash(getName(), "Create an auction")
                .addOption(OptionType.NUMBER, "code", "The code you got from doing /discord link on Altitude in Minecraft", true)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);

        Utilities.registerCommand(jda, commandData);
    }

    @Override
    public String getName() {
        return "link";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) {
            handleError("Unable to find you", event);
            return;
        }

        UUID uuid = getUUID(event.getOption("link", OptionMapping::getAsInt));
        if (uuid == null) {
            handleError("This is not a valid link code, please check Minecraft and try again", event);
            return;
        }

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

        linkAccount(discordLinkPlayer, event);
    }

    private void linkAccount(DiscordLinkPlayer discordLinkPlayer, SlashCommandInteractionEvent event) {
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
        Guild guild = event.getGuild();
        Member member = event.getMember();
        if (guild == null || member == null) {
            handleError("Unable to find guild", event);
            return;
        }
        if (player != null || user != null)
            DiscordLink.getPlugin().getBot().changeNick(
                    guild.getIdLong(),
                    member.getIdLong(),
                    player == null ?
                            user.getUsername() :
                            player.getUsername());
        else
            DiscordLink.getPlugin().getBot().changeNick(
                    guild.getIdLong(),
                    member.getIdLong(),
                    discordLinkPlayer.getUsername());

        event.replyEmbeds(Utilities.genericSuccessEmbed("Success","You have successfully linked " +
                        discordLinkPlayer.getUsername() + " with " +
                        discordLinkPlayer.getDiscordUsername() + "!"))
                .setEphemeral(true)
                .queue(result -> result.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));

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

    private UUID getUUID(Integer code) {
        if (code == null)
            return null;
        return DiscordLink.getPlugin().getCache().getUUID(String.valueOf(code));
    }

    private void handleError(String text, SlashCommandInteractionEvent event) {
        event.replyEmbeds(Utilities.genericErrorEmbed("Error", text))
                .setEphemeral(true)
                .queue(res -> res.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public long getChannelId() {
        return BotConfig.DISCORD.LINK_CHANNEL;
    }
}
