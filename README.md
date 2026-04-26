# QuickTrigger

Adds a **Spawn** button and up to **9 Home** buttons directly in your Minecraft inventory.  
Works with the [VanillaTweaks](https://vanillatweaks.net/) Homes & Spawn datapacks.

---

## What it does

When you open your inventory, you'll see buttons above it:

- **Spawn** — teleports you to spawn instantly
- **Home #1 to #9** — teleports you to the corresponding home

No commands to type, no chat to open. One click and you're there.

---

## Requirements

- Minecraft **1.21.11** with [Fabric Loader](https://fabricmc.net/) and [Fabric API](https://modrinth.com/mod/fabric-api)
- The **VanillaTweaks Homes & Spawn datapacks** installed on the server
- *(Optional)* [Mod Menu](https://modrinth.com/mod/modmenu) — enables the settings screen

---

## Installation

Drop `quicktrigger-x.x.x.jar` into your `mods/` folder. That's it.

> Players without the mod can still join and play normally — they just won't see the buttons.

---

## Customization

If you have [Mod Menu](https://modrinth.com/mod/modmenu) installed, open **Mods → QuickTrigger → Config** to:

- Change the **bed color** of each home button (all 16 vanilla colors)
- Give each home a **custom name** — shown when you hover over the button

Colors and names are saved per server and per singleplayer world, so each save has its own setup.

---

## For server admins

Install the same `.jar` on your Fabric server to unlock the full feature set:

- Set a **maximum number of home slots** visible to all players (up to 9)
- Control **how many slots each player can use** via a scoreboard
- Customize the **tooltip** on locked slots

The config file (`config/quicktrigger-server.json`) is generated automatically on first launch.  
See [TECHNICAL.md](TECHNICAL.md) for the full setup details.

---

## Compatibility

| Situation | Works? |
|-----------|--------|
| Client only (no server mod) | Yes — Spawn + 1 Home |
| Client + server mod | Yes — full feature set |
| Vanilla client on a modded server | Yes — mod is ignored |
