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

        boolean sync = false;

        String username = event.getPlayer().getUsername();
        DiscordLinkPlayer discordLinkPlayer = DiscordLinkPlayer.getDiscordLinkPlayer(event.getPlayer().getUniqueId());

        if (!discordLinkPlayer.getUsername().equals(username)) { //Update username if needed
            discordLinkPlayer.setUsername(username);
            sync = true;

            if (!discordLinkPlayer.hasNick())
                DiscordLink.getPlugin().getBot().changeNick(BotConfig.GUILD_ID, discordLinkPlayer.getUserId(), username);
        }

        if (discordLinkPlayer.hasNick()) { //If they have a nick update it, if nick is empty set it to false and use username
            String nick = DiscordLink.getPlugin().getDatabase().getNick(discordLinkPlayer.getUuid());
            if (!nick.isBlank()) {
                discordLinkPlayer.setNick(false);
                nick = discordLinkPlayer.getUsername();
            }
            DiscordLink.getPlugin().getBot().changeNick(BotConfig.GUILD_ID, discordLinkPlayer.getUserId(), nick);
        }

        if (sync) //Sync if needed
            DiscordLink.getPlugin().getDatabase().syncPlayer(discordLinkPlayer);
    }
}
