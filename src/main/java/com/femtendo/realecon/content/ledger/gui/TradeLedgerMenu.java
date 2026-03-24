package com.femtendo.realecon.content.ledger.gui;

import com.femtendo.realecon.init.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class TradeLedgerMenu extends AbstractContainerMenu {
    private final Player player;
    public final ItemStackHandler inputSlots = new ItemStackHandler(2);

    public TradeLedgerMenu(int id, Inventory playerInv) {
        super(ModMenuTypes.TRADE_LEDGER_MENU.get(), id);
        this.player = playerInv.player;

        // REVERTED & MOVED EXACTLY 1 TILE LEFT (-18 pixels on the X-axis)
        this.addSlot(new SlotItemHandler(inputSlots, 0, 28, 52)); // Checkmark Slot
        this.addSlot(new SlotItemHandler(inputSlots, 1, 78, 52)); // Fire Slot

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInv, k, 8 + k * 18, 142));
        }
    }

    public ItemStack getLedgerStack() {
        return this.player.getMainHandItem();
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (slotId >= 0 && slotId <= 1) {
            ItemStack carried = getCarried();
            if (carried.isEmpty()) {
                inputSlots.setStackInSlot(slotId, ItemStack.EMPTY);
            } else {
                ItemStack copy = carried.copy();
                copy.setCount(carried.getCount());
                inputSlots.setStackInSlot(slotId, copy);
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return !getLedgerStack().isEmpty();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }
}