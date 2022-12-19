package com.alttd.proxydiscordlink.util;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.commandManager.SubOption;
import com.alttd.proxydiscordlink.bot.objects.DiscordRole;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.config.Config;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.RestAction;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Utilities {

    private static LuckPerms luckPerms;
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static LuckPerms getLuckPerms() {
        if (luckPerms == null)
            luckPerms = LuckPermsProvider.get();
        return luckPerms;
    }

    public static boolean isDonor(UUID uuid) {
        User user = getLuckPerms().getUserManager().getUser(uuid);
        if (user == null) {
            ALogger.error("Unable to find user in isDonor!");
            return false;
        }

        Set<String> groups = user
                .getNodes().stream()
                .filter(NodeType.INHERITANCE::matches)
                .map(NodeType.INHERITANCE::cast)
                .map(InheritanceNode::getGroupName)
                .collect(Collectors.toSet());

        for (String group : Config.DONOR_GROUPS) {
            if (groups.contains(group)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasMinecraftNitro(Player player) {
        User user = getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            ALogger.error("Unable to find user in isNitro!");
            return false;
        }

        Set<String> groups = user
                .getNodes().stream()
                .filter(NodeType.INHERITANCE::matches)
                .map(NodeType.INHERITANCE::cast)
                .map(InheritanceNode::getGroupName)
                .collect(Collectors.toSet());

        for (String group : Config.DISCORD_GROUPS) {
            if (groups.contains(group)) {
                return true;
            }
        }
        return false;
    }

    public static String getAuthKey() {
        String randChars = "1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();

        while (salt.length() < 6) {
            int index = (int) (rnd.nextFloat() * (float) randChars.length());
            salt.append(randChars.charAt(index));
        }

        return salt.toString();
    }

    public static String getRankName(UUID uuid) {
        return getLuckPerms().getUserManager().getUser(uuid).getPrimaryGroup();
    }

    public static String capitalize(String str) {
        if(str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static void broadcast(String message) {
        ProxyServer server = DiscordLink.getPlugin().getProxy();
        server.sendMessage(miniMessage.deserialize(message));
    }

    public static List<DiscordRole> getDiscordRolesForUser(UUID uuid, Member member) {
        User user = Utilities.getLuckPerms().getUserManager().getUser(uuid);
        List<InheritanceNode> groups;
        List<Role> roles = member.getRoles();

        if (user == null) {
            ALogger.error("Got null user from LuckPerms when processing " + uuid + " during linking.");
            groups = new ArrayList<>();
        } else {
            groups = user.getNodes().stream()
                    .filter(node -> node instanceof InheritanceNode)
                    .map(node -> (InheritanceNode) node)
                    .collect(Collectors.toList());
        }

        return DiscordRole.getDiscordRoles().stream()
                .filter(discordRole -> {
                    for (Role role : roles) {
                        if (role.getIdLong() == discordRole.getId())
                            return true;
                    }
                    for (InheritanceNode group : groups) {
                        if (group.getGroupName().equals(discordRole.getLuckpermsName()))
                            return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    public static void registerCommand(JDA jda, CommandData commandData) {
        Guild guild = jda.getGuildById(BotConfig.DISCORD.GUILD_ID);
        if (guild == null) {
            ALogger.error("Unable to find guild id to register commands");
            return;
        }
        registerCommand(guild, commandData);
    }

    public static void registerCommand(Guild guild, CommandData commandData) {
        guild.upsertCommand(commandData).queue(RestAction.getDefaultSuccess(), Utilities::handleFailure);
    }

    public static void registerSubOptions(HashMap<String, SubOption> subCommandMap, SubOption... subOptions) {
        for (SubOption subOption : subOptions)
            subCommandMap.put(subOption.getName(), subOption);
    }

    public static MessageEmbed invalidSubcommand(String subcommandName) {
        return new EmbedBuilder()
                .setTitle("Invalid sub command")
                .setDescription("This is not a valid sub command: " + subcommandName)
                .setColor(Color.RED)
                .build();
    }

    public static MessageEmbed genericErrorEmbed(String title, String desc) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(desc)
                .setColor(Color.RED)
                .build();
    }

    public static MessageEmbed genericSuccessEmbed(String title, String desc) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(desc)
                .setColor(Color.GREEN)
                .build();
    }

    public static MessageEmbed genericWaitingEmbed(String title, String desc) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(desc)
                .setColor(Color.BLUE)
                .build();
    }

    public static void ignoreSuccess(Object o) {
        // IDK I thought this looked nicer in the .queue call
    }

    public static void handleFailure(Throwable failure) {
        ALogger.error(failure.getMessage());
    }

    public static void commandErrAutoRem(String text, SlashCommandInteractionEvent event) {
        event.replyEmbeds(Utilities.genericErrorEmbed("Error", text))
                .setEphemeral(true)
                .queue(res -> res.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
    }
}
