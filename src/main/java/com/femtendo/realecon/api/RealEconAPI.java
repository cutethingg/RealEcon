package com.femtendo.realecon.api;

import com.femtendo.realecon.capability.PlayerWealth;
import com.femtendo.realecon.capability.PlayerWealthProvider;
import com.femtendo.realecon.content.ledger.data.ActiveTradeNode;
import com.femtendo.realecon.content.wagebox.WageboxBlockEntity;
import com.femtendo.realecon.logic.EmpireManager;
import com.femtendo.realecon.logic.market.GlobalMarketManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RealEconAPI implements IRealEconAPI {

    private static final RealEconAPI INSTANCE = new RealEconAPI();

    private RealEconAPI() {}

    public static IRealEconAPI get() {
        return INSTANCE;
    }

    @Override
    public double getItemValue(ServerLevel level, ResourceLocation itemId) {
        if (level == null || itemId == null) return 0.0;

        GlobalMarketManager market = GlobalMarketManager.get(level.getServer());
        return market.getPriceIndex().getOrDefault(itemId.toString(), 0.0);
    }

    @Override
    public double getItemValue(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) return 0.0;

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return 0.0;

        double singleValue = getItemValue(level, id);
        return singleValue * stack.getCount();
    }

    @Override
    public double getInventoryValue(ServerLevel level, IItemHandler inventory) {
        if (level == null || inventory == null) return 0.0;

        GlobalMarketManager market = GlobalMarketManager.get(level.getServer());
        Map<String, Double> index = market.getPriceIndex();

        double totalValue = 0.0;

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (id != null) {
                    totalValue += index.getOrDefault(id.toString(), 0.0) * stack.getCount();
                }
            }
        }

        return totalValue;
    }

    @Override
    public Map<String, Double> getLiveMarketIndex(ServerLevel level) {
        if (level == null) return Collections.emptyMap();

        GlobalMarketManager market = GlobalMarketManager.get(level.getServer());
        return Collections.unmodifiableMap(market.getPriceIndex());
    }

    @Override
    public void injectExternalTrade(ServerLevel level, ItemStack given, ItemStack received) {
        if (level == null || given.isEmpty() || received.isEmpty()) return;

        GlobalMarketManager market = GlobalMarketManager.get(level.getServer());
        market.recordTrade(given, received);
        market.checkAndRunEpoch(level.getServer(), false);
    }

    // --- PLAYER STATS IMPLEMENTATION ---

    @Override
    public long getWalletBalance(ServerPlayer player) {
        if (player == null) return 0L;

        return player.getCapability(PlayerWealthProvider.PLAYER_WEALTH)
                .map(PlayerWealth::getNetWorth)
                .orElse(0L);
    }

    @Override
    public double getShopStockValue(ServerLevel level, UUID playerId) {
        if (level == null || playerId == null) return 0.0;

        EmpireManager manager = EmpireManager.get(level);
        GlobalMarketManager market = GlobalMarketManager.get(level.getServer());
        Map<String, Double> index = market.getPriceIndex();

        double totalStockValue = 0.0;

        for (Map.Entry<Long, ActiveTradeNode> entry : manager.getActiveWageboxes().entrySet()) {
            if (entry.getValue().getOwnerId().equals(playerId)) {
                BlockPos pos = BlockPos.of(entry.getKey());

                if (level.isLoaded(pos)) {
                    if (level.getBlockEntity(pos) instanceof WageboxBlockEntity wagebox) {
                        ItemStackHandler inv = wagebox.getInventory();
                        if (inv != null) {
                            for (int i = 0; i < inv.getSlots(); i++) {
                                ItemStack stack = inv.getStackInSlot(i);
                                if (!stack.isEmpty()) {
                                    ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
                                    if (id != null) {
                                        totalStockValue += index.getOrDefault(id.toString(), 0.0) * stack.getCount();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return totalStockValue;
    }

    @Override
    public List<BlockPos> getPlayerShops(ServerLevel level, UUID playerId) {
        if (level == null || playerId == null) return Collections.emptyList();

        EmpireManager manager = EmpireManager.get(level);
        List<BlockPos> shops = new ArrayList<>();

        for (Map.Entry<Long, ActiveTradeNode> entry : manager.getActiveWageboxes().entrySet()) {
            if (entry.getValue().getOwnerId().equals(playerId)) {
                shops.add(BlockPos.of(entry.getKey()));
            }
        }
        return shops;
    }
}