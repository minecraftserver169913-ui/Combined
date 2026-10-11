package net.bayruby.contractnpc.villager.network;

import net.bayruby.contractnpc.villager.Villager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record AffectionSyncPayload(
        String npcId,
        int affection
) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(
                    Villager.MODID,
                    "affection_sync"
            );

    public static final Type<AffectionSyncPayload> TYPE =
            new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, AffectionSyncPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    AffectionSyncPayload::npcId,
                    ByteBufCodecs.INT,
                    AffectionSyncPayload::affection,
                    AffectionSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final IPayloadHandler<AffectionSyncPayload> HANDLER =
            (payload, context) -> {
                context.enqueueWork(() -> {
                    AffectionClientData.setAffection(
                            payload.npcId,
                            payload.affection
                    );
                });
            };
}