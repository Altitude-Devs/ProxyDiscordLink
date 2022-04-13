package com.alttd.proxydiscordlink.bot.commands;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.Bot;
import com.alttd.proxydiscordlink.bot.DiscordCommand;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.util.Utilities;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DiscordServerList extends DiscordCommand {

    private DiscordLink plugin;
    private final Bot bot;

    public DiscordServerList() {
        plugin = DiscordLink.getPlugin();
        bot = plugin.getBot();
    }

    @Override
    public String getCommand() {
        return "serverlist";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Lists all online players on the server or a specific server";
    }

    @Override
    public String getSyntax() {
        return "serverlist";
    }

    @Override
    public long getChannel() {
        return BotConfig.STAFF_COMMAND_CHANNEL;
    }

    @Override
    public void handleCommand(Message message, String sender, String command, String[] args) {
        String serverName = "Altitude";
        Collection<Player> onlinePlayer = plugin.getProxy().getAllPlayers();
        ServerInfo server;
        if (args.length != 0) {
            Optional<RegisteredServer> registeredServer = plugin.getProxy().getServer(args[0]);
            if (registeredServer.isEmpty()) {
                return;
            }
            onlinePlayer = registeredServer.get().getPlayersConnected();
            serverName = registeredServer.get().getServerInfo().getName();
        }
        LuckPerms luckPerms = Utilities.getLuckPerms();
        List<User> players = onlinePlayer
                .stream()
                .map(player -> luckPerms.getUserManager().getUser(player.getUniqueId()))
                .sorted((o1, o2) -> {
                    int i = Integer.compare(luckPerms.getGroupManager().getGroup(o2.getPrimaryGroup()).getWeight().orElse(0), luckPerms.getGroupManager().getGroup(o1.getPrimaryGroup()).getWeight().orElse(0));
                    return i != 0 ? i : o1.getUsername().compareToIgnoreCase(o2.getUsername());
                })
                .collect(Collectors.toList());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        String title = "Players online on " + serverName + ": " + players.size();
        embedBuilder.setTitle(title);
        String separator = "\n";
        String rankname = "";
        StringBuilder currentFieldText = new StringBuilder();
        int entryCounter = 0;
        int totalCharacters = title.length();
        int fieldCounter = 0;

        Iterator<User> iterator = players.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if(user != null) {
                if(!rankname.equalsIgnoreCase(user.getPrimaryGroup())) {
                    if (currentFieldText.length() != 0) {
                        totalCharacters += rankname.length() + currentFieldText.length();
                        fieldCounter++;
                        if (totalCharacters > 6000 || fieldCounter > 25) {
                            bot.sendEmbedToDiscord(BotConfig.COMMAND_CHANNEL, embedBuilder, 300);
                            embedBuilder.clearFields();
                            totalCharacters = title.length() + rankname.length() + currentFieldText.length();
                            fieldCounter = 1;
                        }
                        embedBuilder.addField(rankname, currentFieldText.toString(), true);
                        entryCounter = 0;
                        currentFieldText = new StringBuilder();
                    }
                    rankname = Utilities.capitalize(user.getPrimaryGroup());
                } else if(rankname.equalsIgnoreCase(user.getPrimaryGroup())) {
                    currentFieldText.append(separator);
                }
                if (entryCounter <= 50) {
                    Optional<Player> optionalPlayer = plugin.getProxy().getPlayer(user.getUniqueId());
                    if(optionalPlayer.isPresent()) {
                        Player player = optionalPlayer.get();
                        currentFieldText.append("`").append(player.getUsername()).append("`");
                    }
                } else if (entryCounter == 51){
                    currentFieldText.append("...");
                }
                entryCounter++;
            }
        }

        if (currentFieldText.length() > 0) {
            totalCharacters = title.length() + rankname.length() + currentFieldText.length();
            fieldCounter++;
            if (totalCharacters > 6000 || fieldCounter > 25) {
                bot.sendEmbedToDiscord(BotConfig.COMMAND_CHANNEL, embedBuilder, 300);
                embedBuilder.clearFields();
            }
            embedBuilder.addField(rankname, currentFieldText.toString(), true);
        }

        message.delete().queueAfter(300, TimeUnit.SECONDS);
        bot.sendEmbedToDiscord(BotConfig.COMMAND_CHANNEL, embedBuilder, 300);
    }
}
