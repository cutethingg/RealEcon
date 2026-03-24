package com.femtendo.realecon.content.wagebox;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class WageTrade {
    private ItemStack bounty;
    private ItemStack reward;

    public WageTrade(ItemStack bounty, ItemStack reward) {
        this.bounty = bounty;
        this.reward = reward;
    }

    public ItemStack getBounty() { return bounty; }
    public ItemStack getReward() { return reward; }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("Bounty", bounty.serializeNBT());
        tag.put("Reward", reward.serializeNBT());
        return tag;
    }

    public static WageTrade deserializeNBT(CompoundTag tag) {
        ItemStack b = ItemStack.of(tag.getCompound("Bounty"));
        ItemStack r = ItemStack.of(tag.getCompound("Reward"));
        return new WageTrade(b, r);
    }
}