package com.femtendo.realecon.init;

import com.femtendo.realecon.RealEcon;
import com.femtendo.realecon.content.wagebox.WageboxBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RealEcon.MODID);

    public static final RegistryObject<BlockEntityType<WageboxBlockEntity>> WAGEBOX_BE =
            BLOCK_ENTITIES.register("wagebox", () ->
                    BlockEntityType.Builder.of(
                            // This lambda correctly matches the arguments!
                            (pos, state) -> new WageboxBlockEntity(ModBlockEntities.WAGEBOX_BE.get(), pos, state),
                            ModBlocks.WAGEBOX.get()
                    ).build(null)
            );
}