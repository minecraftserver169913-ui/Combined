package net.bayruby.contractnpc;

import net.bayruby.contractnpc.block.ModBlocks;
import net.bayruby.contractnpc.item.ModItems;
import net.bayruby.contractnpc.menu.ModMenus;
import net.bayruby.contractnpc.shipping.ModAttachments;
import net.bayruby.contractnpc.villager.Villager;
import net.minecraft.world.item.CreativeModeTabs;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ContractNpc.MODID)
public class ContractNpc {

    public static final String MODID = "contractnpc";

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MODID);

    public ContractNpc(
            IEventBus modEventBus,
            ModContainer modContainer
    ) {
        ITEMS.register(modEventBus);

        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModAttachments.ATTACHMENTS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);

        Villager.register(modEventBus);
        Villager.registerConfig(modContainer);

        modEventBus.addListener(this::addCreative);

        NeoForge.EVENT_BUS.register(this);

        new CropPlantingHandler();

        modContainer.registerConfig(
                ModConfig.Type.COMMON,
                Config.SPEC
        );
    }

    private void addCreative(
            BuildCreativeModeTabContentsEvent event
    ) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.SHIPPING_CONTAINER.get());
            event.accept(ModItems.POTATO_SPROUTS.get());
            event.accept(ModItems.CARROT_SEEDS.get());
            event.accept(ModItems.ONION_SEEDS);
        }
    }

    @SubscribeEvent
    public void onServerStarting(
            ServerStartingEvent event
    ) {
        // Nothing needed yet.
    }
}
