package net.bayruby.contractnpc.villager.trade;

import net.bayruby.contractnpc.villager.Config;
import net.bayruby.contractnpc.villager.Villager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class NpcTrades {

    private NpcTrades() {
    }

    public static Map<String, NpcTrade> all() {
        Map<String, NpcTrade> trades = new LinkedHashMap<>();

        for (String line : Config.TRADES.get()) {
            try {
                NpcTrade trade = parse(line);

                if (trades.containsKey(trade.id())) {
                    Villager.LOGGER.warn(
                            "Duplicate trade ID '{}'; ignoring duplicate definition",
                            trade.id()
                    );
                    continue;
                }

                trades.put(trade.id(), trade);
            } catch (Exception exception) {
                Villager.LOGGER.warn(
                        "Ignoring invalid trade definition '{}': {}",
                        line,
                        exception.getMessage()
                );
            }
        }

        return trades;
    }

    public static NpcTrade find(String id) {
        return all().get(id);
    }

    /**
     * Returns trades whose IDs begin with this NPC's profession ID.
     *
     * Example: blacksmith:iron belongs to blacksmith NPCs.
     */
    public static List<NpcTrade> forNpc(String npcId) {
        String prefix = npcId + ":";

        return all()
                .values()
                .stream()
                .filter(trade -> trade.id().startsWith(prefix))
                .toList();
    }

    private static NpcTrade parse(String line) {
        String[] fields = line.split(",", -1);

        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }

        return switch (fields.length) {
            // Legacy single input:
            // id,item1,result
            case 3 -> new NpcTrade(
                    fields[0],
                    fields[1],
                    1,
                    "",
                    0,
                    fields[2],
                    1
            );

            // Legacy two inputs:
            // id,item1,item2,result
            case 4 -> new NpcTrade(
                    fields[0],
                    fields[1],
                    1,
                    fields[2],
                    1,
                    fields[3],
                    1
            );

            // Legacy single input with quantities:
            // id,item1,item1_count,result,result_count
            case 5 -> new NpcTrade(
                    fields[0],
                    fields[1],
                    parsePositiveInt(fields[2], "first input count"),
                    "",
                    0,
                    fields[3],
                    parsePositiveInt(fields[4], "result count")
            );

            // Single input with quantities and explicit global scope:
            // id,item1,item1_count,result,result_count,global
            case 6 -> {
                requireScope(fields[5]);

                yield new NpcTrade(
                        fields[0],
                        fields[1],
                        parsePositiveInt(fields[2], "first input count"),
                        "",
                        0,
                        fields[3],
                        parsePositiveInt(fields[4], "result count"),
                        parseScope(fields[5]),
                        0
                );
            }

            // Either:
            // Legacy two inputs with quantities:
            // id,item1,item1_count,item2,item2_count,result,result_count
            //
            // Or local single input:
            // id,item1,item1_count,result,result_count,local,hearts
            case 7 -> {
                if (isScope(fields[5])) {
                    NpcTrade.Scope scope = parseScope(fields[5]);

                    yield new NpcTrade(
                            fields[0],
                            fields[1],
                            parsePositiveInt(fields[2], "first input count"),
                            "",
                            0,
                            fields[3],
                            parsePositiveInt(fields[4], "result count"),
                            scope,
                            parseRequiredHearts(fields[6], scope)
                    );
                }

                yield new NpcTrade(
                        fields[0],
                        fields[1],
                        parsePositiveInt(fields[2], "first input count"),
                        fields[3],
                        parsePositiveInt(fields[4], "second input count"),
                        fields[5],
                        parsePositiveInt(fields[6], "result count")
                );
            }

            // Two inputs with quantities and explicit global scope:
            // id,item1,item1_count,item2,item2_count,result,result_count,global
            case 8 -> {
                requireScope(fields[7]);

                yield new NpcTrade(
                        fields[0],
                        fields[1],
                        parsePositiveInt(fields[2], "first input count"),
                        fields[3],
                        parsePositiveInt(fields[4], "second input count"),
                        fields[5],
                        parsePositiveInt(fields[6], "result count"),
                        parseScope(fields[7]),
                        0
                );
            }

            // Two inputs with quantities and explicit local scope:
            // id,item1,item1_count,item2,item2_count,result,result_count,local,hearts
            case 9 -> {
                requireScope(fields[7]);

                NpcTrade.Scope scope = parseScope(fields[7]);

                yield new NpcTrade(
                        fields[0],
                        fields[1],
                        parsePositiveInt(fields[2], "first input count"),
                        fields[3],
                        parsePositiveInt(fields[4], "second input count"),
                        fields[5],
                        parsePositiveInt(fields[6], "result count"),
                        scope,
                        parseRequiredHearts(fields[8], scope)
                );
            }

            default -> throw new IllegalArgumentException(
                    "Expected 3, 4, 5, 6, 7, 8, or 9 fields but found "
                            + fields.length
            );
        };
    }

    private static int parsePositiveInt(String value, String description) {
        int parsed;

        try {
            parsed = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid " + description + ": " + value
            );
        }

        if (parsed < 1) {
            throw new IllegalArgumentException(
                    description + " must be at least 1"
            );
        }

        return parsed;
    }

    private static int parseRequiredHearts(
            String value,
            NpcTrade.Scope scope
    ) {
        if (scope == NpcTrade.Scope.GLOBAL) {
            return 0;
        }

        int hearts = parsePositiveInt(value, "required hearts");

        if (hearts > 5) {
            throw new IllegalArgumentException(
                    "Required hearts must be between 1 and 5"
            );
        }

        return hearts;
    }

    private static boolean isScope(String value) {
        return "global".equalsIgnoreCase(value)
                || "local".equalsIgnoreCase(value);
    }

    private static void requireScope(String value) {
        if (!isScope(value)) {
            throw new IllegalArgumentException(
                    "Scope must be 'global' or 'local'"
            );
        }
    }

    private static NpcTrade.Scope parseScope(String value) {
        return switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "global" -> NpcTrade.Scope.GLOBAL;
            case "local" -> NpcTrade.Scope.LOCAL;
            default -> throw new IllegalArgumentException(
                    "Unknown trade scope: " + value
            );
        };
    }
}