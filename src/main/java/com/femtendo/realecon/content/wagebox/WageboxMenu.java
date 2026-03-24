package com.femtendo.realecon.content.wagebox;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WageboxMenu extends MerchantMenu {

    private final WageboxBlockEntity blockEntity;

    public WageboxMenu(int containerId, Inventory playerInv, WageboxMerchant merchant, WageboxBlockEntity blockEntity) {
        super(containerId, playerInv, merchant);
        this.blockEntity = blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index == 2) { // The Result Slot (Trade Output)

                // --- CRITICAL SERVER-SIDE VALIDATION ---
                // Prevents network latency or hacked clients from grabbing an item that just went out of stock
                if (!slot.mayPickup(player)) {
                    return ItemStack.EMPTY;
                }
                // ---------------------------------------

                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);

                if (this.blockEntity != null && this.blockEntity.getLevel() != null) {
                    BlockPos pos = this.blockEntity.getBlockPos();
                    this.blockEntity.getLevel().playSound(null, pos, net.minecraft.sounds.SoundEvents.VILLAGER_YES, SoundSource.BLOCKS, 1.0F, 1.0F);
                }

            } else if (index != 0 && index != 1) {
                if (index >= 3 && index < 30) {
                    if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 30 && index < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }
}