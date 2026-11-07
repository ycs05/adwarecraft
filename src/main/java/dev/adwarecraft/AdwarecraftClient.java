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

/** Client-side ad satire. Ads are decorative and never consume mouse or gameplay input. */
public final class AdwarecraftClient implements ClientModInitializer {
    private static final int SCREEN_AD_COOLDOWN = 20 * 18;
    private static final int COPY_ROTATION_TICKS = 40;
    private static final int BRAND_COUNT = 5;
    private static final int AD_TYPE_COUNT = 6;
    private static final int CHAT_COPY_COUNT = 6;
    private static final Random RANDOM = new Random();

    private static KeyBinding dismissKey;
    private static KeyBinding settingsKey;
    private static int gameplayTicks;
    private static int chatTicks;
    private static int adTicksRemaining;
    private static int adNumber;
    private static int adAge;
    private static int skipStyle;
    private static int adType;
    private static long lastAdStartedAt = Long.MIN_VALUE / 2;
    private static Screen lastScreen;

    @Override
    public void onInitializeClient() {
        AdwarecraftConfig.load();
        gameplayTicks = AdwarecraftConfig.gameplayIntervalTicks();
        chatTicks = AdwarecraftConfig.chatIntervalTicks();
        dismissKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.adwarecraft.dismiss_ad", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X,
                "category.adwarecraft"));
        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.adwarecraft.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K,
                "category.adwarecraft"));

        ClientTickEvents.END_CLIENT_TICK.register(AdwarecraftClient::tick);
        HudRenderCallback.EVENT.register((context, tickDelta) -> renderHudAd(context));
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) ->
                ScreenEvents.afterRender(screen).register((renderedScreen, context, mouseX, mouseY, tickDelta) ->
                        renderScreenAd(context)));
    }

    private static void tick(MinecraftClient client) {
        if (settingsKey.wasPressed()) {
            client.setScreen(new AdwarecraftSettingsScreen(client.currentScreen));
        }
        if (client.player == null || client.world == null) {
            lastScreen = client.currentScreen;
            return;
        }
        if (!AdwarecraftConfig.enabled) {
            adTicksRemaining = 0;
            lastScreen = client.currentScreen;
            return;
        }

        tickChatAds(client);
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
            dismissKey.wasPressed();
            if (AdwarecraftConfig.gameplayAds) {
                gameplayTicks--;
                if (gameplayTicks <= 0) {
                    gameplayTicks = AdwarecraftConfig.gameplayIntervalTicks();
                    startAd(client);
                }
            } else {
                gameplayTicks = AdwarecraftConfig.gameplayIntervalTicks();
            }

            boolean openedScreen = client.currentScreen != lastScreen;
            if (AdwarecraftConfig.screenAds && openedScreen && isAdScreen(client.currentScreen)
                    && client.world.getTime() - lastAdStartedAt >= SCREEN_AD_COOLDOWN) {
                startAd(client);
            }
        }
        lastScreen = client.currentScreen;
    }

    private static void tickChatAds(MinecraftClient client) {
        if (!AdwarecraftConfig.chatAds) {
            chatTicks = AdwarecraftConfig.chatIntervalTicks();
            return;
        }
        if (--chatTicks <= 0) {
            int line = RANDOM.nextInt(CHAT_COPY_COUNT);
            client.player.sendMessage(Text.translatable("adwarecraft.chat.ad." + line), false);
            chatTicks = AdwarecraftConfig.chatIntervalTicks();
        }
    }

    private static boolean isAdScreen(Screen screen) {
        return screen instanceof GameMenuScreen || screen instanceof HandledScreen<?>;
    }

    private static void startAd(MinecraftClient client) {
        if (!AdwarecraftConfig.enabled || adTicksRemaining > 0 || client.player == null) return;
        adNumber++;
        adAge = 0;
        skipStyle = RANDOM.nextInt(4);
        adType = RANDOM.nextInt(AD_TYPE_COUNT);
        adTicksRemaining = AdwarecraftConfig.durationTicks();
        lastAdStartedAt = client.world == null ? 0 : client.world.getTime();
        client.player.sendMessage(Text.translatable("adwarecraft.chat.interruption", adNumber), true);
    }

    private static void closeAd(MinecraftClient client) {
        adTicksRemaining = 0;
        if (client.player != null) {
            client.player.sendMessage(Text.translatable("adwarecraft.chat.dismissed"), true);
        }
    }

    static void disableForSettings() {
        adTicksRemaining = 0;
    }

    private static void renderHudAd(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!AdwarecraftConfig.enabled || !AdwarecraftConfig.gameplayAds || adTicksRemaining <= 0
                || client.player == null || client.options.hudHidden) return;
        renderAdWindows(context, false);
    }

    private static void renderScreenAd(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!AdwarecraftConfig.enabled || adTicksRemaining <= 0 || client.player == null) return;
        boolean screenAd = isAdScreen(client.currentScreen);
        if (screenAd && !AdwarecraftConfig.screenAds) return;
        if (!screenAd && !AdwarecraftConfig.gameplayAds) return;
        renderAdWindows(context, screenAd);
    }

    private static void renderAdWindows(DrawContext context, boolean screenAd) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int count = Math.max(1, Math.min(3, AdwarecraftConfig.adWindows));

        // The large sponsor panel plus up to two smaller overlapping promo windows.
        int mainWidth = Math.min(350, width - 16);
        int mainHeight = screenAd ? 122 : 110;
        int mainLeft = (width - mainWidth) / 2;
        int mainTop = Math.max(8, height / (screenAd ? 5 : 4));
        drawMainAd(context, mainLeft, mainTop, mainWidth, mainHeight);

        if (count >= 2) {
            int smallWidth = Math.min(184, width - 16);
            int smallLeft = Math.max(8, width - smallWidth - 12);
            int smallTop = Math.min(height - 68, mainTop + mainHeight + 10);
            drawMiniWindow(context, smallLeft, smallTop, smallWidth, 58, 1);
        }
        if (count >= 3) {
            int smallWidth = Math.min(184, width - 16);
            int smallLeft = 8;
            int smallTop = Math.max(8, mainTop - 48);
            drawMiniWindow(context, smallLeft, smallTop, smallWidth, 58, 2);
        }
        if (AdwarecraftConfig.messageBoxAds) {
            drawMessageBox(context, width, height, mainTop, mainHeight);
        }
    }

    private static void drawMainAd(DrawContext context, int left, int top, int width, int height) {
        MinecraftClient client = MinecraftClient.getInstance();
        int copyIndex = (adNumber - 1 + adAge / COPY_ROTATION_TICKS) % 30;
        int secondsLeft = (adTicksRemaining + 19) / 20;

        context.fill(left, top, left + width, top + height, 0xF0181420);
        context.fill(left, top, left + width, top + 3, typeAccent());
        context.fill(left + 7, top + 9, left + width - 7, top + height - 7, 0xFF282033);
        drawTinyAdArt(context, left + 14, top + 20, adAge, adType);
        context.drawTextWithShadow(client.textRenderer,
                Text.translatable("adwarecraft.type." + typeKey(adType)), left + 66, top + 12, typeAccent());
        context.drawTextWithShadow(client.textRenderer,
                Text.translatable("adwarecraft.brand." + brandKey((adNumber - 1) % BRAND_COUNT)),
                left + 66, top + 28, 0xFFFFD35A);
        context.drawTextWithShadow(client.textRenderer,
                Text.translatable("adwarecraft.ad.copy." + copyIndex), left + 66, top + 44, 0xFFFFFFFF);
        context.drawTextWithShadow(client.textRenderer,
                Text.translatable("adwarecraft.ad.sponsored", adNumber), left + 66, top + 60, 0xFFBDB7C7);
        renderSkipJoke(context, left, top, width, height);
        context.drawCenteredTextWithShadow(client.textRenderer,
                Text.translatable("adwarecraft.ad.footer", secondsLeft),
                left + width / 2, top + height - 16, 0xFFBDB7C7);
    }

    private static void drawMiniWindow(DrawContext context, int left, int top, int width, int height, int index) {
        MinecraftClient client = MinecraftClient.getInstance();
        context.fill(left, top, left + width, top + height, 0xF0261C31);
        context.fill(left, top, left + width, top + 12, 0xFFB43A57);
        context.drawTextWithShadow(client.textRenderer,
                Text.translatable("adwarecraft.window.title", index), left + 5, top + 2, 0xFFFFFFFF);
        context.drawTextWithShadow(client.textRenderer,
                Text.translatable("adwarecraft.window.body"), left + 7, top + 20, 0xFFFFE6A3);
        context.drawTextWithShadow(client.textRenderer,
                Text.translatable("adwarecraft.window.close_joke"), left + 7, top + 36, 0xFFBDB7C7);
    }

    private static void drawMessageBox(DrawContext context, int screenWidth, int screenHeight, int mainTop, int mainHeight) {
        MinecraftClient client = MinecraftClient.getInstance();
        int width = Math.min(250, screenWidth - 24);
        int height = 88;
        int left = (screenWidth - width) / 2;
        int top = Math.min(screenHeight - height - 8, mainTop + mainHeight + 16);
        context.fill(left, top, left + width, top + height, 0xFF101018);
        context.fill(left + 2, top + 2, left + width - 2, top + 19, 0xFF344C79);
        context.drawTextWithShadow(client.textRenderer, Text.translatable("adwarecraft.message.title"), left + 8, top + 6, 0xFFFFFFFF);
        context.drawTextWithShadow(client.textRenderer, Text.translatable("adwarecraft.message.body"), left + 10, top + 29, 0xFFFFFFFF);
        context.fill(left + width - 76, top + 58, left + width - 12, top + 78, 0xFF52658C);
        context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("adwarecraft.message.ok"), left + width - 44, top + 64, 0xFFFFFFFF);
    }

    private static int typeAccent() {
        return switch (adType) {
            case 1 -> 0xFFFF5B55;
            case 2 -> 0xFF63C7FF;
            case 3 -> 0xFFFFD34D;
            case 4 -> 0xFFFF76D7;
            case 5 -> 0xFF94E36F;
            default -> 0xFFFFB000;
        };
    }

    private static String typeKey(int index) {
        return switch (index) {
            case 1 -> "flash_sale";
            case 2 -> "fake_system";
            case 3 -> "daily_reward";
            case 4 -> "short_video";
            case 5 -> "survey";
            default -> "sponsored";
        };
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

    private static void drawTinyAdArt(DrawContext context, int x, int y, int age, int type) {
        int bob = (age / 5) % 2;
        context.fill(x, y + 3 + bob, x + 42, y + 39 + bob, 0xFF101019);
        if (type == 1) {
            context.fill(x + 9, y + 10 + bob, x + 32, y + 29 + bob, 0xFFFF5B55);
            context.fill(x + 15, y + 14 + bob, x + 27, y + 18 + bob, 0xFFFFFFFF);
            context.fill(x + 19, y + 22 + bob, x + 29, y + 25 + bob, 0xFFFFFF55);
        } else if (type == 2) {
            context.fill(x + 11, y + 8 + bob, x + 31, y + 32 + bob, 0xFF63C7FF);
            context.fill(x + 15, y + 12 + bob, x + 27, y + 15 + bob, 0xFFFFFFFF);
            context.fill(x + 15, y + 19 + bob, x + 27, y + 22 + bob, 0xFFFFFFFF);
            context.fill(x + 15, y + 26 + bob, x + 24, y + 29 + bob, 0xFFFFFFFF);
        } else if (type == 3) {
            context.fill(x + 10, y + 16 + bob, x + 32, y + 31 + bob, 0xFFFFD34D);
            context.fill(x + 19, y + 14 + bob, x + 23, y + 31 + bob, 0xFFFF5B55);
            context.fill(x + 8, y + 12 + bob, x + 34, y + 17 + bob, 0xFFFF5B55);
        } else if (type == 4) {
            context.fill(x + 10, y + 8 + bob, x + 32, y + 32 + bob, 0xFFFF76D7);
            context.fill(x + 18, y + 14 + bob, x + 22, y + 27 + bob, 0xFFFFFFFF);
            context.fill(x + 22, y + 17 + bob, x + 26, y + 24 + bob, 0xFFFFFFFF);
        } else if (type == 5) {
            for (int i = 0; i < 5; i++) {
                context.fill(x + 5 + i * 7, y + 17 + bob, x + 10 + i * 7, y + 24 + bob,
                        i < ((age / 10) % 6) ? 0xFFFFD34D : 0xFF77717E);
            }
        } else {
            context.fill(x + 13, y + 8 + bob, x + 29, y + 30 + bob, 0xFF27C879);
            context.fill(x + 17, y + 5 + bob, x + 25, y + 8 + bob, 0xFF66F0A5);
            context.fill(x + 17, y + 30 + bob, x + 25, y + 34 + bob, 0xFF11844B);
            context.fill(x + 17, y + 10 + bob, x + 21, y + 23 + bob, 0xFFB4FFD4);
        }
    }

    private static void renderSkipJoke(DrawContext context, int left, int top, int width, int height) {
        MinecraftClient client = MinecraftClient.getInstance();
        int buttonRight = left + width - 12;
        int buttonTop = top + height - 40;
        int wait = Math.max(0, 5 - adAge / 20);
        int buttonLeft = buttonRight - 76;
        if (skipStyle == 2) {
            context.drawTextWithShadow(client.textRenderer, Text.translatable("adwarecraft.skip.unavailable"), left + 66, buttonTop + 5, 0xFF8F8997);
            return;
        }
        if (skipStyle == 3) {
            context.drawTextWithShadow(client.textRenderer, Text.translatable("adwarecraft.skip.no_skip"), left + 66, buttonTop + 5, 0xFF8F8997);
            return;
        }
        Text label = skipStyle == 0 && wait > 0
                ? Text.translatable("adwarecraft.skip.wait", wait)
                : Text.translatable("adwarecraft.skip.skip_now");
        int color = skipStyle == 1 ? 0xFFB43542 : (wait > 0 ? 0xFF57515E : 0xFF426A53);
        context.fill(buttonLeft, buttonTop, buttonRight, buttonTop + 19, color);
        context.drawCenteredTextWithShadow(client.textRenderer, label, buttonLeft + 38, buttonTop + 6, 0xFFFFFFFF);
        if (skipStyle == 1) {
            context.drawTextWithShadow(client.textRenderer, Text.translatable("adwarecraft.skip.decorative"), buttonLeft - 63, buttonTop + 6, 0xFFFF9191);
        } else if (adAge >= 100) {
            context.drawTextWithShadow(client.textRenderer, Text.translatable("adwarecraft.skip.not_real"), buttonLeft - 83, buttonTop + 6, 0xFFFF9191);
        }
    }
}
