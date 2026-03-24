package com.femtendo.realecon.network;

import com.femtendo.realecon.content.ledger.TradeLedgerItem;
import com.femtendo.realecon.content.wagebox.WageTrade;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SaveLedgerTradePacket {
    private final ItemStack bounty;
    private final ItemStack reward;

    public SaveLedgerTradePacket(ItemStack bounty, ItemStack reward) {
        this.bounty = bounty;
        this.reward = reward;
    }

    public SaveLedgerTradePacket(FriendlyByteBuf buf) {
        this.bounty = buf.readItem();
        this.reward = buf.readItem();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeItem(bounty);
        buf.writeItem(reward);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            // Get the item the player is currently holding (should be the Ledger)
            ItemStack mainHandItem = player.getMainHandItem();
            if (mainHandItem.getItem() instanceof TradeLedgerItem) {

                // Read the existing data or create a new tag
                CompoundTag tag = mainHandItem.getOrCreateTag();
                ListTag tradesList = tag.getList("SavedTrades", CompoundTag.TAG_COMPOUND);

                // Add the new trade to the list (max 20)
                if (tradesList.size() < 20) {
                    WageTrade newTrade = new WageTrade(bounty, reward);
                    tradesList.add(newTrade.serializeNBT());
                    tag.put("SavedTrades", tradesList);
                    mainHandItem.setTag(tag);

                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aTrade added to Ledger! (" + tradesList.size() + "/20)"));
                } else {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cLedger is full! (20/20 trades)"));
                }
            }
        });
        return true;
    }
}