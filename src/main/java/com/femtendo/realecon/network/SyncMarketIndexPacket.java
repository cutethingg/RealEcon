package com.femtendo.realecon.network;

import com.femtendo.realecon.client.ClientMarketData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncMarketIndexPacket {
    private final Map<String, Double> prices;
    private final int tradesSinceUpdate;
    private final int tradesUntilUpdate;

    public SyncMarketIndexPacket(Map<String, Double> prices, int tradesSinceUpdate, int tradesUntilUpdate) {
        this.prices = prices;
        this.tradesSinceUpdate = tradesSinceUpdate;
        this.tradesUntilUpdate = tradesUntilUpdate;
    }

    public SyncMarketIndexPacket(FriendlyByteBuf buf) {
        this.prices = new HashMap<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            this.prices.put(buf.readUtf(), buf.readDouble());
        }
        this.tradesSinceUpdate = buf.readInt();
        this.tradesUntilUpdate = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.prices.size());
        for (Map.Entry<String, Double> entry : this.prices.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeDouble(entry.getValue());
        }
        buf.writeInt(this.tradesSinceUpdate);
        buf.writeInt(this.tradesUntilUpdate);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientMarketData.PRICES.clear();
            ClientMarketData.PRICES.putAll(this.prices);
            ClientMarketData.tradesSinceUpdate = this.tradesSinceUpdate;
            ClientMarketData.tradesUntilUpdate = this.tradesUntilUpdate;
        });
        context.setPacketHandled(true);
        return true;
    }
}