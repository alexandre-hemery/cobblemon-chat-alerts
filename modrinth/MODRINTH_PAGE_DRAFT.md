# Cobblemon Chat Alerts

## Project Title

Cobblemon Chat Alerts

## Summary

Configurable chat alerts for rare Cobblemon spawns, evolutions, Pokedex entries, and IV-based finds.

## Description

Cobblemon Chat Alerts is a small Fabric companion mod for Cobblemon. It adds configurable chat notifications for important Pokemon events, so players can notice rare spawns, Pokedex progress, evolutions, and level milestones without adding a heavy UI overlay.

The mod is designed to stay lightweight: alerts are sent through Minecraft chat, and the configuration can be edited in game through Mod Menu when Mod Menu and Cloth Config are installed.

## Features

- Chat alerts for rare wild Pokemon spawns
- Chat alerts for rare Pokemon captures and defeats
- Alerts when a Pokemon can evolve or has evolved
- Alerts for new Pokedex entries
- Alerts when a Pokemon reaches level 100
- Separate rarity filters for shiny, legendary, mythical, restricted, sub-legendary, Ultra Beast, perfect IVs, and high IVs
- Configurable rare-spawn range: block radius, chunk radius, or whole dimension
- Optional clickable coordinates, biome, level, and IV details
- English and French localization
- JSON config file plus Mod Menu support through Cloth Config

## Requirements

- Minecraft 1.21.1
- Fabric Loader
- Fabric API
- Cobblemon 1.7.3 for Fabric
- Cloth Config

Mod Menu is optional, but recommended for in-game configuration.

Set Fabric API, Cobblemon, and Cloth Config as required dependencies on Modrinth.

## Compatibility Notes

- Alerts are chat-only for now.
- Whole-world rare-spawn alerts currently mean all players in the same dimension as the spawn.
- IV-based rarity filters are disabled by default.
- Pokemon names use Cobblemon's translated components, so they follow the player's Minecraft language when translations exist.

## Credits

Cobblemon: https://cobblemon.com/

This project is an unofficial companion mod and is not affiliated with or endorsed by the Cobblemon developers, Mojang, Microsoft, Nintendo, Creatures Inc., or GAME FREAK.

## Suggested Tags

Fabric, Utility, Cobblemon, Pokemon, Chat, Alerts, Mod Menu

## Links

- Source code: https://github.com/alexandre-hemery/cobblemon-chat-alerts
- Issue tracker: https://github.com/alexandre-hemery/cobblemon-chat-alerts/issues
- Release files: https://github.com/alexandre-hemery/cobblemon-chat-alerts/releases/tag/v0.1.2

## License

MIT

## Gallery Assets

Suggested gallery order:

- `docs/images/chat-alerts.png` - `Chat alerts in game`
- `docs/images/config-alerts.png` - `Alerts configuration`
- `docs/images/config-rarity-filters.png` - `Rarity and IV filters`
- `docs/images/config-rare-spawns.png` - `Rare spawn range and details`

## Recommended Modrinth Metadata

- Project title: `Cobblemon Chat Alerts`
- Slug: `cobblemon-chat-alerts`
- Short description: `Configurable chat alerts for rare Cobblemon spawns, evolutions, Pokedex entries, and IV-based finds.`
- Loaders: `Fabric`
- Game versions: `1.21.1`
- Categories: `Utility`, `Adventure`
- Required dependencies: `Fabric API`, `Cobblemon`, `Cloth Config`
- Optional dependency: `Mod Menu`
- Client side: `Required`
- Server side: `Required`
- License: `MIT`

## Publishing Notes

Upload the main jar as the release file:

- `cobblemon-chat-alerts-0.1.2+mc1.21.1-fabric.jar`

Suggested version metadata:

- Version name: `0.1.2`
- Version number: `0.1.2`
- Release channel: `Release`
- Game version: `1.21.1`
- Loader: `Fabric`

Suggested changelog:

- Added separate rarity filters for shiny, legendary, mythical, restricted, sub-legendary, and Ultra Beast Pokemon.
- Added optional IV rarity filters for perfect IVs and high IVs, with a configurable threshold defaulting to 90%.
- Added Mod Menu entries for rarity filters.
- Updated French and English translations.
- Rare spawn alerts now ignore Pokemon already owned by a player.
- Added the final mod icon.
- Switched the project license to MIT.
