package com.alttd.proxydiscordlink.util;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.config.Config;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;

import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class Utilities {

    private static LuckPerms luckPerms;
    private static MiniMessage miniMessage = MiniMessage.get();

    public static LuckPerms getLuckPerms() {
        if (luckPerms == null)
            luckPerms = LuckPermsProvider.get();
        return luckPerms;
    }

    public static boolean isDonor(Player player) {
        User user = getLuckPerms().getUserManager().getUser(player.getUniqueId());
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

    public static String getRankName(Player player) {
        return getLuckPerms().getUserManager().getUser(player.getUniqueId()).getPrimaryGroup();
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
}
