package com.femtendo.realecon.init;

import com.femtendo.realecon.RealEcon;
import com.femtendo.realecon.content.ledger.MasterLedgerItem;
import com.femtendo.realecon.content.ledger.TradeLedgerItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RealEcon.MODID);

    public static final RegistryObject<Item> PHYSICAL_COIN = ITEMS.register("physical_coin",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TRADE_LEDGER = ITEMS.register("trade_ledger",
            () -> new TradeLedgerItem(new Item.Properties()));

    public static final RegistryObject<Item> MASTER_LEDGER = ITEMS.register("master_ledger",
            () -> new MasterLedgerItem(new Item.Properties()));
}