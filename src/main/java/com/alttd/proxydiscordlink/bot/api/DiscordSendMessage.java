package com.alttd.proxydiscordlink.bot.api;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class DiscordSendMessage {
    public static void sendMessage(String channelId, String message)
    {
        Bot bot = DiscordLink.getPlugin().getBot();

        bot.sendMessageToDiscord(channelId, message);
    }

    public static void sendEmbed(String channelId, String title, String description)
    {
        Bot bot = DiscordLink.getPlugin().getBot();
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setColor(Color.CYAN);
        embedBuilder.setTitle(title);
        embedBuilder.setDescription(description);

        bot.sendEmbedToDiscord(channelId, embedBuilder, -1);
    }
}
