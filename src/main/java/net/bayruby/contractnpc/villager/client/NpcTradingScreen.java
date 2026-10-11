package net.bayruby.contractnpc.villager.client;

import net.bayruby.contractnpc.villager.npc.BaseNpcEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;

public class NpcTradingScreen extends MerchantScreen {

    private static final int HEART_COUNT = 5;
    private static final int HEART_SIZE = 9;
    private static final int HEART_SPACING = 3;
    private static final int ITEM_HEART_GAP = 4;

    private final BaseNpcEntity trackedNpc;

    public NpcTradingScreen(
            MerchantMenu menu,
            Inventory inventory,
            Component title,
            BaseNpcEntity trackedNpc
    ) {
        super(menu, inventory, title);
        this.trackedNpc = trackedNpc;
    }

    public NpcTradingScreen(
            MerchantMenu menu,
            Inventory inventory,
            Component title
    ) {
        this(menu, inventory, title, null);
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        super.renderLabels(graphics, mouseX, mouseY);

        BaseNpcEntity npc = trackedNpc != null && trackedNpc.isAlive()
                ? trackedNpc
                : findNpc();

        int affection = npc != null ? npc.getAffection() : 0;
        affection = Math.max(0, Math.min(HEART_COUNT, affection));

        int totalHeartWidth =
                HEART_SIZE * HEART_COUNT
                        + HEART_SPACING * (HEART_COUNT - 1);

        int totalWidth = 16 + ITEM_HEART_GAP + totalHeartWidth;

        int xOffset = 45;
        int startX = (this.imageWidth - totalWidth) / 2 + xOffset;

        int rowY = 15;

        int heartStartX = startX + 16 + ITEM_HEART_GAP;

        for (int i = 0; i < HEART_COUNT; i++) {
            ResourceLocation heartSprite =
                    ResourceLocation.withDefaultNamespace(
                            i < affection
                                    ? "hud/heart/full"
                                    : "hud/heart/container"
                    );

            graphics.blitSprite(
                    heartSprite,
                    heartStartX + i * (HEART_SIZE + HEART_SPACING),
                    rowY + 3,
                    HEART_SIZE,
                    HEART_SIZE
            );
        }

        int effectBoxSize = 30;
        int effectBoxXOffset = 60;
        int effectBoxYOffset = -8;

        int effectBoxX =
                heartStartX + totalHeartWidth + effectBoxXOffset;

        int effectBoxY =
                rowY - (effectBoxSize - 20) / 2 + effectBoxYOffset;

        graphics.blitSprite(
                ResourceLocation.withDefaultNamespace(
                        "hud/effect_background"
                ),
                effectBoxX,
                effectBoxY,
                effectBoxSize,
                effectBoxSize
        );

        if (npc != null) {
            ItemStack desiredItem = new ItemStack(npc.getWhimItem());

            int carrotSize = 22;

            graphics.pose().pushPose();

            graphics.pose().translate(
                    effectBoxX + (effectBoxSize - carrotSize) / 2.0,
                    effectBoxY + (effectBoxSize - carrotSize) / 2.0,
                    0
            );

            graphics.pose().scale(
                    carrotSize / 16.0F,
                    carrotSize / 16.0F,
                    1.0F
            );

            graphics.renderItem(desiredItem, 0, 0);

            graphics.pose().popPose();
        }
    }

    private BaseNpcEntity findNpc() {
        if (trackedNpc != null && trackedNpc.isAlive()) {
            return trackedNpc;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null) {
            return null;
        }

        String screenTitle = this.getTitle().getString();

        String profession = screenTitle.startsWith("Trading - ")
                ? screenTitle.substring("Trading - ".length()).trim()
                : screenTitle.trim();

        if (profession.isEmpty()) {
            return null;
        }

        return minecraft.level.getEntitiesOfClass(
                        BaseNpcEntity.class,
                        minecraft.player.getBoundingBox().inflate(8.0D)
                )
                .stream()
                .filter(npc ->
                        npc.getDefinition()
                                .displayName()
                                .equalsIgnoreCase(profession)
                )
                .min((first, second) ->
                        Double.compare(
                                first.distanceToSqr(minecraft.player),
                                second.distanceToSqr(minecraft.player)
                        )
                )
                .orElse(null);
    }
}