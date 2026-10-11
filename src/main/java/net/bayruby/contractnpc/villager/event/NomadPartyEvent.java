package net.bayruby.contractnpc.villager.event;

import java.util.ArrayList;
import java.util.List;


import net.bayruby.contractnpc.villager.ModEntities;
import net.bayruby.contractnpc.villager.Villager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class NomadPartyEvent implements ServerEvent {

    private static final int SPAWN_DISTANCE = 5;
    private static final int SPAWN_RANGE = 10;
    private static final int MIN_SPAWN_HEIGHT = 30;
    private static final int MAX_SPAWN_HEIGHT = 256;
    private static final int GROUP_SIZE_MIN = 1;
    private static final int GROUP_SIZE_MAX = 3;
    private static final int GROUP_RADIUS = 5;

    @Override
    public boolean attemptSpawn(ServerLevel level, Player player) {
        if (player == null || !player.isAlive()) {
            return false;
        }

        Vec3 playerPos = player.position();

        Vec3 center = null;

        // Find a spawn location at SPAWN_DISTANCE blocks away
        for (int attempts = 0; attempts < 10; attempts++) {
            int offsetX = level.getRandom().nextInt(SPAWN_RANGE * 2 + 1) - SPAWN_RANGE;
            int offsetZ = level.getRandom().nextInt(SPAWN_RANGE * 2 + 1) - SPAWN_RANGE;

            int spawnX = (int) playerPos.x + SPAWN_DISTANCE + offsetX;
            int spawnZ = (int) playerPos.z + SPAWN_DISTANCE + offsetZ;
            int spawnY = level.getHeight(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                    spawnX,
                    spawnZ
            );

            spawnY = Math.max(MIN_SPAWN_HEIGHT, Math.min(MAX_SPAWN_HEIGHT, spawnY));

            center = new Vec3(spawnX, spawnY, spawnZ);

            if (canSpawnAt(level, spawnX, spawnY, spawnZ)) {
                break;
            }
        }

        if (center == null) {
            return false;
        }

        int groupSize = GROUP_SIZE_MIN + level.getRandom().nextInt(GROUP_SIZE_MAX - GROUP_SIZE_MIN + 1);

        List<BlockPos> validSpawns = new ArrayList<>();

        for (int i = 0; i < 24; i++) {
            int offsetX = level.getRandom().nextInt(GROUP_RADIUS * 2 + 1) - GROUP_RADIUS;
            int offsetZ = level.getRandom().nextInt(GROUP_RADIUS * 2 + 1) - GROUP_RADIUS;

            int spawnX = (int) center.x + offsetX;
            int spawnZ = (int) center.z + offsetZ;
            int spawnY = (int) center.y;

            if (canSpawnAt(level, spawnX, spawnY, spawnZ)) {
                validSpawns.add(new BlockPos(spawnX, spawnY, spawnZ));
            }

            if (validSpawns.size() >= groupSize) {
                break;
            }
        }

        if (validSpawns.size() < groupSize) {
            return false;
        }

        int spawned = 0;

        for (BlockPos pos : validSpawns) {
            if (spawned >= groupSize) {
                break;
            }

            var nomad = ModEntities.NOMAD.get().create(level);
            if (nomad == null) {
                continue;
            }

            nomad.moveTo(
                    pos.getX() + 0.5D,
                    pos.getY(),
                    pos.getZ() + 0.5D,
                    0.0F,
                    0.0F
            );

            if (level.addFreshEntity(nomad)) {
                spawned++;
            }
        }

        if (spawned > 0) {
            Villager.LOGGER.info(
                    "Nomad Party Event: Spawned {} nomads near {}",
                    spawned,
                    player.getGameProfile().getName()
            );
            return true;
        }

        return false;
    }

    @Override
    public String getEventName() {
        return "Nomad Party";
    }

    @Override
    public Component getChatMessage(Player player) {
        return Component.literal("☆ ")
                .append(Component.literal("A nomad party has arrived nearby!")
                        .withStyle(net.minecraft.ChatFormatting.YELLOW))
                .append(Component.literal(" ☆"));
    }

    private static boolean canSpawnAt(ServerLevel level, int x, int y, int z) {
        BlockPos groundPos = new BlockPos(x, y - 1, z);
        BlockPos spawnPos = new BlockPos(x, y, z);
        BlockPos abovePos = new BlockPos(x, y + 1, z);

        BlockState ground = level.getBlockState(groundPos);
        BlockState spawn = level.getBlockState(spawnPos);
        BlockState above = level.getBlockState(abovePos);

        if (ground.isAir() || !ground.isSolid()) {
            return false;
        }

        if (!spawn.isAir() || !above.isAir()) {
            return false;
        }

        if (level.getDifficulty() == null) {
            return false;
        }

        return true;
    }
}