# Cobblemon Chat Alerts

Fabric 1.21.1 companion mod for Cobblemon that adds configurable chat notifications for important Pokemon events.

This is an unofficial companion mod and is not affiliated with Cobblemon's developers.

## Status

- Latest release: `0.1.3+mc1.21.1-fabric`
- GitHub release: [v0.1.3](https://github.com/alexandre-hemery/cobblemon-chat-alerts/releases/tag/v0.1.3)
- Modrinth: corrected 0.1.3 resubmission pending.
- Issue tracker: [GitHub Issues](https://github.com/alexandre-hemery/cobblemon-chat-alerts/issues)

## Features

- Rare wild Pokemon spawn alerts.
- Rare Pokemon capture and defeat alerts.
- Level 100, evolution-ready, evolution-complete, and Pokedex-entry alerts.
- Separate rarity filters for shiny, legendary, mythical, restricted, Ultra Beast, perfect IVs, and high IVs.
- Configurable rare-spawn range: block radius, chunk radius, or whole dimension.
- Optional clickable coordinates, biome, level, and IV details.
- English and French localization.
- JSON config file plus Mod Menu support through Cloth Config.

## Screenshots

These screenshots were taken with 0.1.2. The non-functional sub-legendary filter visible in the rarity screen has been removed in 0.1.3.

<table>
  <tr>
    <td width="50%">
      <img src="docs/images/chat-alerts.png" alt="Chat alerts in game">
      <br>
      <sub>Chat alerts in game.</sub>
    </td>
    <td width="50%">
      <img src="docs/images/config-alerts.png" alt="Alerts configuration">
      <br>
      <sub>Alert toggles in Mod Menu.</sub>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <img src="docs/images/config-rarity-filters.png" alt="Rarity filters configuration">
      <br>
      <sub>Rarity and IV filters.</sub>
    </td>
    <td width="50%">
      <img src="docs/images/config-rare-spawns.png" alt="Rare spawns configuration">
      <br>
      <sub>Range and spawn detail settings.</sub>
    </td>
  </tr>
</table>

## Requirements

- Minecraft 1.21.1
- Fabric Loader
- Fabric API
- Cobblemon 1.7.3 for Fabric
- Cloth Config
- Mod Menu is optional, but recommended for in-game configuration.

For multiplayer, Cobblemon Chat Alerts is required on both the client and the server. The server detects Cobblemon events, while the client provides the translated alert resources.

## Installation

Download the Fabric jar from the latest GitHub release, then place it in the `mods` folder with the required dependencies.

For multiplayer, install the same mod version on every client and on the server. For singleplayer, installing it in the client instance also makes it available to Minecraft's integrated server.

## Configuration

The config file is created at:

```text
config/cobblemon-chat-alerts.json
```

In singleplayer, Mod Menu edits the configuration used by the integrated server.

On a dedicated server, the authoritative configuration file is the one in the server's own `config` directory. Client-side Mod Menu changes are not synchronized to a remote server. Edit the server file directly and restart the server to apply manual changes.

## Build

```bash
./gradlew build
```

The built jar is created in:

```text
build/libs/
```

## Notes

- Alerts are chat-only for now.
- Whole-dimension rare-spawn alerts notify all players in the same dimension as the spawn.
- IV-based rarity filters are disabled by default.
- Pokemon names use Cobblemon's translated components, so they follow the player's Minecraft language when translations exist.

## License

This project is licensed under the MIT License.
