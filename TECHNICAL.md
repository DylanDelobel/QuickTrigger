# QuickTrigger — Technical Reference

## Architecture

### Communication flow
```
Server JOIN event
  → reads scoreboard objective "homes.limit" (per-player value)
  → reads QuickTriggerServerConfig (maxHomes, lockMessages)
  → sends HomeLimitPayload (playerLimit, maxHomes, lockMessagesJson) S2C

Client receives payload
  → sets serverHasMod = true
  → stores playerLimit, maxHomes, lockMessages[]
  → on disconnect: resets all to defaults (serverHasMod=false, maxHomes=1)

Inventory screen opened
  → if !serverHasMod: show only Spawn + Home #1
  → if serverHasMod: show Spawn + playerLimit colored homes + (maxHomes - playerLimit) gray beds
```

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

## Config files

| File | Side | Contents |
|------|------|----------|
| `config/quicktrigger.json` | Client | Bed color per slot (9 entries, enum names) |
| `config/quicktrigger-server.json` | Server | maxHomes (1–9) + lockMessages[8] (slots 2–9) |

### Server config structure

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

### Client config structure

```json
{
  "bedColors": ["BLUE", "GREEN", "..."],
  "serverBedNames": {
    "play.monserveur.net": ["Maison", "Farm", "", "", "", "", "", "", ""],
    "local:MaSurvie":      ["Base principale", "", "", "", "", "", "", "", ""]
  }
}
```

## Scoreboard `homes.limit`

The server reads each player's personal home limit from the scoreboard on join and sends it to the client via a custom S2C packet (`quicktrigger:data`).

```
/scoreboard objectives add homes.limit dummy
/scoreboard players set <player> homes.limit <value>
```

- Accepted values: `1` to `maxHomes`
- Slots `1` to `playerLimit` → unlocked (colored bed)
- Slots `playerLimit + 1` to `maxHomes` → locked (gray bed with tooltip)
- Players with **no score set** default to `playerLimit = 1` (slot 1 unlocked, rest locked)

## Key implementation rules

- `maxHomes` range: 1–9 (enforced in `QuickTriggerServerConfig.load()`)
- `lockMessages` array: size 8, index 0 = slot 2, index 7 = slot 9 (slot 1 is always unlocked)
- `bedColors` array: size 9, index 0 = slot 1, stored as `BedColor` enum names (e.g. `"BLUE"`)
- `serverHasMod` and `maxHomes` in `QuickTriggerClient` are package-visible (`static volatile`) for use by `ConfigScreen`
- `HomeLimitPayload` encodes lockMessages as a Gson JSON array string (`lockMessagesJson`)
- `ConfigScreen.rowCount` = `QuickTriggerClient.maxHomes` (1 when offline/no server mod)
- Bed color names use `Component.translatable(color.item.getDescriptionId())` — auto-localized

## Known pitfalls

- `renderBackground()` must NOT be called manually in `Screen.render()` — MC 1.21.x calls it internally via `super.render()`, calling it twice crashes with `IllegalStateException: Can only blur once per frame`
- `CycleButton.builder()` in this MC version requires the initial value as second argument: `builder(valueToComponent, initialValue)`
- `lockMessages` indexing: `msgs[index - 1]` in client (index starts at 1 for slot 2)

## Mod Menu integration

- `ModMenuIntegration implements ModMenuApi` — registered under entrypoint key `"modmenu"`
- Mod Menu is `modCompileOnly` (optional at runtime) — declared in `suggests` in fabric.mod.json
- Version pinned in `gradle.properties` as `modmenu_version`

## Building

```bash
./gradlew build
```

Output: `build/libs/quicktrigger-x.x.x.jar`

Requires Java 21.
