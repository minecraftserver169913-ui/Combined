
package net.bayruby.contractnpc.villager.item;



import net.bayruby.contractnpc.villager.ModEntities;
import net.bayruby.contractnpc.villager.npc.BaseNpcEntity;
import net.bayruby.contractnpc.villager.npc.NpcDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class NpcContractItem extends Item {

    private final String npcId;

    public NpcContractItem(String npcId, Properties properties) {
        super(properties);

        // Fail early if the contract references an unknown NPC.
        NpcDefinitions.get(npcId);
        this.npcId = npcId;
    }

    public String getNpcId() {
        return npcId;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        BlockPos spawnBlock = context.getClickedPos()
                .relative(context.getClickedFace());

        Vec3 position = Vec3.atBottomCenterOf(spawnBlock);

        BaseNpcEntity npc = ModEntities.GENERIC_NPC.get()
                .create(serverLevel);

        if (npc == null) {
            return InteractionResult.FAIL;
        }

        npc.setNpcId(npcId);

        Player player = context.getPlayer();

        npc.moveTo(
                position.x,
                position.y,
                position.z,
                player != null ? player.getYRot() : 0.0F,
                0.0F
        );

        if (!serverLevel.addFreshEntity(npc)) {
            return InteractionResult.FAIL;
        }

        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.CONSUME;
    }
}
