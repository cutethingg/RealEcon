package com.femtendo.realecon;

import com.femtendo.realecon.capability.PlayerWealth;
import com.femtendo.realecon.capability.PlayerWealthProvider;
import com.femtendo.realecon.command.BalCommand;
import com.femtendo.realecon.command.BaltopCommand;
import com.femtendo.realecon.command.DebugCommands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class WealthEvents {

    @Mod.EventBusSubscriber(modid = RealEcon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {
        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(PlayerWealth.class);
        }
    }

    @Mod.EventBusSubscriber(modid = RealEcon.MODID)
    public static class ForgeEventBusEvents {

        @SubscribeEvent
        public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player player) {
                if (!player.getCapability(PlayerWealthProvider.PLAYER_WEALTH).isPresent()) {
                    event.addCapability(new ResourceLocation(RealEcon.MODID, "wealth"), new PlayerWealthProvider());
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerCloned(PlayerEvent.Clone event) {
            if (event.isWasDeath()) {
                event.getOriginal().getCapability(PlayerWealthProvider.PLAYER_WEALTH).ifPresent(oldStore -> {
                    event.getEntity().getCapability(PlayerWealthProvider.PLAYER_WEALTH).ifPresent(newStore -> {
                        newStore.copyFrom(oldStore);
                    });
                });
            }
        }

        // FIXED: Now correctly imports and registers commands
        @SubscribeEvent
        public static void onCommandsRegister(RegisterCommandsEvent event) {
            BalCommand.register(event.getDispatcher());
            BaltopCommand.register(event.getDispatcher());
            DebugCommands.register(event.getDispatcher());
        }
    }
}