package net.bayruby.contractnpc.villager.client;

import net.bayruby.contractnpc.villager.npc.NomadEntity;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class NomadRenderer
        extends MobRenderer<NomadEntity, VillagerModel<NomadEntity>> {

    private static final ResourceLocation NOMAD_TEXTURE =
            ResourceLocation.withDefaultNamespace(
                    "textures/entity/villager/wandering_trader.png"
            );

    public NomadRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new VillagerModel<>(
                        context.bakeLayer(ModelLayers.VILLAGER)
                ),
                0.5F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(NomadEntity entity) {
        return NOMAD_TEXTURE;
    }
}