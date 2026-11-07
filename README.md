# Adwarecraft

A client-side Fabric mod for Minecraft Java Edition 1.20.1, satirising apps that force ads into every spare moment. The current feature set carries forward the v1.0.1 release's ad formats and configurable settings.

## Ads

- 30 rotating promotions in six visual ad formats, including Chinese-language red-envelope, check-in, fake-system-alert, countdown, notification, and short-video parodies.
- Gameplay and pause/inventory triggers, plus periodic ads printed as normal in-game chat messages.
- Up to three overlapping decorative ad windows and an optional fake message-box popup.
- The fake skip controls and OK/close-looking shapes are visual only. Press **X** to dismiss the active ads immediately; they never consume clicks or gameplay input.
- Animated pixel art is rendered in-game. No media downloads, tracking, or external ad services.

## Settings

Press **K** by default to open settings (rebind it under Controls). Toggle the mod, gameplay ads, pause/inventory ads, chat ads, and message-box ads. Cycle ad windows (1–3), gameplay interval (15/30/45/90/180 seconds), ad duration (5/10/15/20 seconds), and chat interval (30/60/90/180 seconds). Settings persist in `config/adwarecraft.properties`.

## Build

- Java 17
- Gradle 8.6 or newer

Run `gradle --no-daemon clean build`; the mod JAR will be in `build/libs/`.
