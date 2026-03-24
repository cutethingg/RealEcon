package com.femtendo.realecon.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncEmpireDataPacket {
    private final CompoundTag playerProfile;
    private final CompoundTag yellowPages;

    public SyncEmpireDataPacket(CompoundTag playerProfile, CompoundTag yellowPages) {
        this.playerProfile = playerProfile;
        this.yellowPages = yellowPages;
    }

    public SyncEmpireDataPacket(FriendlyByteBuf buf) {
        this.playerProfile = buf.readNbt();
        this.yellowPages = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(playerProfile);
        buf.writeNbt(yellowPages);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Beams the data to the client-side screen
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                com.femtendo.realecon.content.ledger.gui.MasterLedgerScreen.open(playerProfile, yellowPages);
            });
        });
        return true;
    }
}