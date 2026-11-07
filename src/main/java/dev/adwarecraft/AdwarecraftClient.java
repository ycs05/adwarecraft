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
    private static final int AD_COPY_COUNT = 20;
    private static final int BRAND_COUNT = 5;
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
                closeAd(client);
                lastScreen = client.currentScreen;
                return;
            }
            adTicksRemaining--;
            if (adTicksRemaining == 0) {
                client.player.sendMessage(Text.translatable("adwarecraft.chat.thanks"), true);
            }
        } else {
            // Drain key presses while no ad is active, so one cannot dismiss a later ad accidentally.
            dismissKey.wasPressed();
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
        skipStyle = RANDOM.nextInt(4);
        adTicksRemaining = AD_DURATION;
        lastAdStartedAt = client.world == null ? 0 : client.world.getTime();
        client.player.sendMessage(Text.translatable("adwarecraft.chat.interruption", adNumber), true);
    }

    private static void closeAd(MinecraftClient client) {
        adTicksRemaining = 0;
        if (client.player != null) {
            client.player.sendMessage(Text.translatable("adwarecraft.chat.dismissed"), true);
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
        int copyIndex = (adNumber - 1 + adAge / COPY_ROTATION_TICKS) % AD_COPY_COUNT;
        int secondsLeft = (adTicksRemaining + 19) / 20;

        context.fill(left, top, left + width, top + height, 0xF0181420);
        context.fill(left, top, left + width, top + 3, 0xFFFFB000);
        context.fill(left + 7, top + 9, left + width - 7, top + height - 7, 0xFF282033);
        drawTinyAdArt(context, left + 14, top + 20, adAge);

        context.drawTextWithShadow(client.textRenderer,
                Text.translatable("adwarecraft.brand." + brandKey((adNumber - 1) % BRAND_COUNT)),
                left + 66, top + 12, 0xFFFFD35A);
        context.drawTextWithShadow(client.textRenderer,
                Text.translatable("adwarecraft.ad.copy." + copyIndex), left + 66, top + 28, 0xFFFFFFFF);
        context.drawTextWithShadow(client.textRenderer,
                Text.translatable("adwarecraft.ad.sponsored", adNumber), left + 66, top + 44, 0xFFBDB7C7);
        renderSkipJoke(context, left, top, width, height);
        context.drawCenteredTextWithShadow(client.textRenderer,
                Text.translatable("adwarecraft.ad.footer", secondsLeft),
                left + width / 2, top + height - 16, 0xFFBDB7C7);
    }

    private static String brandKey(int index) {
        return switch (index) {
            case 0 -> "emerald_plus";
            case 1 -> "creeper_vpn";
            case 2 -> "dirt_plus";
            case 3 -> "mineflix";
            default -> "sponsored_by_you";
        };
    }

    /** Draws a tiny animated pixel-art product illustration; no external media or decoder needed. */
    private static void drawTinyAdArt(DrawContext context, int x, int y, int age) {
        int bob = (age / 5) % 2;
        context.fill(x, y + 3 + bob, x + 42, y + 39 + bob, 0xFF101019);
        context.fill(x + 13, y + 8 + bob, x + 29, y + 30 + bob, 0xFF27C879);
        context.fill(x + 17, y + 5 + bob, x + 25, y + 8 + bob, 0xFF66F0A5);
        context.fill(x + 17, y + 30 + bob, x + 25, y + 34 + bob, 0xFF11844B);
        context.fill(x + 17, y + 10 + bob, x + 21, y + 23 + bob, 0xFFB4FFD4);
        if ((age / 4) % 2 == 0) {
            context.fill(x + 33, y + 8, x + 37, y + 12, 0xFFFFFF55);
            context.fill(x + 35, y + 6, x + 36, y + 14, 0xFFFFFFAA);
        } else {
            context.fill(x + 5, y + 27, x + 9, y + 31, 0xFFFF71C8);
        }
    }

    /** The fake skip controls are visual jokes; X is always a real and reliable escape. */
    private static void renderSkipJoke(DrawContext context, int left, int top, int width, int height) {
        int buttonRight = left + width - 12;
        int buttonTop = top + height - 40;
        int wait = Math.max(0, 5 - adAge / 20);
        int buttonLeft = buttonRight - 76;
        MinecraftClient client = MinecraftClient.getInstance();

        if (skipStyle == 2) {
            context.drawTextWithShadow(client.textRenderer,
                    Text.translatable("adwarecraft.skip.unavailable"), left + 66, buttonTop + 5, 0xFF8F8997);
            return;
        }
        if (skipStyle == 3) {
            context.drawTextWithShadow(client.textRenderer,
                    Text.translatable("adwarecraft.skip.no_skip"), left + 66, buttonTop + 5, 0xFF8F8997);
            return;
        }

        Text label = skipStyle == 0 && wait > 0
                ? Text.translatable("adwarecraft.skip.wait", wait)
                : Text.translatable("adwarecraft.skip.skip_now");
        int color = skipStyle == 1 ? 0xFFB43542 : (wait > 0 ? 0xFF57515E : 0xFF426A53);
        context.fill(buttonLeft, buttonTop, buttonRight, buttonTop + 19, color);
        context.drawCenteredTextWithShadow(client.textRenderer, label, buttonLeft + 38, buttonTop + 6, 0xFFFFFFFF);

        if (skipStyle == 1) {
            context.drawTextWithShadow(client.textRenderer,
                    Text.translatable("adwarecraft.skip.decorative"), buttonLeft - 63, buttonTop + 6, 0xFFFF9191);
        } else if (adAge >= 20 * 5) {
            context.drawTextWithShadow(client.textRenderer,
                    Text.translatable("adwarecraft.skip.not_real"), buttonLeft - 83, buttonTop + 6, 0xFFFF9191);
        }
    }
}
