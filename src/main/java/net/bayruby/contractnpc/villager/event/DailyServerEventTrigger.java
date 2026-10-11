package net.bayruby.contractnpc.villager.event;

import net.bayruby.contractnpc.villager.Villager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Villager.MODID)
public class DailyServerEventTrigger {

    private static final int TICKS_PER_DAY = 24000;
    private static final int EVENT_TRIGGER_START = 0; // 6:00 AM / sunrise
    private static final int EVENT_TRIGGER_END = 20; // 1 second window
    private static final int EVENT_CHANCE = 1; // 1 in 4 (25%)
    private static final int TIME_DISPLAY_INTERVAL = 20; // 1 second

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        long dayTime = server.overworld().getDayTime();
        long timeOfDay = Math.floorMod(dayTime, TICKS_PER_DAY);

        // Trigger once per day at sunrise (wake-up time)
        if (timeOfDay >= EVENT_TRIGGER_START && timeOfDay <= EVENT_TRIGGER_END) {
            long currentDay = Math.floorDiv(dayTime, TICKS_PER_DAY);

            NomadEventData eventData = server.overworld()
                    .getDataStorage()
                    .computeIfAbsent(
                            NomadEventData.FACTORY,
                            NomadEventData.DATA_NAME
                    );

            if (!eventData.markProcessed(currentDay)) {
                return;
            }

            Villager.LOGGER.info("Daily nomad event trigger at day {}", currentDay);

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                triggerEventForPlayer(server, player);
            }
        }

        // Update action bar time every second
        tickCounter++;
        if (tickCounter >= TIME_DISPLAY_INTERVAL) {
            tickCounter = 0;
            displayTimeToAllPlayers(server);
        }
    }

    private static void displayTimeToAllPlayers(MinecraftServer server) {
        long dayTime = server.overworld().getDayTime();
        long timeOfDay = Math.floorMod(dayTime, TICKS_PER_DAY);

        // Convert Minecraft time to a readable 24h-like clock,
        // where 0 ticks = 6:00 AM (sunrise)
        int hour = (int) ((timeOfDay / 1000L) + 6) % 24;
        int minute = (int) ((timeOfDay % 1000L) / 16.67);

        String timeText = String.format("Time: %02d:%02d", hour, minute);
        Component timeComponent = Component.literal(timeText);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.displayClientMessage(timeComponent, true); // true = action bar
        }
    }

    private static void triggerEventForPlayer(MinecraftServer server, ServerPlayer player) {
        if (player == null || !player.isAlive()) {
            return;
        }

        Villager.LOGGER.info(
                "{} is attempting spawn now",
                player.getGameProfile().getName()
        );

        if (server.overworld().getRandom().nextInt(EVENT_CHANCE) != 0) {
            Villager.LOGGER.info(
                    "{} roll failed",
                    player.getGameProfile().getName()
            );
            return;
        }

        ServerEvent randomEvent = ServerEventManager.getRandomEvent(server.overworld());

        if (randomEvent != null) {
            boolean success = randomEvent.attemptSpawn(server.overworld(), player);

            if (success) {
                Component chatMessage = randomEvent.getChatMessage(player);
                server.getPlayerList().broadcastSystemMessage(chatMessage, false);

                Villager.LOGGER.info(
                        "Event '{}' triggered for player {}",
                        randomEvent.getEventName(),
                        player.getGameProfile().getName()
                );
            } else {
                Villager.LOGGER.warn(
                        "Event '{}' failed to spawn for player {}",
                        randomEvent.getEventName(),
                        player.getGameProfile().getName()
                );
            }
        }
    }
}