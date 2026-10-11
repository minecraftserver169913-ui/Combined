package net.bayruby.contractnpc.item;

import net.bayruby.contractnpc.ContractNpc;
import net.bayruby.contractnpc.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ContractNpc.MODID);

    public static final DeferredItem<Item> SHIPPING_CONTAINER =
            ITEMS.register(
                    "shipping_container",
                    () -> new BlockItem(
                            ModBlocks.SHIPPING_CONTAINER.get(),
                            new Item.Properties()
                    )
            );

    public static final DeferredItem<Item> POTATO_SPROUTS =
            ITEMS.register(
                    "potato_sprouts",
                    () -> new ItemNameBlockItem(
                            Blocks.POTATOES,
                            new Item.Properties()
                    )
            );

    public static final DeferredItem<Item> CARROT_SEEDS =
            ITEMS.register(
                    "carrot_seeds",
                    () -> new ItemNameBlockItem(
                            Blocks.CARROTS,
                            new Item.Properties()
                    )
            );

    public static final DeferredItem<Item> ONION_SEEDS =
            ITEMS.register("onion_seeds", () ->
                    new ItemNameBlockItem(
                            BuiltInRegistries.BLOCK.get(
                                    ResourceLocation.parse("farmersdelight:onions")
                            ),
                            new Item.Properties()
                    )
            );
}