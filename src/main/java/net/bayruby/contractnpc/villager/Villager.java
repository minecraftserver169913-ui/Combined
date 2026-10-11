package net.bayruby.contractnpc.villager;

import net.bayruby.contractnpc.villager.client.NomadRenderer;
import net.bayruby.contractnpc.villager.client.NpcRenderer;
import net.bayruby.contractnpc.villager.client.NpcScreenEvents;
import net.bayruby.contractnpc.villager.npc.BaseNpcEntity;
import net.bayruby.contractnpc.villager.npc.NomadEntity;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Villager.MODID)
public class Villager {

    public static final String MODID = "villager";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public Villager(IEventBus modEventBus, ModContainer modContainer) {
        // Initialize all item definitions before registry events fire.
        ModItems.CONTRACTS.size();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerEntityAttributes);
        modEventBus.addListener(this::addCreative);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            // Client-only screen intercept.
            NeoForge.EVENT_BUS.addListener(NpcScreenEvents::onScreenOpening);

            // Client-only renderer registration.
            modEventBus.addListener(this::registerRenderers);
        }

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(
                ModConfig.Type.COMMON,
                Config.SPEC
        );
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Villager NPC framework initialized.");
    }

    private void registerRenderers(
            EntityRenderersEvent.RegisterRenderers event
    ) {
        event.registerEntityRenderer(
                ModEntities.GENERIC_NPC.get(),
                NpcRenderer::new
        );

        event.registerEntityRenderer(
                ModEntities.NOMAD.get(),
                NomadRenderer::new
        );
    }

    private void registerEntityAttributes(
            EntityAttributeCreationEvent event
    ) {
        event.put(
                ModEntities.GENERIC_NPC.get(),
                BaseNpcEntity.createBaseAttributes().build()
        );

        event.put(
                ModEntities.NOMAD.get(),
                NomadEntity.createBaseAttributes().build()
        );
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            ModItems.CONTRACTS.values().forEach(item -> event.accept(item.get()));
        }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Villager NPC framework server started.");
    }
}