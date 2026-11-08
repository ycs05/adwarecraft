package dev.adwarecraft;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

/** Extra client-side ad formats, event triggers, and deliberately harmless visual interactions. */
public final class AdwarecraftExtraFeatures implements ClientModInitializer {
    private static final int AD_DURATION_TICKS = 20 * 9;
    private static final int TRIGGER_COOLDOWN_TICKS = 20 * 25;
    private static final int VIRTUAL_PLAYER_LIFETIME = 20 * 30;
    private static final String[] TRIGGERS = {"health", "hunger", "dimension", "inventory", "night"};
    private static final String[] TYPES = {"influencer", "subscription", "breaking_news", "loot_crate", "music"};

    private static int ticksUntilAd = 20 * 75;
    private static int cooldown;
    private static int activeTicks;
    private static int currentType;
    private static int clickCount;
    private static int fakePlayerId = -1;
    private static int fakePlayerAge;
    private static boolean previousLeftMouse;
    private static boolean previouslyLowHealth;
    private static boolean previouslyLowHunger;
    private static boolean previouslyInScreen;
    private static Identifier previousDimension;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(AdwarecraftExtraFeatures::tick);
        // This initializer is registered after the original renderer, so this banner gets top visual priority.
        HudRenderCallback.EVENT.register((context, tickDelta) -> renderAd(context));
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null || !AdwarecraftConfig.enabled) {
            activeTicks = 0;
            previousLeftMouse = false;
            previousInScreen = client.currentScreen != null;
            previousDimension = null;
            fakePlayerId = -1;
            return;
        }

        if (previousDimension == null || !previousDimension.equals(client.world.getRegistryKey().getValue())) {
            if (previousDimension != null) trigger(client, "dimension");
            previousDimension = client.world.getRegistryKey().getValue();
        }

        boolean lowHealth = client.player.getHealth() <= 6.0F;
        boolean lowHunger = client.player.getHungerManager().getFoodLevel() <= 6;
        boolean inScreen = client.currentScreen != null;
        if (lowHealth && !previouslyLowHealth) trigger(client, "health");
        if (lowHunger && !previouslyLowHunger) trigger(client, "hunger");
        if (inScreen && !previouslyInScreen) trigger(client, "inventory");
        if (client.world.getTimeOfDay() % 24000L == 13000L) trigger(client, "night");
        previouslyLowHealth = lowHealth;
        previouslyLowHunger = lowHunger;
        previouslyInScreen = inScreen;

        if (cooldown > 0) cooldown--;
        if (activeTicks > 0) activeTicks--;
        if (fakePlayerId != -1 && ++fakePlayerAge >= VIRTUAL_PLAYER_LIFETIME) {
            Entity fakePlayer = client.world.getEntityById(fakePlayerId);
            if (fakePlayer != null) fakePlayer.discard();
            fakePlayerId = -1;
        }

        if (--ticksUntilAd <= 0) {
            ticksUntilAd = 20 * 75;
            showAd(client, "timer");
        }
        handleAdClick(client);
    }

    private static void trigger(MinecraftClient client, String trigger) {
        if (cooldown == 0) showAd(client, trigger);
    }

    private static void showAd(MinecraftClient client, String trigger) {
        if (client.player == null || client.world == null || activeTicks > 0) return;
        currentType = (currentType + 1 + client.world.random.nextInt(TYPES.length - 1)) % TYPES.length;
        activeTicks = AD_DURATION_TICKS;
        cooldown = TRIGGER_COOLDOWN_TICKS;
        clickCount = 0;
        client.player.sendMessage(Text.translatable("adwarecraft.extra.trigger." + trigger), true);

        // Reuse a built-in music-disc sound: no downloaded audio or extra resource files are needed.
        client.world.playSound(client.player, client.player.getX(), client.player.getY(), client.player.getZ(),
                SoundEvents.MUSIC_DISC_CAT, SoundCategory.RECORDS, 0.65F, 1.0F);
        spawnVirtualAdPlayer(client);
    }

    private static void spawnVirtualAdPlayer(MinecraftClient client) {
        Vec3d front = client.player.getRotationVec(1.0F).multiply(2.5);
        OtherClientPlayerEntity player = new OtherClientPlayerEntity(client.world,
                new GameProfile(UUID.randomUUID(), "Ad_" + (1000 + client.world.random.nextInt(9000))));
        player.refreshPositionAndAngles(client.player.getX() + front.x, client.player.getY(),
                client.player.getZ() + front.z, client.player.getYaw() + 180.0F, 0.0F);
        client.world.addEntity(player.getId(), player);
        fakePlayerId = player.getId();
        fakePlayerAge = 0;
    }

    private static void handleAdClick(MinecraftClient client) {
        Window window = client.getWindow();
        boolean leftMouse = GLFW.glfwGetMouseButton(window.getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT)
                == GLFW.GLFW_PRESS;
        if (activeTicks > 0 && leftMouse && !previousLeftMouse) {
            double[] cursorX = new double[1];
            double[] cursorY = new double[1];
            GLFW.glfwGetCursorPos(window.getHandle(), cursorX, cursorY);
            int x = (int) (cursorX[0] * window.getScaledWidth() / window.getWidth());
            int y = (int) (cursorY[0] * window.getScaledHeight() / window.getHeight());
            int left = Math.max(8, (window.getScaledWidth() - 310) / 2);
            int top = Math.max(8, window.getScaledHeight() / 9);
            if (x >= left && x <= left + Math.min(310, window.getScaledWidth() - 16)
                    && y >= top && y <= top + 54) {
                clickCount++;
                client.player.sendMessage(Text.translatable("adwarecraft.extra.click", clickCount), true);
                spawnClickFeedback(client);
            }
        }
        previousLeftMouse = leftMouse;
    }

    private static void spawnClickFeedback(MinecraftClient client) {
        EntityType<? extends HostileEntity> type = client.world.random.nextBoolean()
                ? EntityType.ZOMBIE : EntityType.CREEPER;
        HostileEntity mob = type.create(client.world);
        if (mob == null) return;
        Vec3d front = client.player.getRotationVec(1.0F).multiply(2.0);
        mob.refreshPositionAndAngles(client.player.getX() + front.x, client.player.getY(),
                client.player.getZ() + front.z, client.world.random.nextFloat() * 360.0F, 0.0F);
        client.world.addEntity(mob.getId(), mob);
    }

    private static void renderAd(net.minecraft.client.gui.DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (activeTicks <= 0 || client.player == null || client.options.hudHidden || client.currentScreen != null) return;
        int width = context.getScaledWindowWidth();
        int left = Math.max(8, (width - 310) / 2);
        int top = Math.max(8, context.getScaledWindowHeight() / 9);
        int boxWidth = Math.min(310, width - 16);
        context.fill(left, top, left + boxWidth, top + 54, 0xF0181420);
        context.fill(left, top, left + boxWidth, top + 3, 0xFFFF58A8);
        context.drawTextWithShadow(client.textRenderer,
                Text.translatable("adwarecraft.extra.type." + TYPES[currentType]), left + 10, top + 9, 0xFFFFD35A);
        context.drawTextWithShadow(client.textRenderer,
                Text.translatable("adwarecraft.extra.copy." + TYPES[currentType]), left + 10, top + 25, 0xFFFFFFFF);
        context.drawTextWithShadow(client.textRenderer,
                Text.translatable("adwarecraft.extra.click_hint", clickCount), left + 10, top + 40, 0xFFBDB7C7);
    }
}
