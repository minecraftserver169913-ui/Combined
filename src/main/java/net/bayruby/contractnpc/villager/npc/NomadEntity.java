package net.bayruby.contractnpc.villager.npc;

import net.bayruby.contractnpc.villager.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class NomadEntity extends PathfinderMob {

    private static final int MIN_TAME_CHANCE = 5;
    private static final int BASE_TAME_MULTIPLIER = 2;
    private static final int DESPAWN_TIMER_MAX = 6000; // 5 minutes in ticks

    private int despawnTimer = 0;

    public NomadEntity(
            EntityType<? extends NomadEntity> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(
                6,
                new LookAtPlayerGoal(this, Player.class, 8.0F)
        );
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createBaseAttributes() {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    public Component getName() {
        return Component.literal("Nomad");
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            return;
        }

        despawnTimer++;

        // Despawn after 5 minutes (6000 ticks)
        if (despawnTimer >= DESPAWN_TIMER_MAX) {
            this.discard();
        }
    }

    @Override
    public InteractionResult mobInteract(
            Player player,
            InteractionHand hand
    ) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.isEmpty()) {
            return InteractionResult.PASS;
        }

        // Check if the item is food
        if (!isFoodItem(heldItem)) {
            return InteractionResult.PASS;
        }

        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Get the food restoration value
        int foodValue = getFoodValue(heldItem);

        if (foodValue <= 0) {
            return InteractionResult.PASS;
        }

        // Calculate tame chance based on food value
        int tameChance = Math.max(MIN_TAME_CHANCE, foodValue * BASE_TAME_MULTIPLIER);

        // Roll the dice
        int roll = level().getRandom().nextInt(100);

        // Consume the food
        if (!player.getAbilities().instabuild) {
            heldItem.shrink(1);
        }

        playSound(SoundEvents.VILLAGER_CELEBRATE, 1.0F, 1.0F);

        if (roll < tameChance) {
            // Taming successful!
            return handleSuccessfulTame();
        } else {
            // Taming failed - nomad stays
            playSound(SoundEvents.VILLAGER_NO, 1.0F, 0.8F);
            return InteractionResult.CONSUME;
        }
    }

    /**
     * Handles a successful taming event.
     * Despawns the nomad and drops a random contract.
     */
    private InteractionResult handleSuccessfulTame() {
        // Choose a random NPC definition
        NpcDefinition randomDefinition = getRandomNpcDefinition();

        if (randomDefinition == null) {
            return InteractionResult.FAIL;
        }

        // Drop the contract item
        ItemStack contractStack = new ItemStack(
                ModItems.getContract(randomDefinition.id()).get()
        );

        this.spawnAtLocation(contractStack, 0.5F);

        // Play a success sound
        playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.2F);

        // Despawn the nomad
        this.discard();

        return InteractionResult.CONSUME;
    }

    /**
     * Gets a random NPC definition to create a contract for.
     */
    private NpcDefinition getRandomNpcDefinition() {
        java.util.Collection<NpcDefinition> allDefinitions =
                NpcDefinitions.all();

        if (allDefinitions.isEmpty()) {
            return null;
        }

        int randomIndex = level().getRandom().nextInt(allDefinitions.size());

        return allDefinitions.stream()
                .skip(randomIndex)
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if an item is a food item that can be used to tame.
     */
    private boolean isFoodItem(ItemStack stack) {
        return stack.getFoodProperties(this) != null
                || stack.is(Items.APPLE)
                || stack.is(Items.CARROT)
                || stack.is(Items.BAKED_POTATO)
                || stack.is(Items.BREAD)
                || stack.is(Items.COOKED_BEEF)
                || stack.is(Items.COOKED_CHICKEN)
                || stack.is(Items.COOKED_PORKCHOP)
                || stack.is(Items.COOKED_MUTTON)
                || stack.is(Items.GOLDEN_CARROT)
                || stack.is(Items.PUMPKIN_PIE)
                || stack.is(Items.COOKIE);
    }

    /**
     * Gets the food restoration value of an item.
     * Higher food value = higher taming chance.
     */
    private int getFoodValue(ItemStack stack) {
        // Use the item's food properties if available
        var foodProps = stack.getFoodProperties(this);
        if (foodProps != null) {
            return foodProps.nutrition();
        }

        // Manual fallback for common foods
        if (stack.is(Items.APPLE)) return 4;
        if (stack.is(Items.CARROT)) return 3;
        if (stack.is(Items.BAKED_POTATO)) return 5;
        if (stack.is(Items.BREAD)) return 5;
        if (stack.is(Items.COOKED_BEEF)) return 8;
        if (stack.is(Items.COOKED_CHICKEN)) return 6;
        if (stack.is(Items.COOKED_PORKCHOP)) return 8;
        if (stack.is(Items.COOKED_MUTTON)) return 6;
        if (stack.is(Items.GOLDEN_CARROT)) return 6;
        if (stack.is(Items.PUMPKIN_PIE)) return 8;
        if (stack.is(Items.COOKIE)) return 2;
        if (stack.is(Items.MELON_SLICE)) return 2;
        if (stack.is(Items.BEEF)) return 3;
        if (stack.is(Items.PORKCHOP)) return 3;
        if (stack.is(Items.CHICKEN)) return 2;
        if (stack.is(Items.MUTTON)) return 2;
        if (stack.is(Items.ROTTEN_FLESH)) return 4;

        return 0;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("DespawnTimer", despawnTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("DespawnTimer")) {
            despawnTimer = tag.getInt("DespawnTimer");
        }
    }
}