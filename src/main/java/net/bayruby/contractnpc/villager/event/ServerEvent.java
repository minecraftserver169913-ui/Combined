package net.bayruby.contractnpc.villager.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/**
 * Base interface for server events that spawn at specific times.
 * Implementations define what happens when the event triggers.
 */
public interface ServerEvent {

    /**
     * Attempt to trigger this event for a specific player.
     *
     * @param level the server level
     * @param player the player to spawn the event near
     * @return true if the event was successfully spawned
     */
    boolean attemptSpawn(ServerLevel level, Player player);

    /**
     * Gets the name/ID of this event for logging purposes.
     */
    String getEventName();

    /**
     * Gets the chat message to display when this event triggers.
     * Called with the player who the event was triggered for.
     */
    net.minecraft.network.chat.Component getChatMessage(Player player);
}