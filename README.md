# Combined Mod Project

This repository contains two NeoForge mods shipped in a single project:

- Contract NPC (`contractnpc`) — the primary mod
- Villager (`villager`) — the secondary imported mod that lives under the `net.bayruby.contractnpc.villager` package

## Structure

- `src/main/java/net/bayruby/contractnpc/` — main Contract NPC registrations, items, blocks, menus, and gameplay systems
- `src/main/java/net/bayruby/contractnpc/villager/` — secondary Villager mod logic, entities, NPC system, and trade handling
- `src/main/resources/` — shared resources and generated data
- `src/main/templates/META-INF/neoforge.mods.toml` — combined mod metadata for both mods in the same jar

## Notes

- Both mods are intentionally kept in the same repository for easier maintenance.
- Each mod still has its own `@Mod` entry and mod id, so they can be loaded independently inside the same build.
- The `villager` package is treated as the imported secondary mod and is not a separate project build.
