package com.femtendo.realecon;

import com.femtendo.realecon.init.ModBlockEntities;
import com.femtendo.realecon.init.ModBlocks;
import com.femtendo.realecon.init.ModCreativeModeTabs;
import com.femtendo.realecon.init.ModItems;
import com.femtendo.realecon.init.ModMenuTypes;
import com.femtendo.realecon.network.PacketHandler;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RealEcon.MODID)
@Mod.EventBusSubscriber(modid = RealEcon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RealEcon {
    public static final String MODID = "realecon";

    public RealEcon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 1. Register ALL our content (Items, Blocks, BlockEntities, Menus)
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);

        // Tells Forge to run our setups and inject into the Creative Menu
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::addCreative);

        // 2. Register our Configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC, "realecon-currency.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, MessageConfig.SERVER_SPEC, "realecon-messages.toml");

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(PacketHandler::register);
    }

    // NEW: Puts our custom items into the standard "Functional Blocks" creative tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.WAGEBOX);
            event.accept(ModItems.TRADE_LEDGER);
            event.accept(ModItems.MASTER_LEDGER);
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
    // --- 0.9.9.10 FIX: INSTANT MARKET SEEDING ---
    @SubscribeEvent
    public void onServerStarted(net.minecraftforge.event.server.ServerStartedEvent event) {
        com.femtendo.realecon.logic.market.GlobalMarketManager market = com.femtendo.realecon.logic.market.GlobalMarketManager.get(event.getServer());

        // If the market index is completely empty (first boot or wiped data),
        // force an immediate epoch run to populate all the baseline prices!
        if (market.getPriceIndex().isEmpty()) {
            market.checkAndRunEpoch(event.getServer(), true);
        }
    }
}