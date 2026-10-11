# Combined Mod Project

This repository is a single NeoForge mod project that contains the core Contract NPC features and the imported villager NPC framework under the `net.bayruby.contractnpc.villager` package.

## Structure

- `src/main/java/net/bayruby/contractnpc/` — main Contract NPC registrations, items, blocks, menus, and gameplay systems
- `src/main/java/net/bayruby/contractnpc/villager/` — villager NPC logic, entities, NPC definitions, trades, and contracts
- `src/main/resources/` — shared resources and generated data
- `src/main/templates/META-INF/neoforge.mods.toml` — mod metadata for the combined project

## Notes

- The villager logic is integrated into the main mod registration flow instead of being loaded as a separate NeoForge mod.
- The code remains organized under the `villager` package for maintainability without creating a duplicate mod entrypoint.
- This keeps the mod loaded as a single combined project while retaining the original villager systems.
