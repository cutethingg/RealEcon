package com.femtendo.realecon.content.wagebox;

import com.femtendo.realecon.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class WageboxStorageMenu extends AbstractContainerMenu {
    private final WageboxBlockEntity blockEntity;

    // Client-side constructor (Called by Forge when opening the GUI)
    public WageboxStorageMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    // Server-side constructor
    public WageboxStorageMenu(int containerId, Inventory inv, BlockEntity entity) {
        super(ModMenuTypes.WAGEBOX_STORAGE_MENU.get(), containerId);

        // Safety check: If the block entity is wrong or null, we shouldn't continue
        if (entity instanceof WageboxBlockEntity wagebox) {
            this.blockEntity = wagebox;
        } else {
            throw new IllegalStateException("Block entity is not a Wagebox!");
        }

        // 1. Add the 27 Storage Slots (3x9 grid)
        // Uses the getInventory() method we added to the Block Entity
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new SlotItemHandler(blockEntity.getInventory(), col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // 2. Player Inventory (The 3 rows above the hotbar)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // 3. Player Hotbar (The bottom-most row)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // 0-26 are the Wagebox slots
            if (index < 27) {
                // Move from Box to Player Inventory
                if (!this.moveItemStackTo(itemstack1, 27, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from Player Inventory to Box
                if (!this.moveItemStackTo(itemstack1, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        // Ensure the player is close enough to the block to use it
        return true;
    }
}