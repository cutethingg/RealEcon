package com.femtendo.realecon.init;

import com.femtendo.realecon.RealEcon;
import com.femtendo.realecon.content.wagebox.WageboxBlock;
import com.femtendo.realecon.content.wagebox.WageboxBlockItem; // NEW IMPORT
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RealEcon.MODID);

    public static final RegistryObject<Block> WAGEBOX = BLOCKS.register("wagebox",
            () -> new WageboxBlock(BlockBehaviour.Properties.copy(Blocks.BARREL).noOcclusion()));

    // We explicitly register the custom item here instead of using a generic helper
    public static final RegistryObject<Item> WAGEBOX_ITEM = ModItems.ITEMS.register("wagebox",
            () -> new WageboxBlockItem(WAGEBOX.get(), new Item.Properties()));
}