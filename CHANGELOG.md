# Changelog

## 0.1.3 - 2026-07-14

- Fixed Pokedex alerts so only species-level progress upgrades generate a new-entry message.
- Prevented cancelled rare spawns from being announced by verifying the entity after the spawn tick.
- Removed the non-functional sub-legendary filter, which is not backed by a Cobblemon 1.7.3 label.
- Updated rarity checks to use Cobblemon's form-aware Pokemon helpers.
- Renamed the whole-world audience option to whole dimension to match its actual scope.
- Clarified that multiplayer installations require the mod on both the client and the server.
- Clarified singleplayer Mod Menu configuration versus dedicated-server configuration.
- Embedded the MIT license in the binary and sources jars.
- Added automated tests for rarity filters, IV thresholds, and Pokedex progress transitions.
- Fixed the Gradle resource configuration warning that would become incompatible with Gradle 10.

## 0.1.2 - 2026-07-08

- Added separate rarity filters for shiny, legendary, mythical, restricted, sub-legendary, and Ultra Beast Pokemon.
- Added optional IV rarity filters for perfect IVs and high IVs, with a configurable threshold defaulting to 90%.
- Added Mod Menu entries for rarity filters.
- Updated French and English translations.
- Rare spawn alerts now ignore Pokemon already owned by a player.
- Added the final mod icon.
- Switched the project license to MIT.

## 0.1.1 - 2026-07-07

- Added a JSON config file.
- Added a Mod Menu/Cloth Config configuration screen.
- Added rare spawn radius settings: block radius, chunk radius, or whole dimension.
- Added clickable coordinates that prepare a `/tp` command.
- Added optional coordinates, biome, level, and IV details.
- Added the first generated icon candidate.

## 0.1.0 - 2026-07-07

- Added local chat alerts for rare Pokemon spawns.
- Added rare Pokemon capture and defeat alerts.
- Added level 100, evolution-ready, evolution-complete, and Pokedex-entry alerts.
- Added English and French localization.
