# QuickTrigger

A Fabric mod that adds quick teleportation buttons directly into the Minecraft inventory, designed to work with the **VanillaTweaks Home & Spawn datapacks**.

---

## Features

### Spawn Button
A compass button displayed above the inventory to instantly teleport to the server spawn.

### Home Buttons
Up to **4 homes** accessible with a single click, color-coded by rank:

| # | Color | Required Role | Command |
|---|-------|---------------|---------|
| Home #1 | 🔵 Blue | All players | `/trigger home` |
| Home #2 | 🟢 Green | Miner | `/trigger home set 2` |
| Home #3 | 🟠 Orange | Architect | `/trigger home set 3` |
| Home #4 | 🟣 Purple | Dragon | `/trigger home set 4` |

Locked homes are displayed as a **gray bed** with a tooltip indicating the Discord role required to unlock them.

---

## Installation

### Requirements
- Minecraft **1.21.11**
- [Fabric Loader](https://fabricmc.net/) **≥ 0.18.0**
- [Fabric API](https://modrinth.com/mod/fabric-api) **0.139.x+1.21.11**
- [VanillaTweaks](https://vanillatweaks.net/) — Homes & Spawn datapacks

### Client side
Copy `quicktrigger-1.0.0.jar` into your Minecraft `mods/` folder.

### Server side
Copy the **same `.jar`** into the Fabric server `mods/` folder.

> The mod is **optional on the client** — players without the mod can still play normally, they just won't see the buttons in their inventory.

---

## Server Configuration

### Scoreboard `homes.limit`
The server automatically sends each player's home limit on join via a custom channel (`quicktrigger:data`).

Create the objective and set the limit per player:
```
/scoreboard objectives add homes.limit dummy
/scoreboard players set <player> homes.limit <value>
```

Accepted values: `1` (default), `2`, `3`, `4`.

### Required Triggers
The mod sends `/trigger` commands — the VanillaTweaks datapacks handle the actual teleportation logic.

---

## Building

### Requirements
- Java **21**
- Gradle (included via wrapper)

```bash
./gradlew build
```

The final `.jar` is located at `build/libs/quicktrigger-1.0.0.jar`.

---

## Compatibility

| Environment | Supported |
|-------------|-----------|
| Client only (singleplayer) | ✅ |
| Client on Fabric server (with server mod) | ✅ |
| Client on Fabric server (without server mod) | ✅ (1 home slot by default) |
| Vanilla client on Fabric server | ✅ (mod safely ignored) |

---

## Project Structure

```
src/main/java/com/quicktrigger/
├── QuickTrigger.java          # Server entrypoint — sends homes.limit on player JOIN
├── QuickTriggerClient.java    # Client entrypoint — renders the inventory buttons
└── QuickTriggerPayloads.java  # Shared payload for the custom S2C channel
```
