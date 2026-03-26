package com.femtendo.realecon.api;

import com.femtendo.realecon.RealEcon;
import com.femtendo.realecon.api.events.MarketUpdateEvent;
import com.femtendo.realecon.api.events.WageboxTradeEvent;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

// This binds our listener to the main Forge Event Bus
@Mod.EventBusSubscriber(modid = RealEcon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ApiDebugListener {

    // --- TEST 1 & 2: THE EVENTS (PILLAR 4) ---
    @SubscribeEvent
    public static void onTradeIntercept(WageboxTradeEvent event) {
        if (event.getBuyer() != null) {
            event.getBuyer().sendSystemMessage(Component.literal("§d[API TEST] Intercepted a trade for: " + event.getReward().getHoverName().getString()));
        }

        ItemStack currentReward = event.getReward();
        if (currentReward.getCount() > 1) {
            currentReward.shrink(1);
            event.setReward(currentReward);

            if (event.getBuyer() != null) {
                event.getBuyer().sendSystemMessage(Component.literal("§c[API TEST] Fiefdom took 1 item as sales tax!"));
            }
        }
    }

    @SubscribeEvent
    public static void onMarketShift(MarketUpdateEvent event) {
        System.out.println("[API TEST] The Global Market just updated! Total items priced: " + event.getNewPrices().size());
    }

    // --- TEST 3: THE COMMAND (PILLARS 1 & 3) ---
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("apitest").executes(context -> {
            try {
                ServerPlayer player = context.getSource().getPlayerOrException();
                ServerLevel level = player.serverLevel();

                // Poll Pillar 1 (Oracle)
                double diamondPrice = RealEconAPI.get().getItemValue(level, new ResourceLocation("minecraft", "diamond"));
                Map<String, Double> marketIndex = RealEconAPI.get().getLiveMarketIndex(level);
                int marketSize = marketIndex.size();

                // Poll Pillar 3 (Wealth)
                long wallet = RealEconAPI.get().getWalletBalance(player);
                double shopValue = RealEconAPI.get().getShopStockValue(level, player.getUUID());
                int shopCount = RealEconAPI.get().getPlayerShops(level, player.getUUID()).size();

                // Print the summary results to the player
                player.sendSystemMessage(Component.literal("§a--- RealEcon API Diagnostic ---"));
                player.sendSystemMessage(Component.literal("§eMarket Tracked Items: §f" + marketSize));
                player.sendSystemMessage(Component.literal("§eValue of 1 Diamond: §f$" + String.format("%.2f", diamondPrice)));
                player.sendSystemMessage(Component.literal("§bYour Wallet Balance: §f$" + wallet));
                player.sendSystemMessage(Component.literal("§bYour Shop Stock Value: §f$" + String.format("%.2f", shopValue)));
                player.sendSystemMessage(Component.literal("§bYour Active Shops: §f" + shopCount));

                // Print the full item price list
                player.sendSystemMessage(Component.literal("§6--- Live Market Prices ---"));
                if (marketIndex.isEmpty()) {
                    player.sendSystemMessage(Component.literal("§7The market index is completely empty."));
                } else {
                    for (Map.Entry<String, Double> entry : marketIndex.entrySet()) {
                        String itemName = entry.getKey();
                        double itemPrice = entry.getValue();
                        player.sendSystemMessage(Component.literal("§7" + itemName + ": §a$" + String.format("%.2f", itemPrice)));
                    }
                }

            } catch (Exception e) {
                context.getSource().sendFailure(Component.literal("Failed to run API test. Are you a player?"));
            }
            return 1;
        }));
    }
}