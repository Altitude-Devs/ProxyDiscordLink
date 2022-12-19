package com.alttd.proxydiscordlink.bot.commandManager.commands;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.Bot;
import com.alttd.proxydiscordlink.bot.commandManager.DiscordCommand;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.util.Utilities;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class CommandServerList extends DiscordCommand {

    CommandData commandData;
    private final DiscordLink plugin;
    private final Bot bot;
    public CommandServerList(JDA jda) {
        plugin = DiscordLink.getPlugin();
        bot = plugin.getBot();
        commandData = Commands.slash(getName(), "Lists all online players on the server or a specific server")
                .addOption(OptionType.STRING, "server", "Server to check online players on", false)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
        Utilities.registerCommand(jda, commandData);
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public String getName() {
        return "serverlist";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String serverName = "Altitude";
        String tmp;
        Collection<Player> onlinePlayer = plugin.getProxy().getAllPlayers();
        ServerInfo server;
        if ((tmp = event.getOption("server", OptionMapping::getAsString)) != null) {
            Optional<RegisteredServer> registeredServer = plugin.getProxy().getServer(tmp);
            if (registeredServer.isEmpty()) {
                Utilities.commandErrAutoRem("This server does not exist " + tmp, event);
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
                .toList();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        String title = "Players online on " + serverName + ": " + players.size();
        embedBuilder.setTitle(title);
        String separator = "\n";
        String rankname = "";
        StringBuilder currentFieldText = new StringBuilder();
        int entryCounter = 0;
        int totalCharacters = title.length();
        int fieldCounter = 0;

        for (User user : players) {
            if (user != null) {
                if (!rankname.equalsIgnoreCase(user.getPrimaryGroup())) {
                    if (currentFieldText.length() != 0) {
                        totalCharacters += rankname.length() + currentFieldText.length();
                        fieldCounter++;
                        if (totalCharacters > 6000 || fieldCounter > 25) {
                            bot.sendEmbedToDiscord(getChannelId(), embedBuilder, 300);
                            embedBuilder.clearFields();
                            totalCharacters = title.length() + rankname.length() + currentFieldText.length();
                            fieldCounter = 1;
                        }
                        embedBuilder.addField(rankname, currentFieldText.toString(), true);
                        entryCounter = 0;
                        currentFieldText = new StringBuilder();
                    }
                    rankname = Utilities.capitalize(user.getPrimaryGroup());
                } else if (rankname.equalsIgnoreCase(user.getPrimaryGroup())) {
                    currentFieldText.append(separator);
                }
                if (entryCounter <= 50) {
                    Optional<Player> optionalPlayer = plugin.getProxy().getPlayer(user.getUniqueId());
                    if (optionalPlayer.isPresent()) {
                        Player player = optionalPlayer.get();
                        currentFieldText.append("`").append(player.getUsername()).append("`");
                    }
                } else if (entryCounter == 51) {
                    currentFieldText.append("...");
                }
                entryCounter++;
            }
        }

        if (currentFieldText.length() > 0) {
            totalCharacters = title.length() + rankname.length() + currentFieldText.length();
            fieldCounter++;
            if (totalCharacters > 6000 || fieldCounter > 25) {
                bot.sendEmbedToDiscord(getChannelId(), embedBuilder, 300);
                embedBuilder.clearFields();
            }
            embedBuilder.addField(rankname, currentFieldText.toString(), true);
        }

        bot.sendEmbedToDiscord(getChannelId(), embedBuilder, 300);
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }

    @Override
    public long getChannelId() {
        return BotConfig.DISCORD.STAFF_COMMAND_CHANNEL;
    }
}
