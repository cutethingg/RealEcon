package com.femtendo.realecon.logic.market;

import com.femtendo.realecon.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class GlobalMarketManager extends SavedData {

    private final Map<String, Double> currentPriceIndex = new HashMap<>();
    private final Map<String, Double> exchangeRates = new HashMap<>();
    private int tradesSinceLastUpdate = 0;

    public static GlobalMarketManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                GlobalMarketManager::load,
                GlobalMarketManager::new,
                "realecon_global_market"
        );
    }

    public int getTradesSinceLastUpdate() { return tradesSinceLastUpdate; }

    public void recordTrade(ItemStack bounty, ItemStack reward) {
        if (bounty.isEmpty() || reward.isEmpty()) return;

        String itemA = ForgeRegistries.ITEMS.getKey(bounty.getItem()).toString();
        String itemB = ForgeRegistries.ITEMS.getKey(reward.getItem()).toString();
        if (itemA.equals(itemB)) return;

        double newRate = (double) reward.getCount() / bounty.getCount();
        String edgeKey = itemA + "|" + itemB;
        String reverseKey = itemB + "|" + itemA;

        if (exchangeRates.containsKey(edgeKey)) {
            double oldRate = exchangeRates.get(edgeKey);
            if (Config.MARKET_MATH_MODE.get() == Config.MarketMathMode.WMA) {
                double weight = Config.WMA_WEIGHT.get();
                exchangeRates.put(edgeKey, (newRate * weight) + (oldRate * (1.0 - weight)));
                exchangeRates.put(reverseKey, 1.0 / exchangeRates.get(edgeKey));
            } else {
                exchangeRates.put(edgeKey, (newRate + oldRate) / 2.0);
                exchangeRates.put(reverseKey, 1.0 / exchangeRates.get(edgeKey));
            }
        } else {
            exchangeRates.put(edgeKey, newRate);
            exchangeRates.put(reverseKey, 1.0 / newRate);
        }

        tradesSinceLastUpdate++;
        this.setDirty();
    }

    // Kept method name to prevent breaking existing command calls
    public void checkAndRunEpoch(MinecraftServer server, boolean force) {
        if (!force && tradesSinceLastUpdate < Config.MARKET_UPDATE_TRADES.get()) return;

        currentPriceIndex.clear();

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        List<? extends String> configCurrencies = Config.CURRENCY_ITEMS.get();
        for (String entry : configCurrencies) {
            String[] parts = entry.split(",");
            if (parts.length == 2) {
                String itemName = parts[0].trim();
                try {
                    double value = Double.parseDouble(parts[1].trim());
                    currentPriceIndex.put(itemName, value);
                    queue.add(itemName);
                    visited.add(itemName);
                } catch (NumberFormatException ignored) {}
            }
        }

        String anchor = Config.ANCHOR_CURRENCY.get();
        if (!visited.contains(anchor)) {
            currentPriceIndex.put(anchor, 1.0);
            queue.add(anchor);
            visited.add(anchor);
        }

        Map<String, Map<String, Double>> graph = new HashMap<>();
        for (Map.Entry<String, Double> entry : exchangeRates.entrySet()) {
            String[] split = entry.getKey().split("\\|");
            graph.computeIfAbsent(split[0], k -> new HashMap<>()).put(split[1], entry.getValue());
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            double currentVal = currentPriceIndex.get(current);

            if (graph.containsKey(current)) {
                for (Map.Entry<String, Double> neighbor : graph.get(current).entrySet()) {
                    String nextItem = neighbor.getKey();
                    if (!visited.contains(nextItem)) {
                        double exchangeRate = neighbor.getValue();
                        double nextVal = currentVal / exchangeRate;

                        currentPriceIndex.put(nextItem, nextVal);
                        visited.add(nextItem);
                        queue.add(nextItem);
                    }
                }
            }
        }

        tradesSinceLastUpdate = 0;
        this.setDirty();

        server.getPlayerList().broadcastSystemMessage(net.minecraft.network.chat.Component.literal("§6[RealEcon] §eGlobal Market Prices Updated!"), false);
    }

    public Map<String, Double> getPriceIndex() { return currentPriceIndex; }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("TradesSinceUpdate", tradesSinceLastUpdate);

        ListTag ratesList = new ListTag();
        for (Map.Entry<String, Double> entry : exchangeRates.entrySet()) {
            CompoundTag rateTag = new CompoundTag();
            rateTag.putString("Edge", entry.getKey());
            rateTag.putDouble("Rate", entry.getValue());
            ratesList.add(rateTag);
        }
        tag.put("ExchangeRates", ratesList);

        ListTag indexList = new ListTag();
        for (Map.Entry<String, Double> entry : currentPriceIndex.entrySet()) {
            CompoundTag indexTag = new CompoundTag();
            indexTag.putString("Item", entry.getKey());
            indexTag.putDouble("Price", entry.getValue());
            indexList.add(indexTag);
        }
        tag.put("PriceIndex", indexList);

        return tag;
    }

    public static GlobalMarketManager load(CompoundTag tag) {
        GlobalMarketManager manager = new GlobalMarketManager();
        manager.tradesSinceLastUpdate = tag.getInt("TradesSinceUpdate");

        ListTag ratesList = tag.getList("ExchangeRates", Tag.TAG_COMPOUND);
        for (int i = 0; i < ratesList.size(); i++) {
            CompoundTag rateTag = ratesList.getCompound(i);
            manager.exchangeRates.put(rateTag.getString("Edge"), rateTag.getDouble("Rate"));
        }

        ListTag indexList = tag.getList("PriceIndex", Tag.TAG_COMPOUND);
        for (int i = 0; i < indexList.size(); i++) {
            CompoundTag indexTag = indexList.getCompound(i);
            manager.currentPriceIndex.put(indexTag.getString("Item"), indexTag.getDouble("Price"));
        }
        return manager;
    }
}