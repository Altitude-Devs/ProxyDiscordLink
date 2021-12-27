package com.alttd.proxydiscordlink.minecraft.listeners;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.objects.DiscordLinkPlayer;
import com.velocitypowered.api.proxy.Player;
import litebans.api.Entry;
import litebans.api.Events;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.Optional;
import java.util.UUID;

public class LiteBansBanListener {

    public void registerEvents() {
        Events.get().register(new Events.Listener() {
            @Override
            public void entryAdded(Entry entry) {
                if (entry.getType().equals("ban"))
                    onBan(entry);
            }

            @Override
            public void entryRemoved(Entry entry) {
                if (entry.getType().equals("ban"))
                    onUnBan(entry);
            }
        });

    }

    private void onBan(Entry entry) {
        if (!entry.isPermanent())
            return;

        String stringUuid = entry.getUuid();
        if (stringUuid == null)
            return;
        UUID uuid = UUID.fromString(stringUuid);

        DiscordLinkPlayer discordLinkPlayer = DiscordLinkPlayer.getDiscordLinkPlayer(uuid);
        discordLinkPlayer.setActive(false);

        DiscordLink.getPlugin().getBot().discordBan(BotConfig.GUILD_ID, discordLinkPlayer.getUserId(), "Auto ban due to Minecraft ban");
        Optional<Player> player = DiscordLink.getPlugin().getProxy().getPlayer(uuid);

        String username = stringUuid;
        if (player.isPresent())
            username = player.get().getUsername();

        DiscordLink.getPlugin().getBot().sendEmbedToDiscord(BotConfig.EVIDENCE_CHANNEL_ID,
                new EmbedBuilder()
                .setColor(Color.RED)
                .setAuthor(username, null, "https://crafatar.com/avatars/" + stringUuid + "?overlay")
                .setTitle("Auto Discord ban")
                .addField("Ban info",
                        "**Discord username**: `" + discordLinkPlayer.getDiscordUsername() + "`" +
                        "\n**Discord id**: `" + discordLinkPlayer.getUserId() + "`" +
                        "\n**Banned by**: `" + entry.getExecutorName() + "`" +
                        "\n**For**: ```" + (entry.getReason().length() < 800 ? entry.getReason() : entry.getReason().substring(0, 797) + "...") + "```",
                        false),
                -1);
    }

    private void onUnBan(Entry entry) {
        if (!entry.isPermanent())
            return;
        String uuid = entry.getUuid();
        if (uuid == null)
            return;
        DiscordLinkPlayer discordLinkPlayer = DiscordLinkPlayer.getDiscordLinkPlayer(UUID.fromString(uuid));
        DiscordLink.getPlugin().getBot().discordUnban(BotConfig.GUILD_ID, discordLinkPlayer.getUserId());
    }

}
