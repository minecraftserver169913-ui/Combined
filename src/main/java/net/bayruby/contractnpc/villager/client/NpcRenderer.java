package net.bayruby.contractnpc.villager.client;


import net.bayruby.contractnpc.villager.npc.BaseNpcEntity;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class NpcRenderer
        extends MobRenderer<BaseNpcEntity, VillagerModel<BaseNpcEntity>> {

    public NpcRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new VillagerModel<>(
                        context.bakeLayer(ModelLayers.VILLAGER)
                ),
                0.5F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(BaseNpcEntity entity) {
        return entity.getDefinition().texture();
    }
}