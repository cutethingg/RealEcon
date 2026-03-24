package com.femtendo.realecon.network;

import com.femtendo.realecon.RealEcon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RealEcon.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        // 1. Save Trade Packet
        INSTANCE.registerMessage(id(),
                SaveLedgerTradePacket.class,
                SaveLedgerTradePacket::toBytes,
                SaveLedgerTradePacket::new,
                SaveLedgerTradePacket::handle);

        // 2. Sync Empire Data Packet
        INSTANCE.registerMessage(id(),
                SyncEmpireDataPacket.class,
                SyncEmpireDataPacket::toBytes,
                SyncEmpireDataPacket::new,
                SyncEmpireDataPacket::handle);

        // 3. Remove Trade Packet
        INSTANCE.registerMessage(id(),
                RemoveTradePacket.class,
                RemoveTradePacket::toBytes,
                RemoveTradePacket::new,
                RemoveTradePacket::handle);

        // 4. Sync Market Index Packet (NEW)
        INSTANCE.registerMessage(id(),
                SyncMarketIndexPacket.class,
                SyncMarketIndexPacket::toBytes,
                SyncMarketIndexPacket::new,
                SyncMarketIndexPacket::handle);
    }

    // Helper method to send from client to server
    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    // Helper method to send from server to a specific client
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}