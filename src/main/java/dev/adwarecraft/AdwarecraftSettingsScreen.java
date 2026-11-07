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
        int center = width / 2;
        int buttonWidth = 250;
        int left = center - buttonWidth / 2;
        int y = height / 4;

        addDrawableChild(ButtonWidget.builder(toggleText("enabled"), button -> {
            AdwarecraftConfig.enabled = !AdwarecraftConfig.enabled;
            if (!AdwarecraftConfig.enabled) AdwarecraftClientSettingsBridge.disableActiveAd();
            saveAndRefresh();
        }).dimensions(left, y, buttonWidth, 20).build());
        y += 26;
        addDrawableChild(ButtonWidget.builder(toggleText("gameplay"), button -> {
            AdwarecraftConfig.gameplayAds = !AdwarecraftConfig.gameplayAds;
            saveAndRefresh();
        }).dimensions(left, y, buttonWidth, 20).build());
        y += 26;
        addDrawableChild(ButtonWidget.builder(toggleText("screen"), button -> {
            AdwarecraftConfig.screenAds = !AdwarecraftConfig.screenAds;
            saveAndRefresh();
        }).dimensions(left, y, buttonWidth, 20).build());
        y += 26;
        addDrawableChild(ButtonWidget.builder(intervalText(), button -> {
            AdwarecraftConfig.intervalSeconds = switch (AdwarecraftConfig.intervalSeconds) {
                case 15 -> 30;
                case 30 -> 45;
                case 45 -> 90;
                default -> 15;
            };
            saveAndRefresh();
        }).dimensions(left, y, buttonWidth, 20).build());
        y += 26;
        addDrawableChild(ButtonWidget.builder(durationText(), button -> {
            AdwarecraftConfig.durationSeconds = switch (AdwarecraftConfig.durationSeconds) {
                case 5 -> 10;
                case 10 -> 15;
                default -> 5;
            };
            saveAndRefresh();
        }).dimensions(left, y, buttonWidth, 20).build());
        y += 32;
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> close())
                .dimensions(left, y, buttonWidth, 20).build());
    }

    private Text toggleText(String setting) {
        boolean value = switch (setting) {
            case "enabled" -> AdwarecraftConfig.enabled;
            case "gameplay" -> AdwarecraftConfig.gameplayAds;
            default -> AdwarecraftConfig.screenAds;
        };
        return Text.translatable("adwarecraft.settings." + setting, Text.translatable(value
                ? "adwarecraft.settings.on" : "adwarecraft.settings.off"));
    }

    private Text intervalText() {
        return Text.translatable("adwarecraft.settings.interval", AdwarecraftConfig.intervalSeconds);
    }

    private Text durationText() {
        return Text.translatable("adwarecraft.settings.duration", AdwarecraftConfig.durationSeconds);
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
                Text.translatable("adwarecraft.settings.hint"), width / 2, height / 4 - 20, 0xFFBDB7C7);
    }
}
