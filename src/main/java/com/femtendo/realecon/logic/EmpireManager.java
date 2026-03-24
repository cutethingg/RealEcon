package com.femtendo.realecon.logic;

import com.femtendo.realecon.content.ledger.data.ActiveTradeNode;
import com.femtendo.realecon.content.ledger.data.CompletedTrade;
import com.femtendo.realecon.content.ledger.data.EmpireProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EmpireManager extends SavedData {

    // 1. The Player Stats
    private final Map<UUID, EmpireProfile> playerProfiles = new HashMap<>();

    // 2. NEW: The Global Yellow Pages (Mapped by the block's exact XYZ coordinate as a Long)
    private final Map<Long, ActiveTradeNode> activeWageboxes = new HashMap<>();

    public EmpireProfile getProfile(UUID playerId) {
        return playerProfiles.computeIfAbsent(playerId, k -> new EmpireProfile());
    }

    public void recordTrade(UUID ownerId, CompletedTrade trade) {
        getProfile(ownerId).addCompletedTrade(trade);
        this.setDirty();
    }

    // --- NEW: YELLOW PAGES LOGIC ---

    // Called when a player programs a Wagebox
    public void registerWagebox(BlockPos pos, UUID ownerId, String ownerName, List<com.femtendo.realecon.content.wagebox.WageTrade> trades) {
        if (trades.isEmpty()) {
            activeWageboxes.remove(pos.asLong()); // If they wiped it empty, remove it from the directory
        } else {
            activeWageboxes.put(pos.asLong(), new ActiveTradeNode(pos, ownerId, ownerName, trades));
        }
        this.setDirty();
    }

    // Called when a player breaks a Wagebox with an axe
    public void unregisterWagebox(BlockPos pos) {
        activeWageboxes.remove(pos.asLong());
        this.setDirty();
    }

    // Used by the Master Ledger to get the list
    public Map<Long, ActiveTradeNode> getActiveWageboxes() {
        return activeWageboxes;
    }

    // --- SAVING/LOADING LOGIC ---

    public static EmpireManager get(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (overworld == null) return new EmpireManager();
        return overworld.getDataStorage().computeIfAbsent(EmpireManager::load, EmpireManager::new, "realecon_empires");
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        // Save Profiles
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, EmpireProfile> entry : playerProfiles.entrySet()) {
            playersTag.put(entry.getKey().toString(), entry.getValue().serializeNBT());
        }
        tag.put("Players", playersTag);

        // Save Yellow Pages
        CompoundTag nodesTag = new CompoundTag();
        for (Map.Entry<Long, ActiveTradeNode> entry : activeWageboxes.entrySet()) {
            nodesTag.put(entry.getKey().toString(), entry.getValue().serializeNBT());
        }
        tag.put("YellowPages", nodesTag);

        return tag;
    }

    public static EmpireManager load(CompoundTag tag) {
        EmpireManager manager = new EmpireManager();

        // Load Profiles
        CompoundTag playersTag = tag.getCompound("Players");
        for (String key : playersTag.getAllKeys()) {
            try {
                EmpireProfile profile = new EmpireProfile();
                profile.deserializeNBT(playersTag.getCompound(key));
                manager.playerProfiles.put(UUID.fromString(key), profile);
            } catch (IllegalArgumentException ignored) {}
        }

        // Load Yellow Pages
        CompoundTag nodesTag = tag.getCompound("YellowPages");
        for (String key : nodesTag.getAllKeys()) {
            try {
                manager.activeWageboxes.put(Long.parseLong(key), ActiveTradeNode.deserializeNBT(nodesTag.getCompound(key)));
            } catch (NumberFormatException ignored) {}
        }

        return manager;
    }
}