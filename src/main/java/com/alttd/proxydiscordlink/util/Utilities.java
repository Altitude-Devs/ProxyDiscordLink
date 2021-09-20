package com.alttd.proxydiscordlink.util;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.objects.DiscordRole;
import com.alttd.proxydiscordlink.config.Config;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;

import java.util.*;
import java.util.stream.Collectors;

public class Utilities {

    private static LuckPerms luckPerms;
    private static MiniMessage miniMessage = MiniMessage.get();

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
        server.sendMessage(miniMessage.parse(message));
    }

    public static List<DiscordRole> getDiscordRolesForUser(UUID uuid, Member member) {
        User user = Utilities.getLuckPerms().getUserManager().getUser(uuid);
        if (user == null) {
            ALogger.error("Got null user from LuckPerms when processing " + uuid + " during linking.");
            return null;
        }

        List<InheritanceNode> groups = user.getNodes().stream()
                .filter(node -> node instanceof InheritanceNode)
                .map(node -> (InheritanceNode) node)
                .collect(Collectors.toList());
        List<Role> roles = member.getRoles();

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

    public static List<DiscordRole> getMinecraftRolesForUser(UUID uuid) {
        User user = getLuckPerms().getUserManager().getUser(uuid);
        List<DiscordRole> roles = new ArrayList<>();
        if (user == null)
            return roles;

        user.getNodes().stream()
                .filter(node -> node instanceof InheritanceNode)
                .map(node -> ((InheritanceNode) node).getGroupName())
                .collect(Collectors.toList())
                .forEach(lpName -> DiscordRole.getDiscordRoles().stream()
                        .filter(discordRole -> discordRole.getLuckpermsName().equals(lpName))
                        .findFirst()
                        .ifPresent(roles::add));
        return roles;
    }
}
