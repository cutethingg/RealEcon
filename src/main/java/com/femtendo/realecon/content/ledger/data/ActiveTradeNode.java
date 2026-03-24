package com.femtendo.realecon.content.ledger.data;

import com.femtendo.realecon.content.wagebox.WageTrade;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ActiveTradeNode {
    private final BlockPos pos;
    private final UUID ownerId;
    private final String ownerName;
    private final List<WageTrade> trades;

    public ActiveTradeNode(BlockPos pos, UUID ownerId, String ownerName, List<WageTrade> trades) {
        this.pos = pos;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        // We copy the list so it isn't tied to the physical block's memory
        this.trades = new ArrayList<>(trades);
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Pos", pos.asLong());
        tag.putUUID("OwnerId", ownerId);
        tag.putString("OwnerName", ownerName);

        ListTag tradesTag = new ListTag();
        for (WageTrade trade : trades) {
            tradesTag.add(trade.serializeNBT());
        }
        tag.put("Trades", tradesTag);
        return tag;
    }

    public static ActiveTradeNode deserializeNBT(CompoundTag tag) {
        BlockPos pos = BlockPos.of(tag.getLong("Pos"));
        UUID ownerId = tag.getUUID("OwnerId");
        String ownerName = tag.getString("OwnerName");

        List<WageTrade> loadedTrades = new ArrayList<>();
        ListTag tradesTag = tag.getList("Trades", Tag.TAG_COMPOUND);
        for (int i = 0; i < tradesTag.size(); i++) {
            loadedTrades.add(WageTrade.deserializeNBT(tradesTag.getCompound(i)));
        }
        return new ActiveTradeNode(pos, ownerId, ownerName, loadedTrades);
    }

    // Getters for the UI
    public BlockPos getPos() { return pos; }
    public UUID getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public List<WageTrade> getTrades() { return trades; }
}