package net.bayruby.contractnpc.villager.network;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side cache for affection data synced from the server.
 * Maps NPC ID -> affection level (0-5).
 */
public final class AffectionClientData {

    private static final Map<String, Integer> AFFECTION_CACHE = new HashMap<>();

    private AffectionClientData() {
    }

    public static void setAffection(String npcId, int affection) {
        int clamped = Math.max(0, Math.min(5, affection));
        AFFECTION_CACHE.put(npcId, clamped);
    }

    public static int getAffection(String npcId) {
        return AFFECTION_CACHE.getOrDefault(npcId, 0);
    }

    public static void clear() {
        AFFECTION_CACHE.clear();
    }
}