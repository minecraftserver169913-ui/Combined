package net.bayruby.contractnpc.villager.npc;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.bayruby.contractnpc.villager.Villager;
import net.bayruby.contractnpc.villager.trade.NpcTrade;
import net.bayruby.contractnpc.villager.trade.NpcTrades;
import net.bayruby.contractnpc.villager.trade.PlayerTradeData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

public class BaseNpcEntity extends PathfinderMob implements Merchant {

    private static final int MIN_AFFECTION = 0;
    private static final int MAX_AFFECTION = 5;

    private static final EntityDataAccessor<String> NPC_ID =
            SynchedEntityData.defineId(
                    BaseNpcEntity.class,
                    EntityDataSerializers.STRING
            );

    private static final EntityDataAccessor<Integer> AFFECTION =
            SynchedEntityData.defineId(
                    BaseNpcEntity.class,
                    EntityDataSerializers.INT
            );

    private static final EntityDataAccessor<String> WHIM_ITEM_ID =
            SynchedEntityData.defineId(
                    BaseNpcEntity.class,
                    EntityDataSerializers.STRING
            );

    private static final List<Item> WHIM_ITEMS = List.of(
            Items.CARROT,
            Items.BREAD,
            Items.APPLE,
            Items.WHEAT,
            Items.COAL,
            Items.IRON_INGOT,
            Items.EMERALD
    );

    private Player tradingPlayer;
    private MerchantOffers offers = new MerchantOffers();
    private int merchantXp;

    private boolean whimInitialized = false;

    /*
     * These unlocks belong to this specific NPC, not the player.
     * They persist through the entity's NBT save data.
     */
    private final Set<String> unlockedLocalTrades = new HashSet<>();

    public BaseNpcEntity(
            EntityType<? extends BaseNpcEntity> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(NPC_ID, NpcDefinitions.BLACKSMITH.id());
        builder.define(AFFECTION, MIN_AFFECTION);
        builder.define(WHIM_ITEM_ID, "minecraft:carrot");
    }

    // --------------------------------------------------
    // Whim system
    // --------------------------------------------------

    public Item getWhimItem() {
        ResourceLocation itemId = ResourceLocation.parse(
                entityData.get(WHIM_ITEM_ID)
        );

        Item item = BuiltInRegistries.ITEM.get(itemId);

        return item != Items.AIR ? item : Items.CARROT;
    }

    private void setWhimItem(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        entityData.set(WHIM_ITEM_ID, itemId.toString());
    }

    private void rerollWhim() {
        if (WHIM_ITEMS.size() <= 1) {
            setWhimItem(WHIM_ITEMS.get(0));
            whimInitialized = true;
            return;
        }

        Item previousWhim = getWhimItem();
        Item nextWhim;

        do {
            nextWhim = WHIM_ITEMS.get(
                    level().getRandom().nextInt(WHIM_ITEMS.size())
            );
        } while (nextWhim == previousWhim);

        setWhimItem(nextWhim);
        whimInitialized = true;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide && !whimInitialized) {
            rerollWhim();
        }
    }

    // --------------------------------------------------
    // Affection
    // --------------------------------------------------

    public int getAffection() {
        return entityData.get(AFFECTION);
    }

    public void setAffection(int affection) {
        int clamped = Math.max(
                MIN_AFFECTION,
                Math.min(MAX_AFFECTION, affection)
        );

        entityData.set(AFFECTION, clamped);

        if (!level().isClientSide) {
            unlockLocalTradesForAffection();
        }
    }

    // --------------------------------------------------
    // NPC identity
    // --------------------------------------------------

    public String getNpcId() {
        return entityData.get(NPC_ID);
    }

    public void setNpcId(String id) {
        NpcDefinition definition = NpcDefinitions.get(id);

        entityData.set(NPC_ID, definition.id());

        applyDefinitionAttributes(definition);

        if (!level().isClientSide) {
            unlockLocalTradesForAffection();
        }
    }

    public NpcDefinition getDefinition() {
        NpcDefinition definition = NpcDefinitions.find(getNpcId());

        return definition != null
                ? definition
                : NpcDefinitions.BLACKSMITH;
    }

    // --------------------------------------------------
    // Local trade unlocks
    // --------------------------------------------------

    /**
     * Unlocks all local trades for this NPC whose heart
     * requirement has been reached.
     *
     * Previously unlocked trades stay unlocked, even if
     * affection is subsequently reduced.
     */
    private void unlockLocalTradesForAffection() {
        int affection = getAffection();
        String npcId = getNpcId();

        for (NpcTrade trade : NpcTrades.forNpc(npcId)) {
            if (trade.isLocal()
                    && affection >= trade.requiredAffection()) {
                unlockedLocalTrades.add(trade.id());
            }
        }
    }

    private boolean hasLocalTradeUnlocked(String tradeId) {
        return unlockedLocalTrades.contains(tradeId);
    }

    // --------------------------------------------------
    // Interaction
    // --------------------------------------------------

    @Override
    public InteractionResult mobInteract(
            Player player,
            InteractionHand hand
    ) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        // Fulfill the whim if the player is holding the requested item.
        if (whimInitialized && heldItem.is(getWhimItem())) {
            if (!player.getAbilities().instabuild) {
                heldItem.shrink(1);
            }

            setAffection(getAffection() + 1);
            rerollWhim();

            playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);

            return InteractionResult.CONSUME;
        }

        // Otherwise, open this NPC's currently available trades.
        openTradingFor(serverPlayer);

        return InteractionResult.CONSUME;
    }

    // --------------------------------------------------
    // Trading
    // --------------------------------------------------

    public void openTradingFor(ServerPlayer serverPlayer) {
        String npcId = getNpcId();

        PlayerTradeData playerData =
                PlayerTradeData.get(serverPlayer.server);

        // Check the affection unlocks before assembling offers.
        unlockLocalTradesForAffection();

        List<NpcTrade> availableTrades = NpcTrades.forNpc(npcId)
                .stream()
                .filter(trade -> {
                    if (trade.isGlobal()) {
                        return playerData.hasTrade(
                                serverPlayer.getUUID(),
                                trade.id()
                        );
                    }

                    return hasLocalTradeUnlocked(trade.id());
                })
                .toList();

        MerchantOffers playerOffers = new MerchantOffers();

        for (NpcTrade trade : availableTrades) {
            Optional<ItemCost> secondCost = trade.hasSecondItem()
                    ? Optional.of(
                    new ItemCost(
                            trade.item2(),
                            trade.item2Count()
                    )
            )
                    : Optional.empty();

            MerchantOffer offer = new MerchantOffer(
                    new ItemCost(
                            trade.item1(),
                            trade.item1Count()
                    ),
                    secondCost,
                    trade.resultStack(),
                    Integer.MAX_VALUE,
                    0,
                    0.0F
            );

            // Preserve the existing blacksmith discount.
            if (npcId.equals("blacksmith")) {
                offer.addToSpecialPriceDiff(-2);
            }

            playerOffers.add(offer);
        }

        this.tradingPlayer = serverPlayer;
        this.offers = playerOffers;

        this.openTradingScreen(
                serverPlayer,
                Component.literal(
                        "Trading - " + getDefinition().displayName()
                ),
                1
        );
    }

    // --------------------------------------------------
    // Merchant implementation
    // --------------------------------------------------

    @Override
    public void setTradingPlayer(Player player) {
        this.tradingPlayer = player;
    }

    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        return this.offers;
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
        // No additional trade-update behavior required.
    }

    @Override
    public int getVillagerXp() {
        return this.merchantXp;
    }

    @Override
    public void overrideXp(int xp) {
        this.merchantXp = xp;
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean isClientSide() {
        return level().isClientSide();
    }

    // --------------------------------------------------
    // NPC attributes and behavior
    // --------------------------------------------------

    private void applyDefinitionAttributes(NpcDefinition definition) {
        if (getAttribute(Attributes.MAX_HEALTH) != null) {
            getAttribute(Attributes.MAX_HEALTH)
                    .setBaseValue(definition.maxHealth());

            setHealth(getMaxHealth());
        }

        if (getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            getAttribute(Attributes.MOVEMENT_SPEED)
                    .setBaseValue(definition.movementSpeed());
        }
    }

    public static AttributeSupplier.Builder createBaseAttributes() {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
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

    @Override
    public Component getName() {
        return Component.literal(getDefinition().displayName());
    }

    // --------------------------------------------------
    // Persistence
    // --------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putString("NpcId", getNpcId());
        tag.putInt("Affection", getAffection());
        tag.putString("WhimItem", entityData.get(WHIM_ITEM_ID));

        ListTag localTrades = new ListTag();

        for (String tradeId : unlockedLocalTrades) {
            localTrades.add(StringTag.valueOf(tradeId));
        }

        tag.put("UnlockedLocalTrades", localTrades);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("NpcId")) {
            String id = tag.getString("NpcId");

            if (NpcDefinitions.find(id) != null) {
                setNpcId(id);
            }
        }

        // Older saved NPCs default to affection 0.
        setAffection(tag.getInt("Affection"));

        if (tag.contains("WhimItem")) {
            String itemId = tag.getString("WhimItem");

            try {
                ResourceLocation resourceLocation =
                        ResourceLocation.parse(itemId);

                if (BuiltInRegistries.ITEM.containsKey(resourceLocation)) {
                    entityData.set(WHIM_ITEM_ID, itemId);
                    whimInitialized = true;
                }
            } catch (Exception exception) {
                Villager.LOGGER.warn(
                        "Could not load whim item '{}' for NPC {}",
                        itemId,
                        getNpcId()
                );
            }
        }

        unlockedLocalTrades.clear();

        if (tag.contains("UnlockedLocalTrades", Tag.TAG_LIST)) {
            ListTag localTrades = tag.getList(
                    "UnlockedLocalTrades",
                    Tag.TAG_STRING
            );

            for (int i = 0; i < localTrades.size(); i++) {
                String tradeId = localTrades.getString(i);

                // Only restore local trades belonging to this NPC.
                NpcTrade trade = NpcTrades.find(tradeId);

                if (trade != null
                        && trade.isLocal()
                        && trade.id().startsWith(getNpcId() + ":")) {
                    unlockedLocalTrades.add(tradeId);
                }
            }
        }

        // Also unlock any newly configured local trades whose
        // affection requirement has already been reached.
        unlockLocalTradesForAffection();
    }
}