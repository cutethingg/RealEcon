package com.femtendo.realecon;

import com.femtendo.realecon.init.ModItems;
import com.femtendo.realecon.init.ModMenuTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RealEcon.MODID)
@Mod.EventBusSubscriber(modid = RealEcon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RealEcon {
    public static final String MODID = "realecon";

    public RealEcon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 1. Register ALL our content (Items, Blocks, BlockEntities, Menus)
        ModItems.ITEMS.register(modEventBus);
        com.femtendo.realecon.init.ModBlocks.BLOCKS.register(modEventBus);
        com.femtendo.realecon.init.ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);

        // Tells Forge to run our setups and inject into the Creative Menu
        com.femtendo.realecon.init.ModCreativeModeTabs.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        // 2. Register our Configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC, "realecon-currency.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, MessageConfig.SERVER_SPEC, "realecon-messages.toml");

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            com.femtendo.realecon.network.PacketHandler.register();
        });
    }

    // NEW: Puts our custom items into the standard "Functional Blocks" creative tab
    private void addCreative(net.minecraftforge.event.BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(com.femtendo.realecon.init.ModBlocks.WAGEBOX);
            event.accept(ModItems.TRADE_LEDGER);
            event.accept(ModItems.MASTER_LEDGER);
            event.accept(ModItems.PHYSICAL_COIN);
        }
    }

    // 3. Rebuild the cache whenever the config loads or changes
    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getModId().equals(MODID)) {
            CurrencyCache.rebuildCache();
        }
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getModId().equals(MODID)) {
            CurrencyCache.rebuildCache();
        }
    }
}