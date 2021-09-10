package com.alttd.proxydiscordlink.config;

import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.yaml.snakeyaml.DumperOptions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class Config {
    private static final Pattern PATH_PATTERN = Pattern.compile("\\.");
    private static final String HEADER = "";

    private static File CONFIG_FILE;
    public static ConfigurationNode config;
    public static YAMLConfigurationLoader configLoader;

    static int version;
    static boolean verbose;

    public static File CONFIGPATH;

    public static void init() { // todo setup share for the config
        CONFIGPATH = new File(System.getProperty("user.home") + File.separator + "share" + File.separator + "configs" + File.separator + "DiscordLink");
        CONFIG_FILE = new File(CONFIGPATH, "config.yml");

        configLoader = YAMLConfigurationLoader.builder()
                .setFile(CONFIG_FILE)
                .setFlowStyle(DumperOptions.FlowStyle.BLOCK)
                .build();
        if (!CONFIG_FILE.getParentFile().exists()) {
            if (!CONFIG_FILE.getParentFile().mkdirs()) {
                return;
            }
        }
        if (!CONFIG_FILE.exists()) {
            try {
                if (!CONFIG_FILE.createNewFile()) {
                    return;
                }
            } catch (IOException error) {
                error.printStackTrace();
            }
        }

        try {
            config = configLoader.load(ConfigurationOptions.defaults().setHeader(HEADER));
        } catch (IOException e) {
            e.printStackTrace();
        }

        verbose = getBoolean("verbose", true);
        version = getInt("config-version", 1);

        readConfig(Config.class, null);
        try {
            configLoader.save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readConfig(Class<?> clazz, Object instance) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPrivate(method.getModifiers())) {
                if (method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE) {
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                    } catch (InvocationTargetException | IllegalAccessException ex) {
                        throw Throwables.propagate(ex.getCause());
                    }
                }
            }
        }
        try {
            configLoader.save(config);
        } catch (IOException ex) {
            throw Throwables.propagate(ex.getCause());
        }
    }

    public static void saveConfig() {
        try {
            configLoader.save(config);
        } catch (IOException ex) {
            throw Throwables.propagate(ex.getCause());
        }
    }

    private static Object[] splitPath(String key) {
        return PATH_PATTERN.split(key);
    }

    private static void set(String path, Object def) {
        if (config.getNode(splitPath(path)).isVirtual())
            config.getNode(splitPath(path)).setValue(def);
    }

    private static void setString(String path, String def) {
        try {
            if (config.getNode(splitPath(path)).isVirtual())
                config.getNode(splitPath(path)).setValue(TypeToken.of(String.class), def);
        } catch (ObjectMappingException ex) {
        }
    }

    private static boolean getBoolean(String path, boolean def) {
        set(path, def);
        return config.getNode(splitPath(path)).getBoolean(def);
    }

    private static double getDouble(String path, double def) {
        set(path, def);
        return config.getNode(splitPath(path)).getDouble(def);
    }

    private static int getInt(String path, int def) {
        set(path, def);
        return config.getNode(splitPath(path)).getInt(def);
    }

    private static String getString(String path, String def) {
        setString(path, def);
        return config.getNode(splitPath(path)).getString(def);
    }

    private static Long getLong(String path, Long def) {
        set(path, def);
        return config.getNode(splitPath(path)).getLong(def);
    }

    private static <T> List<String> getList(String path, T def) {
        try {
            set(path, def);
            return config.getNode(splitPath(path)).getList(TypeToken.of(String.class));
        } catch (ObjectMappingException ex) {
        }
        return new ArrayList<>();
    }

    private static ConfigurationNode getNode(String path) {
        if (config.getNode(splitPath(path)).isVirtual()) {
            //new RegexConfig("Dummy");
        }
        config.getChildrenMap();
        return config.getNode(splitPath(path));
    }


    /**
     * ONLY EDIT ANYTHING BELOW THIS LINE
     **/
    public static String DRIVERS = "mysql";
    public static String IP = "localhost";
    public static String PORT = "3306";
    public static String DATABASE_NAME = "discordlink";
    public static String USERNAME = "root";
    public static String PASSWORD = "root";

    private static void database() {
        DRIVERS = getString("database.drivers", DRIVERS);
        IP = getString("database.ip", IP);
        PORT = getString("database.port", PORT);
        DATABASE_NAME = getString("database.database_name", DATABASE_NAME);
        USERNAME = getString("database.username", USERNAME);
        PASSWORD = getString("database.password", PASSWORD);
    }

    public static List<String> DONOR_GROUPS = new ArrayList<>(List.of("donor"));
    public static List<String> DISCORD_GROUPS = new ArrayList<>(List.of("nitro"));

    private static void loadGroups() {
        DONOR_GROUPS = getList("settings.donor-groups", DONOR_GROUPS);
        DISCORD_GROUPS = getList("settings.discord-groups", DISCORD_GROUPS);
    }

    public static List<String> DISCORD_MESSAGE = new ArrayList<>(List.of("Invite code here."));
    public static String DISCORD_LINK = "<click:run:command:discord link:><yellow>Your Minecraft and Discord accounts aren't linked yet, to link them click this message!</yellow></click>";
    public static String GIVE_CODE = "<yellow>Your code is <gold><code></gold>, To link your accounts do <gold>&link <code></gold> in the Discord #link channel.</yellow>";
    public static String ALREADY_LINKED_ACCOUNTS = "<yellow>Your accounts are already linked. You can unlink your accounts by doing <gold>/discord unlink</gold>.</yellow>";
    public static String ALREADY_GOT_CODE = "<yellow>You have already got your code. Your code is <gold><code><gold></yellow>";
    public static String ACCOUNTS_NOT_LINKED = "<yellow>Your Minecraft and Discord accounts aren't linked</yellow>";
    public static String UNLINKED_ACCOUNTS = "<yellow>You have successfully unlinked your accounts.</yellow>";
    public static String IS_LINKED = "<yellow><player> is <linked_status>.</yellow>";
    public static String INVALID_PLAYER = "<red><player> is not online or is not a valid player.</red>";
    public static String NO_PERMISSION = "<red>You do not have permission to do that.</red>";
    public static String CONSOLE = "<red>This command can not be executed from console.</red>";
    public static String HELP_MESSAGE = "<yellow>DiscordLink commands:\n<commands></yellow>";
    public static String HELP_LINK = "<yellow><gold>/discord link</gold>: Get a code which can be used to link your Minecraft and Discord accounts.</yellow>";
    public static String HELP_UNLINK = "<yellow><gold>/discord unlink</gold>: Unlink your Minecraft and Discord accounts.</yellow>";
    public static String HELP_CHECK_LINKED = "<yellow><gold>/discord checklinked <user></gold>: Check if the specified user has their Minecraft and Discord accounts linked.</yellow>";

    private static void loadMessages() {
        DISCORD_MESSAGE = getList("messages.discord-message", DISCORD_MESSAGE);
        GIVE_CODE = getString("messages.give-code", GIVE_CODE);
        ALREADY_LINKED_ACCOUNTS = getString("messages.already-linked-accounts", ALREADY_LINKED_ACCOUNTS);
        ALREADY_GOT_CODE = getString("messages.already-got-code", ALREADY_GOT_CODE);
        ACCOUNTS_NOT_LINKED = getString("messages.accounts-not-linked", ACCOUNTS_NOT_LINKED);
        UNLINKED_ACCOUNTS = getString("messages.unlinked-accounts", UNLINKED_ACCOUNTS);
        IS_LINKED = getString("messages.is-linked", IS_LINKED);
        INVALID_PLAYER = getString("messages.invalid-player", INVALID_PLAYER);
        NO_PERMISSION = getString("messages.no-permission", NO_PERMISSION);
        CONSOLE = getString("messages.console", CONSOLE);
        HELP_MESSAGE = getString("message.help-message", HELP_MESSAGE);
        HELP_LINK = getString("message.help-link", HELP_LINK);
        HELP_UNLINK = getString("message.help-unlink", HELP_UNLINK);
        HELP_CHECK_LINKED = getString("message.help-check-linked", HELP_CHECK_LINKED);
    }
}
