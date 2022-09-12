package com.alttd.proxydiscordlink.minecraft.listeners;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.objects.DiscordLinkPlayer;
import com.alttd.proxydiscordlink.util.ALogger;
import com.alttd.proxydiscordlink.util.Utilities;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;

import java.util.UUID;

public class PlayerJoin {

    @Subscribe(order = PostOrder.LATE)
    public void playerConnected(ServerConnectedEvent event) {
        if (event.getPreviousServer().isPresent())
            return;
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        DiscordLinkPlayer discordLinkPlayer = DiscordLinkPlayer.getDiscordLinkPlayer(uuid);

        if (discordLinkPlayer == null)
            return;

        boolean sync = false;
        String username = player.getUsername();

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

        boolean hasMinecraftNitro = Utilities.hasMinecraftNitro(player);
        boolean hasDatabaseNitro = Utilities.hasDatabaseNitro(discordLinkPlayer);
        if (hasMinecraftNitro != hasDatabaseNitro) {
            if (hasMinecraftNitro) {
                //Utilities.removeRole(uuid, "nitro"); //TODO add nitro to config
                ALogger.info("Removing nitro role from [" + player.getUsername() + "] since they shouldn't have it");
            } else {
                //Utilities.addRole(uuid, "nitro"); //TODO add nitro to config
                ALogger.info("Added nitro role to [" + player.getUsername() + "] since they should have it");
            }
        }

        if (sync) //Sync if needed
            DiscordLink.getPlugin().getDatabase().syncPlayer(discordLinkPlayer);
    }
}
