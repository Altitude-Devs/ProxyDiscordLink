package com.alttd.proxydiscordlink.minecraft.listeners;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.objects.DiscordLinkPlayer;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;

public class PlayerJoin {

    @Subscribe(order = PostOrder.LATE)
    public void playerConnected(ServerConnectedEvent event) {
        if (event.getPreviousServer().isEmpty())
            return;

        String username = event.getPlayer().getUsername();
        DiscordLinkPlayer discordLinkPlayer = DiscordLinkPlayer.getDiscordLinkPlayer(event.getPlayer().getUniqueId());

        if (!discordLinkPlayer.getUsername().equals(username)) {
            discordLinkPlayer.setUsername(username);
            DiscordLink.getPlugin().getDatabase().syncPlayer(discordLinkPlayer);

            if (!discordLinkPlayer.hasNick())
                DiscordLink.getPlugin().getBot().changeNick(BotConfig.GUILD_ID, discordLinkPlayer.getUserId(), username);
        }
    }
}
