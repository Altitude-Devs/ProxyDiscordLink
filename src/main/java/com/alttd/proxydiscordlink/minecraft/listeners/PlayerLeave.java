package com.alttd.proxydiscordlink.minecraft.listeners;

import com.alttd.proxydiscordlink.DiscordLink;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;

public class PlayerLeave {

    @Subscribe(order = PostOrder.LATE)
    public void playerDisconnect(DisconnectEvent event) {
        DiscordLink.getPlugin().getCache().removeCachedPlayer(event.getPlayer().getUniqueId());
    }

}
