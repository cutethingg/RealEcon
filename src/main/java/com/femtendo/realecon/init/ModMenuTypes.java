package com.femtendo.realecon.init;

import com.femtendo.realecon.RealEcon;
import com.femtendo.realecon.content.ledger.gui.TradeLedgerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, RealEcon.MODID);

    public static final RegistryObject<MenuType<TradeLedgerMenu>> TRADE_LEDGER_MENU = MENUS.register("trade_ledger_menu",
            () -> IForgeMenuType.create((windowId, inv, data) -> new TradeLedgerMenu(windowId, inv)));
    // Add this to your existing register list
    public static final RegistryObject<MenuType<com.femtendo.realecon.content.wagebox.WageboxStorageMenu>> WAGEBOX_STORAGE_MENU =
            MENUS.register("wagebox_storage_menu", () -> IForgeMenuType.create(com.femtendo.realecon.content.wagebox.WageboxStorageMenu::new));
}