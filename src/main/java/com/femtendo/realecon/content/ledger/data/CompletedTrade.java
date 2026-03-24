package com.femtendo.realecon.content.ledger.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CompletedTrade {
    private final ItemStack bountyPaid;
    private final ItemStack rewardGiven;
    private final String buyerName;
    private final String timestamp;

    public CompletedTrade(ItemStack bountyPaid, ItemStack rewardGiven, String buyerName, String timestamp) {
        this.bountyPaid = bountyPaid;
        this.rewardGiven = rewardGiven;
        this.buyerName = buyerName;
        this.timestamp = timestamp;
    }

    public static CompletedTrade createNew(ItemStack bounty, ItemStack reward, String buyer) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));
        return new CompletedTrade(bounty, reward, buyer, time);
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("Bounty", bountyPaid.serializeNBT());
        tag.put("Reward", rewardGiven.serializeNBT());
        tag.putString("Buyer", buyerName);
        tag.putString("Time", timestamp);
        return tag;
    }

    public static CompletedTrade deserializeNBT(CompoundTag tag) {
        ItemStack b = ItemStack.of(tag.getCompound("Bounty"));
        ItemStack r = ItemStack.of(tag.getCompound("Reward"));
        String buyer = tag.getString("Buyer");
        String time = tag.getString("Time");
        return new CompletedTrade(b, r, buyer, time);
    }

    // Getters for the UI later
    public ItemStack getBountyPaid() { return bountyPaid; }
    public ItemStack getRewardGiven() { return rewardGiven; }
    public String getBuyerName() { return buyerName; }
    public String getTimestamp() { return timestamp; }
}