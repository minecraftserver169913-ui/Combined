package net.bayruby.contractnpc.villager.trade;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class PlayerTradeData extends SavedData {

    private static final String DATA_NAME = "villager_player_trades";

    private final Map<UUID, Set<String>> unlocked = new HashMap<>();

    public PlayerTradeData() {
    }

    public static PlayerTradeData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(
                        new Factory<>(
                                PlayerTradeData::new,
                                PlayerTradeData::load,
                                null
                        ),
                        DATA_NAME
                );
    }

    public boolean hasTrade(UUID playerId, String tradeId) {
        return unlocked
                .getOrDefault(playerId, Set.of())
                .contains(tradeId);
    }

    /**
     * Unlocks a global trade for a player.
     *
     * @return true if the trade was newly unlocked
     */
    public boolean unlock(UUID playerId, String tradeId) {
        boolean added = unlocked
                .computeIfAbsent(playerId, ignored -> new HashSet<>())
                .add(tradeId);

        if (added) {
            setDirty();
        }

        return added;
    }

    public Set<String> getUnlocked(UUID playerId) {
        return Set.copyOf(
                unlocked.getOrDefault(playerId, Set.of())
        );
    }

    @Override
    public CompoundTag save(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        ListTag players = new ListTag();

        for (Map.Entry<UUID, Set<String>> entry : unlocked.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Player", entry.getKey());

            ListTag trades = new ListTag();

            for (String tradeId : entry.getValue()) {
                trades.add(StringTag.valueOf(tradeId));
            }

            playerTag.put("Trades", trades);
            players.add(playerTag);
        }

        tag.put("Players", players);

        return tag;
    }

    public static PlayerTradeData load(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        PlayerTradeData data = new PlayerTradeData();

        ListTag players = tag.getList(
                "Players",
                Tag.TAG_COMPOUND
        );

        for (int i = 0; i < players.size(); i++) {
            CompoundTag playerTag = players.getCompound(i);

            if (!playerTag.hasUUID("Player")) {
                continue;
            }

            UUID playerId = playerTag.getUUID("Player");
            Set<String> trades = new HashSet<>();

            ListTag tradeList = playerTag.getList(
                    "Trades",
                    Tag.TAG_STRING
            );

            for (int j = 0; j < tradeList.size(); j++) {
                String tradeId = tradeList.getString(j);

                if (!tradeId.isBlank()) {
                    trades.add(tradeId);
                }
            }

            data.unlocked.put(playerId, trades);
        }

        return data;
    }
}