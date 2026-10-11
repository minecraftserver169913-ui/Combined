package net.bayruby.contractnpc.villager;


import net.bayruby.contractnpc.villager.npc.BaseNpcEntity;
import net.bayruby.contractnpc.villager.npc.NomadEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Villager.MODID);

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<BaseNpcEntity>
            > GENERIC_NPC = ENTITY_TYPES.register(
            "npc",
            () -> EntityType.Builder
                    .of(BaseNpcEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .build("npc")
    );

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<NomadEntity>
            > NOMAD = ENTITY_TYPES.register(
            "nomad",
            () -> EntityType.Builder
                    .of(NomadEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .build("nomad")
    );

    private ModEntities() {
    }
}