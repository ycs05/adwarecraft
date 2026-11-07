package dev.adwarecraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Client-side pop-up ad satire. It never pauses the game or takes control away. */
public final class AdwarecraftClient implements ClientModInitializer {
    private static final int AD_INTERVAL_TICKS = 20 * 180;
    private static final int AD_DURATION_TICKS = 20 * 12;
    private static final int ROTATION_TICKS = 20 * 3;

    private static final String[] AD_COPY = {
            "Your pickaxe has been selected for a limited-time offer!",
            "Mine faster with Premium™! (No actual speed increase.)",
            "This cave is brought to you by DefinitelyNotCreepers™.",
            "Accept cookies? We baked 64. They're in your inventory."
    };

    private static KeyBinding dismissKey;
    private static int ticksUntilAd = AD_INTERVAL_TICKS;
    private static int adTicksRemaining;
    private static int adNumber;

    @Override
    public void onInitializeClient() {
        dismissKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.adwarecraft.dismiss_ad",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category.adwarecraft"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(AdwarecraftClient::tick);
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> renderAd(drawContext));
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        if (dismissKey.wasPressed() && adTicksRemaining > 0) {
            adTicksRemaining = 0;
            client.player.sendMessage(Text.literal("Ad dismissed. Thanks for your feedback (which we ignored)."), true);
            return;
        }

        if (adTicksRemaining > 0) {
            adTicksRemaining--;
            if (adTicksRemaining == 0) {
                client.player.sendMessage(Text.literal("Thanks for watching! Your reward is more ads."), true);
            }
            return;
        }

        ticksUntilAd--;
        if (ticksUntilAd <= 0) {
            ticksUntilAd = AD_INTERVAL_TICKS;
            adTicksRemaining = AD_DURATION_TICKS;
            adNumber++;
            client.player.sendMessage(Text.literal("Sponsored interruption #" + adNumber + "! Press X to dismiss."), true);
        }
    }

    private static void renderAd(net.minecraft.client.gui.DrawContext context) {
        if (adTicksRemaining <= 0) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        int width = 300;
        int height = 86;
        int left = (context.getScaledWindowWidth() - width) / 2;
        int top = Math.max(12, context.getScaledWindowHeight() / 4);
        int copyIndex = (adNumber - 1 + (AD_DURATION_TICKS - adTicksRemaining) / ROTATION_TICKS) % AD_COPY.length;
        int secondsLeft = (adTicksRemaining + 19) / 20;

        context.fill(left, top, left + width, top + height, 0xEE17131F);
        context.fill(left, top, left + width, top + 3, 0xFFFFB000);
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal("✦ TOTALLY OPTIONAL SPONSOR ✦"), left + width / 2, top + 10, 0xFFFFD35A);
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal(AD_COPY[copyIndex]), left + width / 2, top + 31, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal("X: dismiss  •  auto-closes in " + secondsLeft + "s"), left + width / 2, top + 55, 0xFFBDB7C7);
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal("Ad " + adNumber + " of an aggressively growing number"), left + width / 2, top + 70, 0xFF85808D);
    }
}
