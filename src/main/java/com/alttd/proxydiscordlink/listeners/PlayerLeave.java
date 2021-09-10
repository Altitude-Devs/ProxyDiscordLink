package com.alttd.proxydiscordlink.listeners;

import com.alttd.proxydiscordlink.database.Database;
import com.alttd.proxydiscordlink.DiscordLink;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;

public class PlayerLeave {

    @Subscribe(order = PostOrder.LATE)
    public void playerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();

        Database database = DiscordLink.getPlugin().getDatabase();
        if (database.isInCache(player)) //TODO async?
            database.removePlayerFromCache(player);

        DiscordLink.getPlugin().getCache().removeCachedPlayer(player.getUniqueId());
    }

}
