package dev.adwarecraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

/** A harmless client-side satire: ads are visual only and never intercept gameplay input. */
public final class AdwarecraftClient implements ClientModInitializer {
    private static final int GAMEPLAY_INTERVAL = 20 * 45;
    private static final int AD_DURATION = 20 * 10;
    private static final int SCREEN_AD_COOLDOWN = 20 * 18;
    private static final int COPY_ROTATION_TICKS = 20 * 2;

    private static final String[] AD_COPY = {
            "Your pickaxe has been selected for a limited-time offer!",
            "Mine faster with Premium™! (No actual speed increase.)",
            "This cave is brought to you by DefinitelyNotCreepers™.",
            "Accept cookies? We baked 64. They're in your inventory.",
            "Upgrade your dirt to Dirt Plus. Same dirt, more subscription.",
            "Your crafting table is lonely. Try our premium crafting table!",
            "A creeper is approaching. Would you like to hear about our VPN?",
            "Sponsored by Notch's Totally Real Horse Insurance™.",
            "This ad is 100% free! Limited offer: only 9.99 emeralds/month.",
            "Low on health? Our wellness app recommends closing this ad.",
            "You have won! Tap absolutely nothing to claim nothing.",
            "Build a better base with Premium™. (This message is the whole product.)",
            "One weird trick: villagers hate this highly targeted promotion.",
            "Watch another ad to unlock the privilege of watching another ad.",
            "Your inventory has been optimized. Please enjoy this sponsored message.",
            "The End is near! This message was not reviewed by the Ender Dragon.",
            "Today's special: one suspiciously shiny block. Terms apply.",
            "We noticed you mined stone. Here are 400 ads for stone.",
            "Subscribe now and receive a complimentary imaginary diamond!",
            "Ad-free experience available after this brief 10-second interruption."
    };

    private static final String[] BRAND_NAMES = {
            "EMERALD+", "CREEPER VPN", "DIRT PLUS", "MINEFLIX", "SPONSORED BY YOU"
    };

    private static final int[] SKIP_STYLES = {0, 1, 2, 3};
    private static final Random RANDOM = new Random();
    private static KeyBinding dismissKey;
    private static int gameplayTicks = GAMEPLAY_INTERVAL;
    private static int adTicksRemaining;
    private static int adNumber;
    private static int adAge;
    private static int skipStyle;
    private static long lastAdStartedAt = Long.MIN_VALUE / 2;
    private static Screen lastScreen;

    @Override
    public void onInitializeClient() {
        dismissKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.adwarecraft.dismiss_ad", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X,
                "category.adwarecraft"));

        ClientTickEvents.END_CLIENT_TICK.register(AdwarecraftClient::tick);
        HudRenderCallback.EVENT.register((context, tickDelta) -> renderHudAd(context));
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) ->
                ScreenEvents.afterRender(screen).register((renderedScreen, context, mouseX, mouseY, tickDelta) ->
                        renderScreenAd(context)));
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            lastScreen = client.currentScreen;
            return;
        }

        if (adTicksRemaining > 0) {
            adAge++;
            if (dismissKey.wasPressed()) {
                closeAd(client, true);
                return;
            }
            adTicksRemaining--;
            if (adTicksRemaining == 0) {
                client.player.sendMessage(Text.literal("Thanks for watching! Your reward is more ads."), true);
            }
        } else {
            if (dismissKey.wasPressed()) {
                // Consume the keypress while no ad is showing; never let a queued press dismiss a later ad.
            }
            gameplayTicks--;
            if (gameplayTicks <= 0) {
                gameplayTicks = GAMEPLAY_INTERVAL;
                startAd(client);
            }

            boolean openedScreen = client.currentScreen != lastScreen;
            if (openedScreen && isAdScreen(client.currentScreen)
                    && client.world.getTime() - lastAdStartedAt >= SCREEN_AD_COOLDOWN) {
                startAd(client);
            }
        }
        lastScreen = client.currentScreen;
    }

    private static boolean isAdScreen(Screen screen) {
        return screen instanceof GameMenuScreen || screen instanceof HandledScreen<?>;
    }

    private static void startAd(MinecraftClient client) {
        if (adTicksRemaining > 0 || client.player == null) return;
        adNumber++;
        adAge = 0;
        skipStyle = SKIP_STYLES[RANDOM.nextInt(SKIP_STYLES.length)];
        adTicksRemaining = AD_DURATION;
        lastAdStartedAt = client.world == null ? 0 : client.world.getTime();
        client.player.sendMessage(Text.literal("Sponsored interruption #" + adNumber
                + "! Press X to dismiss whenever you like."), true);
    }

    private static void closeAd(MinecraftClient client, boolean manual) {
        adTicksRemaining = 0;
        if (manual && client.player != null) {
            client.player.sendMessage(Text.literal("Ad dismissed. Your feedback has been monetized."), true);
        }
    }

    private static void renderHudAd(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (adTicksRemaining <= 0 || client.player == null || client.options.hudHidden) return;
        renderAd(context, false);
    }

    private static void renderScreenAd(DrawContext context) {
        if (adTicksRemaining <= 0 || MinecraftClient.getInstance().player == null) return;
        renderAd(context, true);
    }

    private static void renderAd(DrawContext context, boolean screenAd) {
        MinecraftClient client = MinecraftClient.getInstance();
        int width = Math.min(330, context.getScaledWindowWidth() - 16);
        int height = screenAd ? 112 : 100;
        int left = (context.getScaledWindowWidth() - width) / 2;
        int top = Math.max(8, context.getScaledWindowHeight() / (screenAd ? 5 : 4));
        int copyIndex = (adNumber - 1 + adAge / COPY_ROTATION_TICKS) % AD_COPY.length;
        int secondsLeft = (adTicksRemaining + 19) / 20;

        context.fill(left, top, left + width, top + height, 0xF0181420);
        context.fill(left, top, left + width, top + 3, 0xFFFFB000);
        context.fill(left + 7, top + 9, left + width - 7, top + height - 7, 0xFF282033);
        drawTinyAdArt(context, left + 14, top + 20, adAge);

        String brand = BRAND_NAMES[(adNumber - 1) % BRAND_NAMES.length];
        context.drawTextWithShadow(client.textRenderer, Text.literal(brand), left + 66, top + 12, 0xFFFFD35A);
        context.drawTextWithShadow(client.textRenderer, Text.literal(AD_COPY[copyIndex]), left + 66, top + 28, 0xFFFFFFFF);
        context.drawTextWithShadow(client.textRenderer, Text.literal("Sponsored • Ad " + adNumber), left + 66, top + 44, 0xFFBDB7C7);
        renderSkipJoke(context, left, top, width, height, secondsLeft);
        context.drawCenteredTextWithShadow(client.textRenderer,
                Text.literal("X always closes this ad  |  auto-closes in " + secondsLeft + "s"),
                left + width / 2, top + height - 16, 0xFFBDB7C7);
    }

    /** Draws a tiny animated pixel-art product illustration; no external media or decoder needed. */
    private static void drawTinyAdArt(DrawContext context, int x, int y, int age) {
        int bob = (age / 5) % 2;
        context.fill(x, y + 3 + bob, x + 42, y + 39 + bob, 0xFF101019);
        // Emerald-shaped product badge.
        context.fill(x + 13, y + 8 + bob, x + 29, y + 30 + bob, 0xFF27C879);
        context.fill(x + 17, y + 5 + bob, x + 25, y + 8 + bob, 0xFF66F0A5);
        context.fill(x + 17, y + 30 + bob, x + 25, y + 34 + bob, 0xFF11844B);
        context.fill(x + 17, y + 10 + bob, x + 21, y + 23 + bob, 0xFFB4FFD4);
        // Animated sparkle.
        if ((age / 4) % 2 == 0) {
            context.fill(x + 33, y + 8, x + 37, y + 12, 0xFFFFFF55);
            context.fill(x + 35, y + 6, x + 36, y + 14, 0xFFFFFFAA);
        } else {
            context.fill(x + 5, y + 27, x + 9, y + 31, 0xFFFF71C8);
        }
    }

    /** Skip-button styles rotate like an obnoxious app, while X remains a real, reliable escape. */
    private static void renderSkipJoke(DrawContext context, int left, int top, int width, int height, int secondsLeft) {
        int buttonRight = left + width - 12;
        int buttonTop = top + height - 40;
        String label;
        int color;
        switch (skipStyle) {
            case 0 -> {
                int wait = Math.max(0, 5 - adAge / 20);
                label = wait > 0 ? "SKIP IN " + wait + "s" : "SKIP*";
                color = wait > 0 ? 0xFF57515E : 0xFF426A53;
            }
            case 1 -> {
                label = "SKIP NOW";
                color = 0xFFB43542;
            }
            case 2 -> {
                label = "";
                color = 0;
            }
            default -> {
                label = "";
                color = 0;
            }
        }

        if (skipStyle == 2) {
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    Text.literal("Skip button temporarily unavailable"), left + 66, buttonTop + 5, 0xFF8F8997);
        } else if (skipStyle == 3) {
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    Text.literal("No skip button. Terms apply."), left + 66, buttonTop + 5, 0xFF8F8997);
        } else {
            int buttonLeft = buttonRight - 76;
            context.fill(buttonLeft, buttonTop, buttonRight, buttonTop + 19, color);
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    Text.literal(label), buttonLeft + 38, buttonTop + 6, 0xFFFFFFFF);
            if (skipStyle == 1) {
                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                        Text.literal("(decorative)"), buttonLeft - 63, buttonTop + 6, 0xFFFF9191);
            } else if (adAge >= 20 * 5) {
                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                        Text.literal("*not a real button"), buttonLeft - 83, buttonTop + 6, 0xFFFF9191);
            }
        }
    }
}
