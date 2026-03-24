package com.femtendo.realecon.content.ledger.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class EmpireProfile {
    // Total physical items imported (bounties received) and exported (rewards given out)
    private long totalItemsImported = 0;
    private long totalItemsExported = 0;

    // We cap the history at 50 to prevent the server packet from getting too massive
    private final List<CompletedTrade> tradeHistory = new ArrayList<>();

    public void addCompletedTrade(CompletedTrade trade) {
        totalItemsImported += trade.getBountyPaid().getCount();
        totalItemsExported += trade.getRewardGiven().getCount();

        tradeHistory.add(0, trade); // Add to the top of the list (newest first)

        if (tradeHistory.size() > 50) {
            tradeHistory.remove(tradeHistory.size() - 1); // Delete the oldest record
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("TotalImported", totalItemsImported);
        tag.putLong("TotalExported", totalItemsExported);

        ListTag historyTag = new ListTag();
        for (CompletedTrade trade : tradeHistory) {
            historyTag.add(trade.serializeNBT());
        }
        tag.put("History", historyTag);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        totalItemsImported = tag.getLong("TotalImported");
        totalItemsExported = tag.getLong("TotalExported");

        tradeHistory.clear();
        ListTag historyTag = tag.getList("History", Tag.TAG_COMPOUND);
        for (int i = 0; i < historyTag.size(); i++) {
            tradeHistory.add(CompletedTrade.deserializeNBT(historyTag.getCompound(i)));
        }
    }

    // Getters
    public long getTotalItemsImported() { return totalItemsImported; }
    public long getTotalItemsExported() { return totalItemsExported; }
    public List<CompletedTrade> getTradeHistory() { return tradeHistory; }
}