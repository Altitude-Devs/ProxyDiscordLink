package com.alttd.proxydiscordlink.minecraft.listeners;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.config.Config;
import com.alttd.proxydiscordlink.util.Utilities;
import com.alttd.shutdowninfo.events.WhitelistKickEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.Template;

public class WhitelistKick {

    @Subscribe
    public void onWhitelistKick(WhitelistKickEvent event) {
        Player player = event.getPlayer();
        if (DiscordLink.getPlugin().getDatabase().playerIsLinked(player))
            return;

        String authCode = Utilities.getAuthKey();
        DiscordLink.getPlugin().getCache().removeCachedPlayer(player.getUniqueId());
        DiscordLink.getPlugin().getCache()
                .cacheCode(player.getUniqueId(), authCode);

        event.appendTemplates(Template.of("code", authCode));
        event.appendMessage("\n\n" + Config.WHITELIST_LINK_MESSAGE);
    }
}