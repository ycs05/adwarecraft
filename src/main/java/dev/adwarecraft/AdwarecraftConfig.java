package dev.adwarecraft;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Persistent client settings stored in config/adwarecraft.properties. */
public final class AdwarecraftConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("adwarecraft.properties");

    public static boolean enabled = true;
    public static boolean gameplayAds = true;
    public static boolean screenAds = true;
    public static boolean chatAds = true;
    public static boolean messageBoxAds = true;
    public static int intervalSeconds = 45;
    public static int durationSeconds = 10;
    public static int chatIntervalSeconds = 60;
    public static int adWindows = 2;

    private AdwarecraftConfig() { }

    public static void load() {
        Properties properties = new Properties();
        if (!Files.exists(FILE)) return;
        try (InputStream input = Files.newInputStream(FILE)) {
            properties.load(input);
            enabled = Boolean.parseBoolean(properties.getProperty("enabled", "true"));
            gameplayAds = Boolean.parseBoolean(properties.getProperty("gameplayAds", "true"));
            screenAds = Boolean.parseBoolean(properties.getProperty("screenAds", "true"));
            chatAds = Boolean.parseBoolean(properties.getProperty("chatAds", "true"));
            messageBoxAds = Boolean.parseBoolean(properties.getProperty("messageBoxAds", "true"));
            intervalSeconds = parseInt(properties.getProperty("intervalSeconds"), 45, 15, 180);
            durationSeconds = parseInt(properties.getProperty("durationSeconds"), 10, 5, 20);
            chatIntervalSeconds = parseInt(properties.getProperty("chatIntervalSeconds"), 60, 30, 180);
            adWindows = parseInt(properties.getProperty("adWindows"), 2, 1, 3);
        } catch (IOException exception) {
            System.err.println("[Adwarecraft] Could not load config: " + exception.getMessage());
        }
    }

    public static void save() {
        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(enabled));
        properties.setProperty("gameplayAds", Boolean.toString(gameplayAds));
        properties.setProperty("screenAds", Boolean.toString(screenAds));
        properties.setProperty("chatAds", Boolean.toString(chatAds));
        properties.setProperty("messageBoxAds", Boolean.toString(messageBoxAds));
        properties.setProperty("intervalSeconds", Integer.toString(intervalSeconds));
        properties.setProperty("durationSeconds", Integer.toString(durationSeconds));
        properties.setProperty("chatIntervalSeconds", Integer.toString(chatIntervalSeconds));
        properties.setProperty("adWindows", Integer.toString(adWindows));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream output = Files.newOutputStream(FILE)) {
                properties.store(output, "Adwarecraft settings");
            }
        } catch (IOException exception) {
            System.err.println("[Adwarecraft] Could not save config: " + exception.getMessage());
        }
    }

    private static int parseInt(String value, int fallback, int min, int max) {
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(value)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    public static int gameplayIntervalTicks() { return intervalSeconds * 20; }
    public static int durationTicks() { return durationSeconds * 20; }
    public static int chatIntervalTicks() { return chatIntervalSeconds * 20; }
}
