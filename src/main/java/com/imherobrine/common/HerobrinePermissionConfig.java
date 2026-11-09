package com.imherobrine.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public final class HerobrinePermissionConfig {
    private static final System.Logger LOGGER = System.getLogger("imherobrine");
    private static final String FILE_NAME = "imherobrine-server.properties";
    private static final int DEFAULT_PERMISSION_LEVEL = 2;
    private static final int MIN_PERMISSION_LEVEL = 0;
    private static final int MAX_PERMISSION_LEVEL = 4;
    private static final int DEFAULT_LEAF_RADIUS = 30;
    private static final int DEFAULT_TREE_TORCH_RADIUS = 30;
    private static final int MAX_ACTION_RADIUS = 70;
    private static final Map<HerobrineAction, Integer> LEVELS = new EnumMap<>(HerobrineAction.class);
    private static volatile int leafRadius = DEFAULT_LEAF_RADIUS;
    private static volatile int treeTorchRadius = DEFAULT_TREE_TORCH_RADIUS;
    private static volatile int commandPermissionLevel = DEFAULT_PERMISSION_LEVEL;

    static {
        for (HerobrineAction action : HerobrineAction.values()) {
            if (!action.hasPermissionSetting()) {
                continue;
            }
            LEVELS.put(action, DEFAULT_PERMISSION_LEVEL);
        }
    }

    private HerobrinePermissionConfig() {
    }

    public static synchronized void load(MinecraftServer server) {
        Path configFile = configFile(server);
        Properties properties = new Properties();
        leafRadius = DEFAULT_LEAF_RADIUS;
        treeTorchRadius = DEFAULT_TREE_TORCH_RADIUS;
        commandPermissionLevel = DEFAULT_PERMISSION_LEVEL;
        for (HerobrineAction action : HerobrineAction.values()) {
            if (!action.hasPermissionSetting()) {
                continue;
            }
            LEVELS.put(action, DEFAULT_PERMISSION_LEVEL);
        }
        try {
            Files.createDirectories(configFile.getParent());
            Path structuresDirectory = configFile.getParent().resolve("imherobrine").resolve("structures");
            Files.createDirectories(structuresDirectory);
            Path example = structuresDirectory.resolve("example.json");
            if (Files.notExists(example)) {
                Files.writeString(example, "{\n  \"size\": [3, 2, 3],\n  \"blocks\": [\n    {\"x\": 1, \"y\": 0, \"z\": 1, \"block\": \"minecraft:gold_block\"},\n    {\"x\": 1, \"y\": 1, \"z\": 1, \"block\": \"minecraft:glowstone\"}\n  ]\n}\n");
            }
            if (Files.exists(configFile)) {
                try (Reader reader = Files.newBufferedReader(configFile)) {
                    properties.load(reader);
                }
            }

            for (HerobrineAction action : HerobrineAction.values()) {
                if (!action.hasPermissionSetting()) {
                    continue;
                }
                String key = configKey(action);
                int level = parseLevel(properties.getProperty(key), DEFAULT_PERMISSION_LEVEL, key);
                LEVELS.put(action, level);
                properties.setProperty(key, Integer.toString(level));
            }

            commandPermissionLevel = parseLevel(properties.getProperty("permissions.commands"), DEFAULT_PERMISSION_LEVEL, "permissions.commands");
            properties.setProperty("permissions.commands", Integer.toString(commandPermissionLevel));

            leafRadius = parseRadius(properties.getProperty("ranges.leaf_clear"), DEFAULT_LEAF_RADIUS, "ranges.leaf_clear");
            treeTorchRadius = parseRadius(properties.getProperty("ranges.tree_torches"), DEFAULT_TREE_TORCH_RADIUS, "ranges.tree_torches");
            properties.setProperty("ranges.leaf_clear", Integer.toString(leafRadius));
            properties.setProperty("ranges.tree_torches", Integer.toString(treeTorchRadius));

            save(configFile, properties);
        } catch (IOException exception) {
            LOGGER.log(System.Logger.Level.ERROR, "Could not load server permissions from " + configFile, exception);
        }
    }

    public static boolean canUse(ServerPlayer player, HerobrineAction action) {
        int requiredLevel = LEVELS.getOrDefault(action.permissionAction(), DEFAULT_PERMISSION_LEVEL);
        return player.hasPermissions(requiredLevel);
    }

    public static int leafRadius() {
        return leafRadius;
    }

    public static int treeTorchRadius() {
        return treeTorchRadius;
    }

    public static int commandPermissionLevel() {
        return commandPermissionLevel;
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("imherobrine")
                .requires(source -> source.hasPermission(commandPermissionLevel));
        root.then(Commands.literal("radius")
                .then(Commands.literal("leaves")
                        .then(Commands.argument("blocks", IntegerArgumentType.integer(0, MAX_ACTION_RADIUS))
                                .executes(context -> setRadius(context.getSource(), "leaves", IntegerArgumentType.getInteger(context, "blocks")))))
                .then(Commands.literal("tree_torches")
                        .then(Commands.argument("blocks", IntegerArgumentType.integer(0, MAX_ACTION_RADIUS))
                                .executes(context -> setRadius(context.getSource(), "tree_torches", IntegerArgumentType.getInteger(context, "blocks"))))));
        root.then(Commands.literal("permission")
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("commands");
                            for (HerobrineAction action : HerobrineAction.values()) {
                                if (action.hasPermissionSetting()) {
                                    builder.suggest(action.name().toLowerCase(Locale.ROOT));
                                }
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("level", IntegerArgumentType.integer(MIN_PERMISSION_LEVEL, MAX_PERMISSION_LEVEL))
                                .executes(context -> setPermission(context.getSource(),
                                        StringArgumentType.getString(context, "name"),
                                        IntegerArgumentType.getInteger(context, "level"))))));
        root.then(Commands.literal("import")
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            if (!canUse(player, HerobrineAction.BUILD_STRUCTURE)) {
                                context.getSource().sendFailure(Component.literal("You do not have permission to import structures."));
                                return 0;
                            }
                            return HerobrineStructureBuilder.importStructure(player, StringArgumentType.getString(context, "name"));
                        })));
        dispatcher.register(root);
    }

    private static int setRadius(CommandSourceStack source, String type, int radius) {
        MinecraftServer server = source.getServer();
        synchronized (HerobrinePermissionConfig.class) {
            String key = type.equals("leaves") ? "ranges.leaf_clear" : "ranges.tree_torches";
            if (!saveProperty(server, key, Integer.toString(radius))) {
                source.sendFailure(Component.literal("Could not save the I'm Herobrine server configuration."));
                return 0;
            }
            if (type.equals("leaves")) {
                leafRadius = radius;
            } else {
                treeTorchRadius = radius;
            }
        }
        source.sendSuccess(() -> Component.literal("I'm Herobrine " + type + " radius set to " + radius + " blocks."), true);
        return 1;
    }

    private static int setPermission(CommandSourceStack source, String name, int level) {
        MinecraftServer server = source.getServer();
        String normalized = name.toLowerCase(Locale.ROOT);
        String key;
        HerobrineAction action = null;
        if (normalized.equals("commands")) {
            key = "permissions.commands";
        } else {
            try {
                action = HerobrineAction.valueOf(normalized.toUpperCase(Locale.ROOT));
                if (!action.hasPermissionSetting()) {
                    source.sendFailure(Component.literal("Unknown configurable action: " + name));
                    return 0;
                }
            } catch (IllegalArgumentException exception) {
                source.sendFailure(Component.literal("Unknown action: " + name));
                return 0;
            }
            key = configKey(action);
        }

        synchronized (HerobrinePermissionConfig.class) {
            if (!saveProperty(server, key, Integer.toString(level))) {
                source.sendFailure(Component.literal("Could not save the I'm Herobrine server configuration."));
                return 0;
            }
            if (action == null) {
                commandPermissionLevel = level;
            } else {
                LEVELS.put(action, level);
            }
        }
        source.sendSuccess(() -> Component.literal("I'm Herobrine permission " + normalized + " set to operator level " + level + "."), true);
        return 1;
    }

    private static boolean saveProperty(MinecraftServer server, String key, String value) {
        Path configFile = configFile(server);
        try {
            Properties properties = new Properties();
            if (Files.exists(configFile)) {
                try (Reader reader = Files.newBufferedReader(configFile)) {
                    properties.load(reader);
                }
            }
            properties.setProperty(key, value);
            for (HerobrineAction action : HerobrineAction.values()) {
                if (!action.hasPermissionSetting()) {
                    continue;
                }
                properties.putIfAbsent(configKey(action), Integer.toString(LEVELS.getOrDefault(action, DEFAULT_PERMISSION_LEVEL)));
            }
            properties.putIfAbsent("permissions.commands", Integer.toString(commandPermissionLevel));
            properties.putIfAbsent("ranges.leaf_clear", Integer.toString(leafRadius));
            properties.putIfAbsent("ranges.tree_torches", Integer.toString(treeTorchRadius));
            Files.createDirectories(configFile.getParent());
            save(configFile, properties);
            return true;
        } catch (IOException exception) {
            LOGGER.log(System.Logger.Level.ERROR, "Could not save server configuration to " + configFile, exception);
            return false;
        }
    }

    private static Path configFile(MinecraftServer server) {
        return server.getServerDirectory().resolve("config").resolve(FILE_NAME);
    }

    private static void save(Path configFile, Properties properties) throws IOException {
        try (Writer writer = Files.newBufferedWriter(configFile)) {
            properties.store(writer, null);
        }
    }

    private static int parseLevel(String value, int fallback, String key) {
        if (value == null) {
            return fallback;
        }
        try {
            int level = Integer.parseInt(value.trim());
            if (level >= MIN_PERMISSION_LEVEL && level <= MAX_PERMISSION_LEVEL) {
                return level;
            }
        } catch (NumberFormatException ignored) {
        }
        LOGGER.log(System.Logger.Level.WARNING, "Invalid operator level for " + key + "; using " + fallback);
        return fallback;
    }

    private static int parseRadius(String value, int fallback, String key) {
        if (value == null) {
            return fallback;
        }
        try {
            int radius = Integer.parseInt(value.trim());
            if (radius >= 0 && radius <= MAX_ACTION_RADIUS) {
                return radius;
            }
        } catch (NumberFormatException ignored) {
        }
        LOGGER.log(System.Logger.Level.WARNING, "Invalid radius for " + key + "; using " + fallback);
        return fallback;
    }

    private static String configKey(HerobrineAction action) {
        return "permissions." + action.permissionAction().name().toLowerCase(Locale.ROOT);
    }
}
