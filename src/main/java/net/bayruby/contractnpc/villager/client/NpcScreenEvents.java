package net.bayruby.contractnpc.villager.client;

import java.util.Comparator;


import net.bayruby.contractnpc.villager.npc.BaseNpcEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class NpcScreenEvents {

    private NpcScreenEvents() {
    }

    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof MerchantScreen screen)) {
            return;
        }

        // Prevent wrapping the custom trading screen again.
        if (screen instanceof NpcTradingScreen) {
            return;
        }

        String title = screen.getTitle().getString();

        if (!title.startsWith("Trading - ")) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        BaseNpcEntity npc = findClosestNpcForTitle(title, minecraft.player);

        if (npc == null) {
            return;
        }

        event.setNewScreen(
                new NpcTradingScreen(
                        screen.getMenu(),
                        minecraft.player.getInventory(),
                        screen.getTitle(),
                        npc
                )
        );
    }

    private static BaseNpcEntity findClosestNpcForTitle(
            String title,
            Player player
    ) {
        String profession = title
                .substring("Trading - ".length())
                .trim();

        if (profession.isEmpty()) {
            return null;
        }

        return player.level()
                .getEntitiesOfClass(
                        BaseNpcEntity.class,
                        player.getBoundingBox().inflate(8.0D)
                )
                .stream()
                .filter(npc ->
                        npc.getDefinition()
                                .displayName()
                                .equalsIgnoreCase(profession)
                )
                .min(
                        Comparator.comparingDouble(
                                npc -> npc.distanceToSqr(player)
                        )
                )
                .orElse(null);
    }
}