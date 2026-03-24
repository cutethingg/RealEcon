package com.femtendo.realecon;

// Imports must come AFTER the package statement
import com.femtendo.realecon.content.ledger.gui.TradeLedgerScreen;
import com.femtendo.realecon.content.ledger.gui.WageboxStorageScreen;
import com.femtendo.realecon.init.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RealEcon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.TRADE_LEDGER_MENU.get(), TradeLedgerScreen::new);
            MenuScreens.register(ModMenuTypes.WAGEBOX_STORAGE_MENU.get(), WageboxStorageScreen::new);
        });
    }
}