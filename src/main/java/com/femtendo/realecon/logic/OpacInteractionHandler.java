package com.femtendo.realecon.logic;

import com.femtendo.realecon.RealEcon;
import com.femtendo.realecon.content.wagebox.WageboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RealEcon.MODID)
public class OpacInteractionHandler {

    // We use HIGHEST priority so our code runs *before* OPAC's protection cancels the event
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide()) {
            BlockState state = event.getLevel().getBlockState(event.getPos());

            // If the block is our Wagebox, we forcefully allow the interaction
            if (state.getBlock() instanceof WageboxBlock) {
                event.setUseBlock(net.minecraftforge.eventbus.api.Event.Result.ALLOW);
            }
        }
    }
}