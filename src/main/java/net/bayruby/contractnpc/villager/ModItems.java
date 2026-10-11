
package net.bayruby.contractnpc.villager;

import java.util.LinkedHashMap;
import java.util.Map;


import net.bayruby.contractnpc.villager.item.NpcContractItem;
import net.bayruby.contractnpc.villager.npc.NpcDefinition;
import net.bayruby.contractnpc.villager.npc.NpcDefinitions;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ModItems {

    public static final Map<String, DeferredItem<NpcContractItem>> CONTRACTS =
            registerContracts();

    private ModItems() {
    }

    private static Map<String, DeferredItem<NpcContractItem>>
    registerContracts() {

        Map<String, DeferredItem<NpcContractItem>> contracts =
                new LinkedHashMap<>();

        for (NpcDefinition definition : NpcDefinitions.all()) {
            DeferredItem<NpcContractItem> item =
                    Villager.ITEMS.register(
                            definition.contractItemId(),
                            () -> new NpcContractItem(
                                    definition.id(),
                                    new net.minecraft.world.item.Item.Properties()
                                            .stacksTo(16)
                            )
                    );

            contracts.put(definition.id(), item);
        }

        return Map.copyOf(contracts);
    }

    public static DeferredItem<NpcContractItem> getContract(String npcId) {
        DeferredItem<NpcContractItem> item = CONTRACTS.get(npcId);

        if (item == null) {
            throw new IllegalArgumentException(
                    "No contract registered for NPC: " + npcId
            );
        }

        return item;
    }
}
