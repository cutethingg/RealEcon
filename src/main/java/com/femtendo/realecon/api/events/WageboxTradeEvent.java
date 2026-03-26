package com.femtendo.realecon.api.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.UUID;

/**
 * Fired immediately when a player attempts to execute a trade at a Wagebox,
 * BEFORE any items are extracted or deposited.
 */
@Cancelable
public class WageboxTradeEvent extends Event {
    private final BlockPos pos;
    private final Player buyer;
    private final UUID ownerId;
    private ItemStack bounty;
    private ItemStack reward;

    public WageboxTradeEvent(BlockPos pos, Player buyer, UUID ownerId, ItemStack bounty, ItemStack reward) {
        this.pos = pos;
        this.buyer = buyer;
        this.ownerId = ownerId;
        this.bounty = bounty;
        this.reward = reward;
    }

    public BlockPos getPos() { return pos; }
    public Player getBuyer() { return buyer; }
    public UUID getOwnerId() { return ownerId; }

    public ItemStack getBounty() { return bounty; }
    public void setBounty(ItemStack bounty) { this.bounty = bounty; }

    public ItemStack getReward() { return reward; }
    public void setReward(ItemStack reward) { this.reward = reward; }
}