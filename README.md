# QuickTrigger

A Fabric mod that adds quick teleportation buttons directly into the Minecraft inventory, designed to work with the **VanillaTweaks Home & Spawn datapacks**.

---

## Features

### Spawn Button
A compass button displayed above the inventory to instantly send `/trigger spawn`.

### Home Buttons
Up to **9 homes** accessible with a single click, displayed above the inventory.

- **Unlocked slots** — colored bed (color configurable per player via Mod Menu)
- **Locked slots** — gray bed with a tooltip message configurable by the server admin
- **No server mod** — only Home #1 is shown (no gray beds)

### Configuration (Mod Menu)
If [Mod Menu](https://modrinth.com/mod/modmenu) is installed, a **Config** button appears on QuickTrigger's entry. It opens a screen where each player can:

- Choose the **bed color** for each home slot (16 vanilla colors, displayed in the player's language)
- Set a **custom name** for each home slot (up to 24 characters) — shown on hover instead of "Home #N"

Names and colors are saved client-side per server/world in `config/quicktrigger.json`.

---

## Installation

### Requirements
- Minecraft **1.21.11**
- [Fabric Loader](https://fabricmc.net/) **≥ 0.18.0**
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [VanillaTweaks](https://vanillatweaks.net/) — Homes & Spawn datapacks (server-side)
- [Mod Menu](https://modrinth.com/mod/modmenu) *(optional — enables the config screen)*

### Client
Drop `quicktrigger-x.x.x.jar` into your `mods/` folder.

> Players without the mod can still play normally — they just won't see the buttons.

### Server
Drop the **same `.jar`** into the Fabric server `mods/` folder.

> The server mod is optional. Without it, clients fall back to showing only Home #1.

---

## Server Configuration

On first launch, the file `config/quicktrigger-server.json` is generated automatically:

```json
{
  "maxHomes": 1,
  "lockMessages": [
    "Unlock slot #2",
    "Unlock slot #3",
    "Unlock slot #4",
    "Unlock slot #5",
    "Unlock slot #6",
    "Unlock slot #7",
    "Unlock slot #8",
    "Unlock slot #9"
  ]
}
```

| Field | Description |
|-------|-------------|
| `maxHomes` | Total number of home slots shown to all players (1–9) |
| `lockMessages[0]` | Tooltip on the gray bed for slot #2 |
| `lockMessages[1]` | Tooltip on the gray bed for slot #3 |
| … | … |
| `lockMessages[7]` | Tooltip on the gray bed for slot #9 |

Changes require a **server restart** to take effect.

### Scoreboard `homes.limit`
The server reads each player's personal limit from the scoreboard on join and sends it to the client via a custom S2C packet (`quicktrigger:data`).

```
/scoreboard objectives add homes.limit dummy
/scoreboard players set <player> homes.limit <value>
```

- Accepted values: `1` to `maxHomes`
- Slots `1` to `playerLimit` → unlocked (colored bed)
- Slots `playerLimit + 1` to `maxHomes` → locked (gray bed with tooltip)
- Players with **no score set** default to `playerLimit = 1` (slot 1 unlocked, rest locked)

---

## Client Configuration

Each player can configure colors and custom names per home slot via **Mod Menu → QuickTrigger → Config**.

- Config saved in `.minecraft/config/quicktrigger.json`
- 16 available colors (all vanilla Minecraft bed colors)
- Custom name up to **24 characters** — replaces "Home #N" in the hover tooltip
- Names and colors are stored **per server/world** — each server has its own independent set
- The config screen shows exactly as many rows as `maxHomes` from the server (or 1 if offline)

The JSON structure for custom names:

```json
{
  "bedColors": ["BLUE", "GREEN", "..."],
  "serverBedNames": {
    "play.monserveur.net": ["Maison", "Farm", "", "", "", "", "", "", ""],
    "local:MaSurvie":      ["Base principale", "", "", "", "", "", "", "", ""]
  }
}
```

---

## Behavior Summary

| Situation | Inventory display |
|-----------|-------------------|
| No server mod | Spawn + Home #1 only |
| Server mod, `playerLimit = 2`, `maxHomes = 4` | Spawn + 2 colored homes + 2 gray beds |
| Server mod, `playerLimit = maxHomes` | Spawn + all homes colored, no gray beds |

---

## Building

```bash
./gradlew build
```

Output: `build/libs/quicktrigger-x.x.x.jar`

Requires Java 21.

---

## Compatibility

| Environment | Supported |
|-------------|-----------|
| Client only | ✅ (1 home slot) |
| Client + server mod | ✅ (full feature set) |
| Client without server mod | ✅ (graceful fallback) |
| Vanilla client on modded server | ✅ (mod safely ignored) |

---

## Project Structure

```
src/main/java/com/quicktrigger/
├── QuickTrigger.java             # Server entrypoint — loads server config, sends payload on JOIN
├── QuickTriggerClient.java       # Client entrypoint — renders inventory buttons
├── QuickTriggerPayloads.java     # S2C packet: playerLimit + maxHomes + lockMessages (JSON)
├── QuickTriggerConfig.java       # Client config — bed colors per slot (quicktrigger.json)
├── QuickTriggerServerConfig.java # Server config — maxHomes + lockMessages (quicktrigger-server.json)
├── ConfigScreen.java             # Mod Menu config screen — CycleButton per home slot
└── ModMenuIntegration.java       # Mod Menu bridge — registers ConfigScreen as config factory
```
