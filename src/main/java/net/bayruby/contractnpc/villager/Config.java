package net.bayruby.contractnpc.villager;

import java.util.List;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {

    private static final ModConfigSpec.Builder BUILDER =
            new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends String>>
            TRADES;

    static {
        BUILDER.push("trades");

        TRADES = BUILDER
                .comment(
                        "=== TRADE FORMAT GUIDE ===",
                        "",
                        "All trades are CSV format with the following patterns:",
                        "",
                        "LEGACY FORMATS (default to global scope):",
                        "  3 fields:  trade_id,item1_id,result_id",
                        "  4 fields:  trade_id,item1_id,item2_id,result_id",
                        "  5 fields:  trade_id,item1_id,item1_count,result_id,result_count",
                        "  7 fields:  trade_id,item1_id,item1_count,item2_id,item2_count,result_id,result_count",
                        "",
                        "MODERN FORMATS (explicit scope):",
                        "  6 fields (global single):  trade_id,item1_id,item1_count,result_id,result_count,global",
                        "  8 fields (global dual):    trade_id,item1_id,item1_count,item2_id,item2_count,result_id,result_count,global",
                        "",
                        "LOCAL TRADES (unlock at affection threshold):",
                        "  7 fields (single):  trade_id,item1_id,item1_count,result_id,result_count,local,required_hearts",
                        "  9 fields (dual):    trade_id,item1_id,item1_count,item2_id,item2_count,result_id,result_count,local,required_hearts",
                        "",
                        "TRADE ID CONVENTION:",
                        "  - Format: npc_profession:trade_name",
                        "  - Example: blacksmith:iron, carpenter:oak_door",
                        "  - Global trades are shared across all NPCs with the same profession",
                        "  - Local trades are tied to individual NPCs and persist in their NBT data",
                        "",
                        "AFFECTION/HEARTS SYSTEM:",
                        "  - Range: 1-5 hearts required for local trades",
                        "  - Once unlocked, trades stay unlocked even if affection drops",
                        "  - Unlocks are saved per NPC (not per player)",
                        "",
                        "GLOBAL TRADES:",
                        "  - Unlocked via '/trade unlock <player> <trade_id>' command",
                        "  - Shared across all NPCs of the same profession",
                        "  - Persist in PlayerTradeData (server-wide data)",
                        "",
                        "=== EXAMPLE TRADES ===",
                        "",
                        "--- BLACKSMITH (Global & Local Examples) ---",
                        "",
                        "Global single-input trade:",
                        "  blacksmith:coal_trade,minecraft:coal,64,minecraft:iron_block,1,global",
                        "",
                        "Global dual-input trade:",
                        "  blacksmith:iron,minecraft:iron_ingot,64,minecraft:coal,64,minecraft:iron_block,1,global",
                        "",
                        "Local trades (unlock at affection thresholds):",
                        "  blacksmith:bronze_1h,minecraft:copper_ingot,8,minecraft:diamond,2,minecraft:netherite_scrap,1,local,1",
                        "  blacksmith:bronze_2h,minecraft:copper_ingot,16,minecraft:diamond,4,minecraft:netherite_scrap,2,local,2",
                        "  blacksmith:silver,minecraft:copper_ingot,32,minecraft:diamond,8,minecraft:netherite_scrap,4,local,3",
                        "  blacksmith:gold_plate,minecraft:gold_ingot,16,minecraft:diamond,16,minecraft:netherite_scrap,8,local,4",
                        "  blacksmith:legendary,minecraft:emerald,64,minecraft:diamond,64,minecraft:netherite_scrap,32,local,5",
                        "",
                        "--- CARPENTER (Global & Local Examples) ---",
                        "",
                        "Legacy format (global, defaults to 1:1 ratio):",
                        "  carpenter:stick,minecraft:stick,minecraft:oak_planks",
                        "",
                        "Local single-input trades:",
                        "  carpenter:oak_door,minecraft:oak_planks,12,minecraft:oak_door,1,local,1",
                        "  carpenter:birch_door,minecraft:birch_planks,12,minecraft:birch_door,1,local,2",
                        "  carpenter:dark_oak_door,minecraft:dark_oak_planks,12,minecraft:dark_oak_door,1,local,3",
                        "  carpenter:spruce_door,minecraft:spruce_planks,12,minecraft:spruce_door,1,local,4",
                        "  carpenter:warped_door,minecraft:warped_planks,12,minecraft:warped_door,1,local,5",
                        ""
                )
                .defineListAllowEmpty(
                        "definitions",
                        List.of(
                                "blacksmith:coal,minecraft:coal,64,minecraft:iron_ingot,64,minecraft:iron_block,1,global",
                                "blacksmith:local_1h,minecraft:copper_ingot,8,minecraft:diamond,2,minecraft:iron_sword,1,local,1",
                                "blacksmith:local_2h,minecraft:copper_ingot,16,minecraft:diamond,4,minecraft:diamond_sword,1,local,2",
                                "blacksmith:local_silver,minecraft:copper_ingot,32,minecraft:diamond,8,minecraft:netherite_scrap,4,local,3",
                                "blacksmith:local_gold,minecraft:gold_ingot,16,minecraft:diamond,16,minecraft:enchanted_golden_apple,1,local,4",
                                "blacksmith:local_legendary,minecraft:emerald,64,minecraft:diamond,64,minecraft:netherite_ingot,8,local,5",
                                "carpenter:stick,minecraft:stick,minecraft:oak_planks",
                                "carpenter:local_oak,minecraft:oak_planks,12,minecraft:oak_button,1,local,1",
                                "carpenter:local_birch,minecraft:birch_planks,12,minecraft:birch_button,1,local,2",
                                "carpenter:local_dark_oak,minecraft:dark_oak_planks,12,minecraft:dark_oak_button,1,local,3",
                                "carpenter:local_spruce,minecraft:spruce_planks,12,minecraft:spruce_button,1,local,4",
                                "carpenter:local_warped,minecraft:warped_planks,12,minecraft:warped_button,1,local,5"
                        ),
                        entry -> {
                            if (!(entry instanceof String line)) {
                                return false;
                            }

                            int count = line.split(",", -1).length;

                            return count == 3
                                    || count == 4
                                    || count == 5
                                    || count == 6
                                    || count == 7
                                    || count == 8
                                    || count == 9;
                        }
                );

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }
}