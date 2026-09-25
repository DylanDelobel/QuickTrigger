# QuickTrigger

QuickTrigger adds **Spawn and Home buttons directly to your inventory**

so you don't have to type `/trigger spawn` or `/trigger home` every time you want to teleport.

It's made to work with the [VanillaTweaks](https://vanillatweaks.net/) Homes and Spawn datapacks.

## Features

Open your inventory and you'll find a **Spawn** button along with up to **9 Home slots**.

Clicking one simply runs the corresponding trigger for you.

Homes can be customized with a name and a bed color, making it easier to remember which home goes where.

The mod works client-side, so you can use it on a compatible server without requiring other players to install anything.

## Installation

Download the `.jar` and put it in your `mods` folder.

QuickTrigger supports **Fabric and NeoForge**.

If you're only using the client mod, you'll have access to Spawn and one Home slot.

Other players don't need QuickTrigger installed.

## Customization

If you have [Mod Menu](https://modrinth.com/mod/modmenu) installed, go to:

**Mods → QuickTrigger → Config**

From there, you can:

- Change the bed color for each home
- Give your homes custom names

These settings are saved separately for each server and singleplayer world.

## Server support

QuickTrigger can optionally be installed on the server as well.

This isn't required, but it gives server admins a few additional options:

- Choose how many Home slots are displayed
- Control how many Home slots each player can use
- Customize the tooltip shown for locked slots

The server config is generated automatically on first launch.

More information about the server-side configuration is available in [TECHNICAL.md](https://github.com/DylanDelobel/QuickTrigger/blob/master/TECHNICAL.md).

## Compatibility

| Setup | Support |
| --- | --- |
| Client only | Spawn + 1 Home |
| Client + server | Full feature set |
| Vanilla client + modded server | Works normally; QuickTrigger features are simply unavailable |
