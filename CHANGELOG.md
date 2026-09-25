# Changelog

## [1.3.0] - 2026-09-25

### Changed
- Updated for Minecraft 26.3 (Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, NeoForge 26.3.0.16-beta, Mod Menu 21.0.0).
- New mod icon.
- Internal code cleanup and light restructuring to match my other Minecraft mods. No gameplay change.

---

## [1.2.0] - 2026-06-17

### Added
- **NeoForge support**: the project is now split into `common`, `fabric` and `neoforge` modules, and each loader gets its own jar.

### Changed
- Updated for Minecraft 26.2 (Fabric Loader 0.19.3, Fabric API 0.152.1+26.2, NeoForge 26.2.0.1-beta, Mod Menu 20.0.0-beta.2).

### Fixed
- Mod icon not showing in Mod Menu (the file was a JPEG with a `.png` extension).

---

## [1.1.2] - 2026-04-26

### Changed
- Updated mod author name in metadata.

---

## [1.1.1] - 2026-04-26

### Fixed
- Custom home names were lost after reconnecting to a server. The server key was resolved using `client.getCurrentServer()`, which can return `null` on the network thread when the JOIN event fires (race condition), causing names to be stored under the wrong key. The fix uses `handler.getServerData()`, which is guaranteed to be populated at JOIN time.

---

## [1.1.0] - 2026-04-26

### Added
- **Custom home names** — each player can assign a custom name (up to 24 characters) to each home slot via Mod Menu → QuickTrigger → Config. The name replaces "Home #N" in the hover tooltip.
- **Per-server/world name isolation** — names are stored separately per server IP or singleplayer world, just like bed colors.

### Fixed
- Bed colors were never reloaded from `quicktrigger.json` on game restart due to an incorrect length check (`== 4` instead of slot-by-slot copy). Colors and names now persist correctly across restarts.

### Changed
- Mod Menu entry now links to the correct website (Modrinth) and issue tracker (GitHub).
- Version is now injected automatically from `gradle.properties` via `processResources`.

---

## [1.0.1] - 2026-04-24

### Fixed
- Avoid NullPointerException in `ConfigScreen` when opened from the main menu (no active server connection).

---

## [1.0.0] - 2026-04-24

### Added
- Initial release.
- Spawn button (compass) and Home button(s) displayed above the player inventory.
- Up to 9 home slots with configurable bed colors per slot (Mod Menu).
- Server-side control of `maxHomes` and per-player `playerLimit` via scoreboard `homes.limit`.
- Locked slots shown as gray beds with configurable tooltip messages.
- Graceful fallback to Spawn + Home #1 only when no server mod is present.
