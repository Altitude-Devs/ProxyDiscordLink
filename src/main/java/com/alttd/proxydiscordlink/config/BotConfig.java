package com.alttd.proxydiscordlink.config;

import com.alttd.proxydiscordlink.bot.objects.DiscordRole;
import com.alttd.proxydiscordlink.util.ALogger;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BotConfig {
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
        CONFIG_FILE = new File(CONFIGPATH, "bot-config.yml");

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

        readConfig(BotConfig.class, null);
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

    public static String BOT_TOKEN = "unconfigured";
    public static long COMMAND_CHANNEL = -1;
    public static long STAFF_COMMAND_CHANNEL = -1;
    public static long LINK_CHANNEL = -1;
    public static long GUILD_ID = -1;
    public static long EVIDENCE_CHANNEL_ID = -1;
    public static long LINKED_ROLE_ID = -1;

    private static void settings() {
        BOT_TOKEN = getString("settings.token", BOT_TOKEN);
        COMMAND_CHANNEL = getLong("settings.command-channel", COMMAND_CHANNEL);
        LINK_CHANNEL = getLong("settings.link-channel", LINK_CHANNEL);
        GUILD_ID = getLong("settings.guild-id", GUILD_ID);
        EVIDENCE_CHANNEL_ID = getLong("settings.evidence-channel-id", EVIDENCE_CHANNEL_ID);
        LINKED_ROLE_ID = getLong("settings.linked-role-id", LINKED_ROLE_ID);
    }

    public static String SL_MINIMUMRANK = "trainee";
    public static String SL_HOVERMESSAGE = "Click here to message %player% on %servername%.";
    public static String SL_CLICKCOMMAND = "/msg %player%";

    private static void stafflist() {
        SL_MINIMUMRANK = getString("commands.staff-list.minimum-rank", SL_MINIMUMRANK);
        SL_HOVERMESSAGE = getString("commands.staff-list.hover-message", SL_HOVERMESSAGE);
        SL_CLICKCOMMAND = getString("commands.staff-list.click-command", SL_CLICKCOMMAND);
    }

    public static Map<Long, String> prefixMap = new HashMap<>();

    private static void prefix() {
        prefixMap.clear();
        ConfigurationNode node = getNode("prefixes");
        if (node.getChildrenMap().isEmpty()) {
            ALogger.warn("No prefixes found in BotConfig, add them to use commands:\n" +
                    "prefixes:\n\t" +
                    "server_id: prefix");
        }
        node.getChildrenMap().forEach((key, value) -> {
            prefixMap.put((Long) key, value.getString());
        });
    }

    private static void roles() {
        DiscordRole.cleanDiscordRoles();
        ConfigurationNode node = getNode("sync-roles");
        if (node.getChildrenMap().isEmpty())
            ALogger.warn("No roles found in BotConfig, add them to use sync-roles feature:\n" +
                    "sync-roles:\n\t" +
                    "example_rank:\n\t\t" +
                    "role-id: 0\n\t\t" +
                    "luckperms-name: example\n\t\t" +
                    "display-name: Example Rank\n\t\t" +
                    "update-to-minecraft: true\n\t\t" +
                    "update-to-discord: true\n\t\t" +
                    "announcement: <player> got example rank!");
        node.getChildrenMap().forEach((key, value) -> {
            String internalName = key.toString();
            long id = value.getNode("role-id").getLong(-1);
            String luckpermsName = value.getNode("luckperms-name").getString("example");
            String display_name = value.getNode("display-name").getString("Example");
            boolean updateToMinecraft = value.getNode("update-to-minecraft").getBoolean(false);
            boolean updateToDiscord = value.getNode("update-to-discord").getBoolean(false);
            String announcement = value.getNode("announcement").getString("<player> got example rank!");

            if (id == -1)
                ALogger.error("Invalid id in BotConfig for roles.");
            else
                DiscordRole.addDiscordRole(new DiscordRole(internalName, id, luckpermsName, display_name, updateToMinecraft, updateToDiscord, announcement));

        });
    }
}
