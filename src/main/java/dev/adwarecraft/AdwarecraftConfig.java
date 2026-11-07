package dev.adwarecraft;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Small persistent client configuration stored in config/adwarecraft.properties. */
public final class AdwarecraftConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("adwarecraft.properties");

    public static boolean enabled = true;
    public static boolean gameplayAds = true;
    public static boolean screenAds = true;
    public static int intervalSeconds = 45;
    public static int durationSeconds = 10;

    private AdwarecraftConfig() { }

    public static void load() {
        Properties properties = new Properties();
        if (Files.exists(FILE)) {
            try (InputStream input = Files.newInputStream(FILE)) {
                properties.load(input);
                enabled = Boolean.parseBoolean(properties.getProperty("enabled", "true"));
                gameplayAds = Boolean.parseBoolean(properties.getProperty("gameplayAds", "true"));
                screenAds = Boolean.parseBoolean(properties.getProperty("screenAds", "true"));
                intervalSeconds = parseInt(properties.getProperty("intervalSeconds"), 45, 15, 180);
                durationSeconds = parseInt(properties.getProperty("durationSeconds"), 10, 5, 20);
            } catch (IOException exception) {
                System.err.println("[Adwarecraft] Could not load config: " + exception.getMessage());
            }
        }
    }

    public static void save() {
        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(enabled));
        properties.setProperty("gameplayAds", Boolean.toString(gameplayAds));
        properties.setProperty("screenAds", Boolean.toString(screenAds));
        properties.setProperty("intervalSeconds", Integer.toString(intervalSeconds));
        properties.setProperty("durationSeconds", Integer.toString(durationSeconds));
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

    public static int gameplayIntervalTicks() {
        return intervalSeconds * 20;
    }

    public static int durationTicks() {
        return durationSeconds * 20;
    }
}
