# Adwarecraft

A client-side Fabric mod for Minecraft Java Edition 1.20.1, satirising apps that force ads into every spare moment.

## Ads

- Six visual ad formats with 30 rotating messages, including Chinese-language red-envelope, daily check-in, fake system alert, countdown, notification, and short-video parodies.
- Ads can appear during gameplay and when opening pause/inventory screens.
- Fake skip-button styles remain visual jokes: press **X** to dismiss any ad immediately.
- Small animated pixel art is drawn in-game; the decorative QR-like pattern is not scannable and no external media or network services are used.

## Settings

Press **K** by default to open the settings screen (the key can be rebound in Controls). Toggle the mod, gameplay ads, and pause/inventory ads; cycle gameplay interval (15/30/45/90 seconds) and ad duration (5/10/15 seconds). Changes are saved automatically to `config/adwarecraft.properties`.

## Build

- Java 17
- Gradle 8.6 or newer

Run `gradle --no-daemon clean build`; the mod JAR will be in `build/libs/`.
