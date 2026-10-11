package net.bayruby.contractnpc.villager.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PayloadRegistry {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PayloadRegistry::registerPayloads);
    }

    private static void registerPayloads(
            RegisterPayloadHandlersEvent event
    ) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                AffectionSyncPayload.TYPE,
                AffectionSyncPayload.CODEC,
                AffectionSyncPayload.HANDLER
        );
    }
}