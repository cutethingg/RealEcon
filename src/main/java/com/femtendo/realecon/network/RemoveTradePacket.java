package com.femtendo.realecon.network;

import com.femtendo.realecon.content.ledger.TradeLedgerItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RemoveTradePacket {
    private final int index;

    public RemoveTradePacket(int index) { this.index = index; }
    public RemoveTradePacket(FriendlyByteBuf buf) { this.index = buf.readInt(); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeInt(index); }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                ItemStack ledger = player.getMainHandItem();
                if (ledger.getItem() instanceof TradeLedgerItem) {
                    CompoundTag tag = ledger.getOrCreateTag();
                    if (tag.contains("SavedTrades")) {
                        ListTag trades = tag.getList("SavedTrades", Tag.TAG_COMPOUND);
                        if (index >= 0 && index < trades.size()) {
                            trades.remove(index);
                            // Forces the client-side Menu to sync the item's new NBT
                            player.containerMenu.broadcastChanges();
                        }
                    }
                }
            }
        });
        return true;
    }
}