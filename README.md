# Adwarecraft

A small, client-side Fabric mod for Minecraft Java Edition 1.20.1, satirising apps that force ads into every spare moment.

## What it does

- A random sponsored interruption about every **45 seconds** of active gameplay.
- Extra ads when opening an inventory/container or the pause menu, with an 18-second cooldown to avoid a constant re-trigger loop.
- 20+ rotating absurd ad messages and five fake brands.
- Four obnoxious skip styles: a five-second countdown and fake skip, a fake instant skip, a hidden-skip message, and no skip button. The ad always auto-closes after 10 seconds.
- A tiny animated pixel-art emerald promo rendered in-game (bobbing and sparkling), so there are no external images, video files, or media decoders.

**A real escape is always available:** press **X** to dismiss the ad immediately, even when the mock ad claims there is no skip button. These are visual overlays only: they do not capture clicks, pause gameplay, block inventory controls, or connect to an ad service. Use the Controls menu to rebind the dismiss key.

## Build

- Java 17
- Gradle 8.6 or newer

Run `gradle build`; the mod jar will be in `build/libs/`.

For development, import this project as a Gradle project and run the generated Minecraft client configuration.
