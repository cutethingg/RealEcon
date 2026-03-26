package com.femtendo.realecon.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The primary API for interacting with the RealEcon mod.
 * Obtain the instance via RealEconAPI.get()
 */
public interface IRealEconAPI {

    // --- MARKET ORACLE ---
    /**
     * @return The live dollar value of a single item based on the Global Market Index.
     */
    double getItemValue(ServerLevel level, ResourceLocation itemId);

    /**
     * Helper method to get the value of an entire ItemStack (Item Value * Stack Count).
     * @return The total value of the stack.
     */
    double getItemValue(ServerLevel level, ItemStack stack);

    /**
     * Scans an entire inventory (like a Chest or a Fiefdom Tax Bin) and calculates the
     * total combined live market value of all items inside.
     * @return The total dollar value of the inventory.
     */
    double getInventoryValue(ServerLevel level, IItemHandler inventory);

    /**
     * @return A read-only map of the entire current market index.
     */
    Map<String, Double> getLiveMarketIndex(ServerLevel level);

    // --- MARKET INFLUENCE ---
    /**
     * Logs an external transaction into the RealEcon market algorithm.
     * Use this if your mod handles custom trades (NPCs, Town Halls, Quest Rewards)
     * that should affect global supply and demand prices.
     * @param level    The ServerLevel (needed to access world data)
     * @param given    The item the player gave (Bounty)
     * @param received The item the player received (Reward)
     */
    void injectExternalTrade(ServerLevel level, ItemStack given, ItemStack received);

    // --- PLAYER STATS ---
    /**
     * Retrieves the digital net worth (/bal) of a currently online player.
     * @return The player's digital wallet balance.
     */
    long getWalletBalance(ServerPlayer player);

    /**
     * Scans every loaded Wagebox owned by the player and calculates the total
     * live market value of all their stock. Works for both online and offline players.
     * @return The total dollar value of the player's physical shop stock.
     */
    double getShopStockValue(ServerLevel level, UUID playerId);

    /**
     * @return A list of world coordinates for all active Wageboxes owned by this player.
     */
    List<BlockPos> getPlayerShops(ServerLevel level, UUID playerId);
}