package com.imherobrine.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class HerobrineClientConfig {
    private static final Path FILE = Path.of("config", "imherobrine-client.properties");
    private static boolean previewEnabled = true;

    private HerobrineClientConfig() {
    }

    public static boolean previewEnabled() {
        return previewEnabled;
    }

    public static void togglePreview() {
        previewEnabled = !previewEnabled;
        Properties properties = new Properties();
        properties.setProperty("preview.structures", Boolean.toString(previewEnabled));
        try {
            Files.createDirectories(FILE.getParent());
            try (var writer = Files.newBufferedWriter(FILE)) {
                properties.store(writer, "I'm Herobrine client settings");
            }
        } catch (IOException exception) {
            previewEnabled = !previewEnabled;
        }
    }

    static {
        try {
            if (Files.exists(FILE)) {
                Properties properties = new Properties();
                try (var reader = Files.newBufferedReader(FILE)) {
                    properties.load(reader);
                }
                previewEnabled = Boolean.parseBoolean(properties.getProperty("preview.structures", "true"));
            }
        } catch (IOException ignored) {
            previewEnabled = true;
        }
    }
}
