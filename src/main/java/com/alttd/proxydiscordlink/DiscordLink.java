package com.alttd.proxydiscordlink;

import com.alttd.proxydiscordlink.commands.DiscordCommand;
import com.alttd.proxydiscordlink.config.Config;
import com.alttd.proxydiscordlink.database.Database;
import com.alttd.proxydiscordlink.database.DatabaseConnection;
import com.alttd.proxydiscordlink.listeners.PlayerJoin;
import com.alttd.proxydiscordlink.listeners.PlayerLeave;
import com.alttd.proxydiscordlink.util.ALogger;
import com.alttd.proxydiscordlink.util.Cache;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.logging.Logger;

@Plugin(id = "proxydiscordlink", name = "ProxyDiscordLink", version = "1.0.0",
        description = "A plugin that links Discord accounts with uuid's",
        authors = {"Teri"}
)
public class DiscordLink {

    private static DiscordLink plugin;
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Database database;
    private final Cache cache;

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
            getLogger().severe("*** Could not connect to the database. ***");
            getLogger().severe("*** This plugin will be disabled. ***");
            //TODO shutdown plugin
        }
        loadCommands();
        loadEvents();
    }

    public void reloadConfig() {
        Config.init();
        ALogger.info("Reloaded DiscordLink config.");
    }

    public void loadCommands() {// all (proxy)commands go here
        server.getCommandManager().register("discord", new DiscordCommand(), "discordlink");
    }

    public void loadEvents() {
        server.getEventManager().register(new PlayerJoin(), this);
        server.getEventManager().register(new PlayerLeave(), this);
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
}
