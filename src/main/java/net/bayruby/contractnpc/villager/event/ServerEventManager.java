package net.bayruby.contractnpc.villager.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all registered server events.
 * Acts as a registry and provides methods to pick random events.
 */
public class ServerEventManager {

    private static final List<ServerEvent> REGISTERED_EVENTS = new ArrayList<>();

    static {
        // Register all server events here
        registerEvent(new NomadPartyEvent());
    }

    public static void registerEvent(ServerEvent event) {
        REGISTERED_EVENTS.add(event);
    }

    /**
     * Gets a random registered server event.
     */
    public static ServerEvent getRandomEvent(net.minecraft.world.level.Level level) {
        if (REGISTERED_EVENTS.isEmpty()) {
            return null;
        }

        int randomIndex = level.getRandom().nextInt(REGISTERED_EVENTS.size());
        return REGISTERED_EVENTS.get(randomIndex);
    }

    /**
     * Gets all registered events.
     */
    public static List<ServerEvent> getAllEvents() {
        return new ArrayList<>(REGISTERED_EVENTS);
    }
}