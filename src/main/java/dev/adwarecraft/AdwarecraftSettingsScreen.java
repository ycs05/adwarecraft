package dev.adwarecraft;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** In-game settings screen, opened with K by default. */
public final class AdwarecraftSettingsScreen extends Screen {
    private final Screen parent;

    public AdwarecraftSettingsScreen(Screen parent) {
        super(Text.translatable("adwarecraft.settings.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(310, width - 24);
        int left = (width - buttonWidth) / 2;
        int y = Math.max(48, height / 7);
        int step = 23;

        addToggle(left, y, buttonWidth, "enabled", AdwarecraftConfig.enabled, () -> {
            AdwarecraftConfig.enabled = !AdwarecraftConfig.enabled;
            if (!AdwarecraftConfig.enabled) AdwarecraftClientSettingsBridge.disableActiveAd();
        }); y += step;
        addToggle(left, y, buttonWidth, "gameplay", AdwarecraftConfig.gameplayAds, () -> AdwarecraftConfig.gameplayAds = !AdwarecraftConfig.gameplayAds); y += step;
        addToggle(left, y, buttonWidth, "screen", AdwarecraftConfig.screenAds, () -> AdwarecraftConfig.screenAds = !AdwarecraftConfig.screenAds); y += step;
        addToggle(left, y, buttonWidth, "chat", AdwarecraftConfig.chatAds, () -> AdwarecraftConfig.chatAds = !AdwarecraftConfig.chatAds); y += step;
        addToggle(left, y, buttonWidth, "messagebox", AdwarecraftConfig.messageBoxAds, () -> AdwarecraftConfig.messageBoxAds = !AdwarecraftConfig.messageBoxAds); y += step;

        addDrawableChild(ButtonWidget.builder(Text.translatable("adwarecraft.settings.windows", AdwarecraftConfig.adWindows), button -> {
            AdwarecraftConfig.adWindows = AdwarecraftConfig.adWindows % 3 + 1;
            saveAndRefresh();
        }).dimensions(left, y, buttonWidth, 20).build()); y += step;
        addDrawableChild(ButtonWidget.builder(Text.translatable("adwarecraft.settings.interval", AdwarecraftConfig.intervalSeconds), button -> {
            AdwarecraftConfig.intervalSeconds = nextValue(AdwarecraftConfig.intervalSeconds, new int[]{15, 30, 45, 90, 180});
            saveAndRefresh();
        }).dimensions(left, y, buttonWidth, 20).build()); y += step;
        addDrawableChild(ButtonWidget.builder(Text.translatable("adwarecraft.settings.duration", AdwarecraftConfig.durationSeconds), button -> {
            AdwarecraftConfig.durationSeconds = nextValue(AdwarecraftConfig.durationSeconds, new int[]{5, 10, 15, 20});
            saveAndRefresh();
        }).dimensions(left, y, buttonWidth, 20).build()); y += step;
        addDrawableChild(ButtonWidget.builder(Text.translatable("adwarecraft.settings.chat_interval", AdwarecraftConfig.chatIntervalSeconds), button -> {
            AdwarecraftConfig.chatIntervalSeconds = nextValue(AdwarecraftConfig.chatIntervalSeconds, new int[]{30, 60, 90, 180});
            saveAndRefresh();
        }).dimensions(left, y, buttonWidth, 20).build()); y += step + 3;
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> close())
                .dimensions(left, y, buttonWidth, 20).build());
    }

    private void addToggle(int x, int y, int width, String key, boolean value, Runnable action) {
        addDrawableChild(ButtonWidget.builder(toggleText(key, value), button -> {
            action.run();
            saveAndRefresh();
        }).dimensions(x, y, width, 20).build());
    }

    private Text toggleText(String key, boolean value) {
        return Text.translatable("adwarecraft.settings." + key,
                Text.translatable(value ? "adwarecraft.settings.on" : "adwarecraft.settings.off"));
    }

    private int nextValue(int current, int[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) return values[(i + 1) % values.length];
        }
        return values[0];
    }

    private void saveAndRefresh() {
        AdwarecraftConfig.save();
        clearAndInit();
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("adwarecraft.settings.hint"), width / 2, Math.max(12, height / 7 - 17), 0xFFBDB7C7);
    }
}
