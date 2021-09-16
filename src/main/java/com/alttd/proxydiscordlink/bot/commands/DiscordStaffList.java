package com.alttd.proxydiscordlink.bot.commands;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.Bot;
import com.alttd.proxydiscordlink.bot.DiscordCommand;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.util.Utilities;
import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;

import java.util.*;
import java.util.stream.Collectors;

public class DiscordStaffList extends DiscordCommand {

    private DiscordLink plugin;
    private final Bot bot;

    public DiscordStaffList() {
        plugin = DiscordLink.getPlugin();
        bot = plugin.getBot();
    }

    @Override
    public String getCommand() {
        return "stafflist";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Lists all online staff on the server";
    }

    @Override
    public String getSyntax() {
        return "staffList";
    }

    @Override
    public void handleCommand(Message message, String sender, String command, String[] args) {
        LuckPerms luckPerms = Utilities.getLuckPerms();
        List<User> staff = plugin.getProxy().getAllPlayers()
                .stream().filter(player-> player.hasPermission("group." + BotConfig.SL_MINIMUMRANK))
                .map(player -> luckPerms.getUserManager().getUser(player.getUniqueId()))
                .sorted((o1, o2) -> {
                    int i = Integer.compare(luckPerms.getGroupManager().getGroup(o2.getPrimaryGroup()).getWeight().orElse(0), luckPerms.getGroupManager().getGroup(o1.getPrimaryGroup()).getWeight().orElse(0));
                    return i != 0 ? i : o1.getUsername().compareToIgnoreCase(o2.getUsername());
                })
                .collect(Collectors.toList());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        String title = "Online Staff: " + staff.size() + " - Online Players: " + plugin.getProxy().getAllPlayers().size();
        embedBuilder.setTitle(title);
        String separator = "\n";
        String rankname = "";

        Map<String, Integer> onlineStaff = new HashMap<>();
        StringBuilder currentFieldText = new StringBuilder();
        int entryCounter = 0;
        int totalCharacters = title.length();
        int fieldCounter = 0;

        Iterator<User> iterator = staff.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if(user != null) {
                if(!rankname.equalsIgnoreCase(user.getPrimaryGroup())) {
                    if (currentFieldText.length() != 0) {
                        totalCharacters += rankname.length() + currentFieldText.length();
                        fieldCounter++;
                        if (totalCharacters > 6000 || fieldCounter > 25) {
                            bot.sendEmbedToDiscord(BotConfig.COMMAND_CHANNEL, embedBuilder, -1);
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

                Optional<Player> optionalPlayer = plugin.getProxy().getPlayer(user.getUniqueId());
                if(optionalPlayer.isPresent()) {
                    Player player = optionalPlayer.get();
                    String currentServerName = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "";
                    if (onlineStaff.containsKey(currentServerName)){
                        onlineStaff.put(currentServerName, onlineStaff.get(currentServerName) + 1);
                    } else {
                        onlineStaff.put(currentServerName, 1);
                    }

                    if (entryCounter <= 50) {
                        currentFieldText.append("`").append(player.getUsername()).append("`");
                    } else if (entryCounter == 51){
                        currentFieldText.append("...");
                    }
                    entryCounter++;
                }
            }
        }

        if (currentFieldText.length() > 0) {
            totalCharacters = title.length() + rankname.length() + currentFieldText.length();
            fieldCounter++;
            if (totalCharacters > 6000 || fieldCounter > 25) {
                bot.sendEmbedToDiscord(BotConfig.COMMAND_CHANNEL, embedBuilder, -1);
                embedBuilder.clearFields();
            }
            embedBuilder.addField(rankname, currentFieldText.toString(), true);
            currentFieldText = new StringBuilder();
        }

        for (Map.Entry<String, Integer> entry : onlineStaff.entrySet()){
            String serverName = entry.getKey();
            Integer amountOfStaff = entry.getValue();
            // this might error:/
            int playerCount = plugin.getProxy().getServer(serverName).isPresent() ? plugin.getProxy().getServer(serverName).get().getPlayersConnected().size() - amountOfStaff : 1;
            currentFieldText.append(serverName).append(" online staff per player ")
                    .append(amountOfStaff).append(" / ").append(Math.max(playerCount, 0)).append(" = ")
                    .append(playerCount > 0 ? Math.round(((double)amountOfStaff / playerCount) * 100.0) / 100.0 : "-").append("\n");
        }

        if (currentFieldText.length() > 0) {
            rankname = "Staff per server";
            totalCharacters = title.length() + rankname.length() + currentFieldText.length();
            fieldCounter++;
            if (totalCharacters > 6000 || fieldCounter > 25) {
                bot.sendEmbedToDiscord(BotConfig.COMMAND_CHANNEL, embedBuilder, -1);
                embedBuilder.clearFields();
            }
            embedBuilder.addField(rankname, currentFieldText.toString(), true);
        }

        bot.sendEmbedToDiscord(BotConfig.COMMAND_CHANNEL, embedBuilder, -1);
    }
}
