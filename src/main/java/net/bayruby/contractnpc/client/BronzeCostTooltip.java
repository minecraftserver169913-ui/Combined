package net.bayruby.contractnpc.client;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record BronzeCostTooltip(long amount)
        implements TooltipComponent {
}