package net.bayruby.contractnpc.client;

import com.mojang.datafixers.util.Either;

import net.bayruby.contractnpc.Config;
import net.bayruby.contractnpc.ContractNpc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

@EventBusSubscriber(
        modid = ContractNpc.MODID,
        value = Dist.CLIENT
)
public class ShippingTooltipHandler {

    @SubscribeEvent
    public static void onGatherTooltip(
            RenderTooltipEvent.GatherComponents event
    ) {
        ItemStack stack = event.getItemStack();

        int itemCost = getShippingCost(stack);

        if (itemCost <= 0) {
            return;
        }

        // Calculate the value of the entire stack.
        long totalCost = (long) itemCost * stack.getCount();

        // Insert the custom coin tooltip.
        event.getTooltipElements().add(
                1,
                Either.right(
                        new BronzeCostTooltip(totalCost)
                )
        );
    }

    private static int getShippingCost(ItemStack stack) {
        ResourceLocation itemId =
                stack.getItemHolder()
                        .unwrapKey()
                        .map(key -> key.location())
                        .orElse(null);

        if (itemId == null) {
            return -1;
        }

        String id = itemId.toString();

        for (String entry : Config.ITEM_STRINGS.get()) {
            String[] parts = entry.split("=", 2);

            if (parts.length != 2) {
                continue;
            }

            if (!parts[0].equals(id)) {
                continue;
            }

            try {
                return Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }

        return -1;
    }
}