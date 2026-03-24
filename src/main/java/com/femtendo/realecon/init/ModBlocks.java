package com.femtendo.realecon.init;

import com.femtendo.realecon.RealEcon;
import com.femtendo.realecon.content.wagebox.WageboxBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RealEcon.MODID);

    // 1. Register the Wagebox Block
    // We copy the properties of a Barrel (wood sounds, axe to break, etc.) and add noOcclusion() so it doesn't cause lighting glitches
    public static final RegistryObject<Block> WAGEBOX = registerBlock("wagebox",
            () -> new WageboxBlock(BlockBehaviour.Properties.copy(Blocks.BARREL).noOcclusion()));

    // Helper method: Registers the block AND creates an Item version of it
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}