package net.bayruby.contractnpc.villager.trade;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record NpcTrade(
        String id,
        String item1Id,
        int item1Count,
        String item2Id,
        int item2Count,
        String resultId,
        int resultCount,
        Scope scope,
        int requiredAffection
) {

    public enum Scope {
        GLOBAL,
        LOCAL
    }

    public NpcTrade {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Trade ID cannot be empty");
        }

        if (item1Id == null || item1Id.isBlank()) {
            throw new IllegalArgumentException("First input item cannot be empty");
        }

        if (resultId == null || resultId.isBlank()) {
            throw new IllegalArgumentException("Result item cannot be empty");
        }

        if (item1Count < 1 || resultCount < 1) {
            throw new IllegalArgumentException(
                    "Input and result counts must be at least 1"
            );
        }

        boolean hasSecond = item2Id != null && !item2Id.isBlank();

        if (hasSecond && item2Count < 1) {
            throw new IllegalArgumentException(
                    "Second input count must be at least 1"
            );
        }

        if (!hasSecond && item2Count != 0) {
            throw new IllegalArgumentException(
                    "Second input count must be 0 when no second item is configured"
            );
        }

        if (!hasSecond) {
            item2Id = "";
        }

        if (scope == null) {
            scope = Scope.GLOBAL;
        }

        if (scope == Scope.LOCAL
                && (requiredAffection < 1 || requiredAffection > 5)) {
            throw new IllegalArgumentException(
                    "Local trade affection requirement must be between 1 and 5"
            );
        }

        if (scope == Scope.GLOBAL) {
            requiredAffection = 0;
        }
    }

    /**
     * Backward-compatible constructor for existing trade creation code.
     * Trades created this way are global.
     */
    public NpcTrade(
            String id,
            String item1Id,
            int item1Count,
            String item2Id,
            int item2Count,
            String resultId,
            int resultCount
    ) {
        this(
                id,
                item1Id,
                item1Count,
                item2Id,
                item2Count,
                resultId,
                resultCount,
                Scope.GLOBAL,
                0
        );
    }

    public boolean hasSecondItem() {
        return !item2Id.isBlank();
    }

    public boolean isGlobal() {
        return scope == Scope.GLOBAL;
    }

    public boolean isLocal() {
        return scope == Scope.LOCAL;
    }

    public Item item1() {
        return resolveItem(item1Id);
    }

    public Item item2() {
        if (!hasSecondItem()) {
            throw new IllegalStateException(
                    "Trade '" + id + "' has no second input item"
            );
        }

        return resolveItem(item2Id);
    }

    public Item result() {
        return resolveItem(resultId);
    }

    public ItemStack resultStack() {
        return new ItemStack(result(), resultCount);
    }

    private static Item resolveItem(String id) {
        ResourceLocation key = ResourceLocation.parse(id);
        Item item = BuiltInRegistries.ITEM.get(key);

        if (item == Items.AIR) {
            throw new IllegalArgumentException("Unknown trade item: " + id);
        }

        return item;
    }
}