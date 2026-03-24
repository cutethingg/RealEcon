package com.femtendo.realecon.logic;

import com.femtendo.realecon.RealEcon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RealEcon.MODID)
public class TaxDayTimer {

    // Minecraft runs at 20 ticks per second.
    // 20 * 60 = 1,200 ticks per minute.
    // 1200 * 10 = 12,000 ticks (10 minutes).
    private static final int UPDATE_INTERVAL_TICKS = 12000;
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;

            if (tickCounter >= UPDATE_INTERVAL_TICKS) {
                tickCounter = 0;

                // Tax Day: Update everyone silently in the background
                if (event.getServer() != null) {
                    for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                        WealthScanner.updatePlayerWealth(player, true);
                    }
                }
            }
        }
    }
}