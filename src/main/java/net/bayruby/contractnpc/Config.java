package net.bayruby.contractnpc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {

    private static final ModConfigSpec.Builder BUILDER =
            new ModConfigSpec.Builder();



    public static final ModConfigSpec.ConfigValue<
            List<? extends String>
            > ITEM_STRINGS =
            BUILDER
                    .comment(
                            "Item values in bronze coins.",
                            "Format: item_id=value",
                            "100 bronze = 1 silver.",
                            "100 silver = 1 gold.",
                            "Example: minecraft:diamond_pickaxe=20535"
                    )
                    .defineListAllowEmpty(
                            "items",
                            List.of(
                                    "minecraft:diamond_pickaxe=20000"
                            ),
                            () -> "minecraft:diamond_pickaxe=20000",
                            Config::validateShippingItem
                    );

    static final ModConfigSpec SPEC =
            BUILDER.build();

    private static boolean validateShippingItem(
            final Object obj
    ) {
        if (!(obj instanceof String entry)) {
            return false;
        }

        String[] parts = entry.split("=", 2);

        if (parts.length != 2) {
            return false;
        }

        String itemName = parts[0];
        String valueString = parts[1];

        // Validate the item ID.
        try {
            ResourceLocation itemId =
                    ResourceLocation.parse(itemName);

            if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
                return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }

        // Validate the emerald cost.
        try {
            int value = Integer.parseInt(valueString);

            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}