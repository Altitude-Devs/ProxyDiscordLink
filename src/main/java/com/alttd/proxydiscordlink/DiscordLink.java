package com.alttd.proxydiscordlink;

import com.alttd.proxydiscordlink.bot.Bot;
import com.alttd.proxydiscordlink.minecraft.commands.MinecraftCommand;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.config.Config;
import com.alttd.proxydiscordlink.database.Database;
import com.alttd.proxydiscordlink.database.DatabaseConnection;
import com.alttd.proxydiscordlink.minecraft.listeners.LiteBansBanListener;
import com.alttd.proxydiscordlink.minecraft.listeners.LuckpermsEvents;
import com.alttd.proxydiscordlink.minecraft.listeners.PlayerLeave;
import com.alttd.proxydiscordlink.minecraft.listeners.WhitelistKick;
import com.alttd.proxydiscordlink.util.ALogger;
import com.alttd.proxydiscordlink.util.Cache;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;

@Plugin(id = "proxydiscordlink", name = "ProxyDiscordLink", version = "1.0.0",
        description = "A plugin that links Discord accounts with uuid's",
        authors = {"Teri"},
        dependencies = {@Dependency(id = "luckperms"),@Dependency(id = "shutdowninfo"),@Dependency(id = "litebans")}
)
public class DiscordLink {

    private static DiscordLink plugin;
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Database database;
    private final Cache cache;
    private Bot bot;

    @Inject
    public DiscordLink(ProxyServer proxyServer, Logger proxyLogger, @DataDirectory Path proxydataDirectory)
    {
        plugin = this;
        server = proxyServer;
        logger = proxyLogger;
        dataDirectory = proxydataDirectory;
        database = new Database();
        cache = new Cache();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        ALogger.init(logger);
        reloadConfig();
        try {
            DatabaseConnection.initialize();
        } catch (SQLException exception) {
            exception.printStackTrace();
            ALogger.error("*** Could not connect to the database. ***");
            ALogger.error("*** This plugin will be disabled. ***");
            //TODO shutdown plugin
        }
        loadCommands();
        loadEvents();
        loadBot();
        new LiteBansBanListener().registerEvents();
    }

    public void reloadConfig() {
        Config.init();
        BotConfig.init();
        ALogger.info("Reloaded DiscordLink config.");
    }

    public void loadCommands() {// all (proxy)commands go here
        server.getCommandManager().register("discord", new MinecraftCommand());
    }

    public void loadEvents() {
        server.getEventManager().register(this, new PlayerLeave());
        server.getEventManager().register(this, new WhitelistKick());
        new LuckpermsEvents().listener();
    }

    public void loadBot() {
        bot = new Bot();
        bot.connect();
    }


    public File getDataDirectory() {
        return dataDirectory.toFile();
    }

    public static DiscordLink getPlugin() {
        return plugin;
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getProxy() {
        return server;
    }

    public Database getDatabase() {
        return database;
    }

    public Cache getCache() {
        return cache;
    }

    public Bot getBot() {
        return bot;
    }
}
