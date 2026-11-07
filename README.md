# Adwarecraft

A client-side Fabric mod for Minecraft Java Edition 1.20.1, satirising apps that force ads into every spare moment.

## Languages

All visible strings are defined in standard Minecraft language JSON files under `src/main/resources/assets/adwarecraft/lang/`. The mod uses Minecraft's `Text.translatable` system, so the selected Minecraft language automatically controls the ad copy, brands, skip-button jokes, and chat messages. English (US), French, Spanish (Spain), German, Japanese, and Simplified Chinese are included; add or edit a locale file to contribute another translation. English US is the fallback locale.

## Ads

- A sponsored interruption about every 45 seconds of active gameplay.
- Extra ads when opening an inventory/container or pause menu, with an 18-second cooldown.
- Rotating ad copy and fake brands, translated using resource keys.
- Four fake skip styles; press **X** at any time to really dismiss the ad. Ads also auto-close after 10 seconds.
- Animated pixel-art emerald promo rendered in-game without external media.

The visual overlay never captures clicks, pauses gameplay, blocks controls, or connects to an ad service. Rebind the dismiss key in Controls.

## Build

- Java 17
- Gradle 8.6 or newer

Run `gradle build`; the mod jar will be in `build/libs/`.
