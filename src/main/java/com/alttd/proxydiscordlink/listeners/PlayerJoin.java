package com.alttd.proxydiscordlink.listeners;

import com.alttd.proxydiscordlink.database.Database;
import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.util.ALogger;
import com.alttd.proxydiscordlink.util.Utilities;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;

public class PlayerJoin {

    @Subscribe(order = PostOrder.LATE)
    public void onPlayerJoin(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        Database database = DiscordLink.getPlugin().getDatabase();

        if (database.playerIsLinked(player)) {
            database.syncPlayerData(player);

            boolean isNitro = database.hasDiscordNitro(player);
            boolean hasNitro = Utilities.hasMinecraftNitro(player);

            LuckPerms luckPermsAPI = Utilities.getLuckPerms();
            Group discord = luckPermsAPI.getGroupManager().getGroup("discord");

            if (discord == null) {
                ALogger.error("Unable to find discord group in DiscordLink");
                return;
            }

            if (isNitro && !hasNitro) {
                luckPermsAPI.getUserManager().modifyUser(player.getUniqueId(), (User user) -> {
                    Node node = InheritanceNode.builder(discord).build();
                    user.data().add(node);
                });

            } else if (!isNitro && hasNitro) {
                luckPermsAPI.getUserManager().modifyUser(player.getUniqueId(), (User user) -> {
                    Node node = InheritanceNode.builder(discord).build();
                    user.data().remove(node);
                });
            }
        }
    }
}
