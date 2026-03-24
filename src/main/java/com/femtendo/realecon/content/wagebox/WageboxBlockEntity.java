package com.femtendo.realecon.content.wagebox;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class WageboxBlockEntity extends BlockEntity implements MenuProvider {

    private UUID ownerId;
    private final List<WageTrade> trades = new ArrayList<>();

    // NEW: Tracks any players currently looking at the Trading Post GUI
    private final List<WageboxMerchant> activeMerchants = new CopyOnWriteArrayList<>();
    private boolean isProcessingTrade = false;

    private final ItemStackHandler inventory = new ItemStackHandler(27) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            // If a hopper takes an item, instantly tell the UI to update!
            if (!isProcessingTrade) {
                updateActiveMerchants();
            }
        }
    };

    private final LazyOptional<IItemHandler> optionalInventory = LazyOptional.of(() -> inventory);

    public WageboxBlockEntity(BlockEntityType<?> type, @NotNull BlockPos pos, @NotNull BlockState state) {
        super(type, pos, state);
    }

    public ItemStackHandler getInventory() {
        return this.inventory;
    }

    public void setOwner(UUID owner) {
        this.ownerId = owner;
        setChanged();
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public List<WageTrade> getTrades() {
        return trades;
    }

    public void addTrade(WageTrade trade) {
        if (trades.size() < 20) {
            trades.add(trade);
            setChanged();
        }
    }

    // --- NEW: UI SYNCING LOGIC ---
    public void registerMerchant(WageboxMerchant merchant) {
        if (!activeMerchants.contains(merchant)) {
            activeMerchants.add(merchant);
        }
    }

    public void unregisterMerchant(WageboxMerchant merchant) {
        activeMerchants.remove(merchant);
    }

    private void updateActiveMerchants() {
        if (this.level != null && !this.level.isClientSide()) {
            for (WageboxMerchant merchant : activeMerchants) {
                merchant.refreshOffers();
            }
        }
    }
    // -----------------------------

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return optionalInventory.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        optionalInventory.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
        tag.put("Inventory", inventory.serializeNBT());
        ListTag tradesTag = new ListTag();
        for (WageTrade trade : trades) {
            tradesTag.add(trade.serializeNBT());
        }
        tag.put("Trades", tradesTag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("Owner")) {
            this.ownerId = tag.getUUID("Owner");
        }
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        trades.clear();
        ListTag tradesTag = tag.getList("Trades", Tag.TAG_COMPOUND);
        for (int i = 0; i < tradesTag.size(); i++) {
            trades.add(WageTrade.deserializeNBT(tradesTag.getCompound(i)));
        }
    }

    public int calculateAvailableUses(WageTrade trade) {
        ItemStack requiredReward = trade.getReward();
        ItemStack incomingBounty = trade.getBounty();

        if (requiredReward.isEmpty() || incomingBounty.isEmpty()) return 0;

        int availableRewards = 0;
        int availableBountySpace = 0;

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (ItemStack.isSameItemSameTags(stackInSlot, requiredReward)) {
                availableRewards += stackInSlot.getCount();
            }
            if (stackInSlot.isEmpty()) {
                availableBountySpace += incomingBounty.getMaxStackSize();
            } else if (ItemStack.isSameItemSameTags(stackInSlot, incomingBounty)) {
                availableBountySpace += (stackInSlot.getMaxStackSize() - stackInSlot.getCount());
            }
        }

        int possibleByRewards = availableRewards / requiredReward.getCount();
        int possibleBySpace = availableBountySpace / incomingBounty.getCount();

        return Math.min(possibleByRewards, possibleBySpace);
    }

    public void processTrade(MerchantOffer offer, Player buyer) {
        // Halt UI updates temporarily while we move items around
        this.isProcessingTrade = true;

        ItemStack targetReward = offer.getResult();
        ItemStack bountyReceived = offer.getBaseCostA().copy();

        int amountToExtract = targetReward.getCount();

        for (int i = 0; i < inventory.getSlots(); i++) {
            if (amountToExtract <= 0) break;
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (!stackInSlot.isEmpty() && ItemStack.isSameItemSameTags(stackInSlot, targetReward)) {
                int toTake = Math.min(stackInSlot.getCount(), amountToExtract);
                inventory.extractItem(i, toTake, false);
                amountToExtract -= toTake;
            }
        }

        for (int i = 0; i < inventory.getSlots(); i++) {
            if (bountyReceived.isEmpty()) break;
            bountyReceived = inventory.insertItem(i, bountyReceived, false);
        }

        if (this.ownerId != null && this.level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            String buyerName = buyer != null ? buyer.getName().getString() : "Unknown";
            com.femtendo.realecon.content.ledger.data.CompletedTrade receipt =
                    com.femtendo.realecon.content.ledger.data.CompletedTrade.createNew(offer.getBaseCostA(), offer.getResult(), buyerName);
            com.femtendo.realecon.logic.EmpireManager.get(serverLevel).recordTrade(this.ownerId, receipt);
        }

        // Trade finished. Resume UI updates and fire one off to show the new stock levels.
        this.isProcessingTrade = false;
        updateActiveMerchants();
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Wagebox Inventory");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new WageboxStorageMenu(containerId, playerInventory, this);
    }
}