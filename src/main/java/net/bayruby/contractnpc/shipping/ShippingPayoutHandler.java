package net.bayruby.contractnpc.shipping;

import net.bayruby.contractnpc.Config;
import net.bayruby.contractnpc.ContractNpc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import notsosig.numismaticoverhaul.cap.CurrencyHolder;
import notsosig.numismaticoverhaul.cap.CurrencyHolderAttacher;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = ContractNpc.MODID)
public class ShippingPayoutHandler {

    private static final long TICKS_PER_DAY = 24000L;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        long dayTime = server.overworld().getDayTime();
        long timeOfDay = Math.floorMod(dayTime, 24000L);

        // Wait until 6:02 AM or later.
        // This avoids missing the payout if the exact tick is skipped.
        if (timeOfDay < 40) {
            return;
        }

        long currentDay = Math.floorDiv(dayTime, 24000L);

        ShippingPayoutData payoutData = server.overworld()
                .getDataStorage()
                .computeIfAbsent(
                        ShippingPayoutData.FACTORY,
                        ShippingPayoutData.DATA_NAME
                );

        // Persistent across server restarts.
        if (!payoutData.markProcessed(currentDay)) {
            return;
        }

        Map<String, Integer> prices = loadPrices();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sellShippingInventory(player, prices);
        }
    }


    private static Map<String, Integer> loadPrices() {
        Map<String, Integer> prices = new HashMap<>();

        for (String entry : Config.ITEM_STRINGS.get()) {
            String[] parts = entry.split("=", 2);

            if (parts.length != 2) {
                continue;
            }

            try {
                int value = Integer.parseInt(parts[1]);

                if (value > 0) {
                    prices.put(parts[0], value);
                }
            } catch (NumberFormatException ignored) {
                // Ignore invalid config entries.
            }
        }

        return prices;
    }

    private static void sellShippingInventory(
            ServerPlayer player,
            Map<String, Integer> prices
    ) {
        ShippingInventory shippingInventory =
                player.getData(
                        ModAttachments.SHIPPING_INVENTORY.get()
                );

        SimpleContainer inventory =
                shippingInventory.getInventory();

        long totalPayout = 0;

        // First calculate the payout for all eligible stacks.
        for (int slot = 0;
             slot < inventory.getContainerSize();
             slot++) {

            ItemStack stack = inventory.getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            ResourceLocation itemId =
                    BuiltInRegistries.ITEM.getKey(stack.getItem());

            Integer unitPrice = prices.get(itemId.toString());

            if (unitPrice == null || unitPrice <= 0) {
                continue;
            }

            totalPayout += (long) unitPrice * stack.getCount();
        }

        if (totalPayout <= 0) {
            return;
        }

        // Remove eligible stacks after calculating the total.
        for (int slot = 0;
             slot < inventory.getContainerSize();
             slot++) {

            ItemStack stack = inventory.getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            ResourceLocation itemId =
                    BuiltInRegistries.ITEM.getKey(stack.getItem());

            Integer unitPrice = prices.get(itemId.toString());

            if (unitPrice != null && unitPrice > 0) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }

        // Credit the player's Numismatic Overhaul purse, keep this method- this is commented for testing but it works reliably
        // CurrencyHolder holder =
                // CurrencyHolderAttacher.getCurrencyHolder(player);

        // holder.modify(totalPayout, player);
        // holder.commitTransactions(player);
    }
}