
package net.bayruby.contractnpc.villager.npc;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import net.bayruby.contractnpc.villager.Villager;

public final class NpcDefinitions {

    private static final Map<String, NpcDefinition> DEFINITIONS =
            new LinkedHashMap<>();

    public static final NpcDefinition BLACKSMITH = register(
            new NpcDefinition(
                    "blacksmith",
                    "Blacksmith",
                    ResourceLocation.withDefaultNamespace(
                            "textures/entity/villager/villager.png"
                    ),
                    30.0D,
                    0.22D
            )
    );

    public static final NpcDefinition CARPENTER = register(
            new NpcDefinition(
                    "carpenter",
                    "Carpenter",
                    ResourceLocation.withDefaultNamespace(
                            "textures/entity/villager/villager.png"
                    ),
                    30.0D,
                    0.22D
            )
    );


    // Example of another NPC definition:
    //
    // public static final NpcDefinition MERCHANT = register(
    //         new NpcDefinition(
    //                 "merchant",
    //                 "Merchant",
    //                 ResourceLocation.fromNamespaceAndPath(
    //                         Villager.MODID,
    //                         "textures/entity/npcs/merchant.png"
    //                 ),
    //                 20.0D,
    //                 0.25D
    //         )
    // );

    private NpcDefinitions() {
    }

    private static NpcDefinition register(NpcDefinition definition) {
        if (DEFINITIONS.putIfAbsent(definition.id(), definition) != null) {
            throw new IllegalArgumentException(
                    "Duplicate NPC definition: " + definition.id()
            );
        }

        return definition;
    }

    public static NpcDefinition get(String id) {
        NpcDefinition definition = DEFINITIONS.get(id);

        if (definition == null) {
            throw new IllegalArgumentException("Unknown NPC id: " + id);
        }

        return definition;
    }

    public static NpcDefinition find(String id) {
        return DEFINITIONS.get(id);
    }

    public static Collection<NpcDefinition> all() {
        return Collections.unmodifiableCollection(DEFINITIONS.values());
    }
}
