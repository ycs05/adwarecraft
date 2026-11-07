package dev.adwarecraft;

/** Small bridge to keep ad lifecycle private to the client initializer. */
final class AdwarecraftClientSettingsBridge {
    private AdwarecraftClientSettingsBridge() { }

    static void disableActiveAd() {
        AdwarecraftClient.disableForSettings();
    }
}
