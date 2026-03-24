package com.femtendo.realecon.init;

import com.femtendo.realecon.RealEcon;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RealEcon.MODID);

    // Note: The 'unused' warning can be safely ignored. Forge's registry system handles this.
    public static final RegistryObject<CreativeModeTab> REALECON_TAB = CREATIVE_MODE_TABS.register("realecon_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.WAGEBOX.get())) // FIXED: Uses your registered Block
                    .title(Component.translatable("creativetab.realecon_tab"))
                    .displayItems((parameters, output) -> {
                        // FIXED: Names updated to match your ModItems and ModBlocks
                        output.accept(ModBlocks.WAGEBOX.get());
                        output.accept(ModItems.TRADE_LEDGER.get());
                        output.accept(ModItems.MASTER_LEDGER.get());
                        output.accept(ModItems.PHYSICAL_COIN.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}