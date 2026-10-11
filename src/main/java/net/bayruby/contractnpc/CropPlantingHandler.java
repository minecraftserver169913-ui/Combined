package net.bayruby.contractnpc;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class CropPlantingHandler {

    public CropPlantingHandler() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRightClickBlock(
            PlayerInteractEvent.RightClickBlock event
    ) {
        if (event.getFace() != Direction.UP) {
            return;
        }

        if (!event.getLevel()
                .getBlockState(event.getPos())
                .is(Blocks.FARMLAND)) {
            return;
        }

        if (event.getItemStack().is(Items.POTATO)
                || event.getItemStack().is(Items.CARROT)) {

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }

        if (BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem())
                .equals(ResourceLocation.parse("farmersdelight:onion"))) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }
}