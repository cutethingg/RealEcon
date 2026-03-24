package com.femtendo.realecon.logic;

import com.femtendo.realecon.CurrencyCache;
import com.femtendo.realecon.capability.PlayerWealthProvider;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.claims.api.IServerClaimsManagerAPI;

import java.util.Map;

public class WealthScanner {

    // ---------------------------------------------------------
    // PILLAR I: INVENTORY & ENDER CHEST (Attached to Player)
    // ---------------------------------------------------------
    public static long scanPlayerPocket(ServerPlayer player) {
        long total = 0;

        // 1. Standard Inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            total += CurrencyCache.getValue(player.getInventory().getItem(i));
        }

        // 2. Ender Chest Inventory (Fix)
        for (int i = 0; i < player.getEnderChestInventory().getContainerSize(); i++) {
            total += CurrencyCache.getValue(player.getEnderChestInventory().getItem(i));
        }

        return total;
    }

    // ---------------------------------------------------------
    // PILLAR II: DEEP CHUNK SCAN (Containers + Physical Blocks)
    // ---------------------------------------------------------
    public static long scanClaimedChunks(ServerPlayer player) {
        long totalClaimWealth = 0;
        ServerLevel level = player.serverLevel();

        IServerClaimsManagerAPI claimsAPI = OpenPACServerAPI.get(level.getServer()).getServerClaimsManager();

        for (LevelChunk chunk : LoadedChunkTracker.getLoadedChunks(level)) {
            ChunkPos cPos = chunk.getPos();

            IPlayerChunkClaimAPI claim = claimsAPI.get(level.dimension().location(), cPos);
            if (claim != null && claim.getPlayerId().equals(player.getUUID())) {
                totalClaimWealth += scanSingleChunk(chunk);
            }
        }
        return totalClaimWealth;
    }

    private static long scanSingleChunk(LevelChunk chunk) {
        long chunkWealth = 0;

        // 1. SCAN BLOCK ENTITIES (Chests, Vaults, Hoppers)
        for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
            BlockEntity be = entry.getValue();
            BlockPos pos = entry.getKey();

            if (be instanceof IMultiBlockEntityContainer multiContainer) {
                if (!pos.equals(multiContainer.getController())) continue;
            }

            var cap = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (cap.isPresent()) {
                IItemHandler handler = cap.orElse(null);
                for (int i = 0; i < handler.getSlots(); i++) {
                    chunkWealth += CurrencyCache.getValue(handler.getStackInSlot(i));
                }
            }
        }

        // 2. SCAN PHYSICAL PLACED BLOCKS (Gold Blocks on the ground)
        for (LevelChunkSection section : chunk.getSections()) {
            if (section == null || section.hasOnlyAir()) continue;

            // Iterate the 16x16x16 block grid of this section
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        int blockValue = CurrencyCache.getBlockValue(section.getBlockState(x, y, z));
                        if (blockValue > 0) {
                            chunkWealth += blockValue;
                        }
                    }
                }
            }
        }

        return chunkWealth;
    }

    public static void updatePlayerWealth(ServerPlayer player, boolean deepScan) {
        long pocketMoney = scanPlayerPocket(player);
        long bankMoney = 0;

        if (deepScan) {
            bankMoney = scanClaimedChunks(player);
        }

        long finalTotal = pocketMoney + bankMoney;
        long currentTime = System.currentTimeMillis();

        player.getCapability(PlayerWealthProvider.PLAYER_WEALTH).ifPresent(wealth -> {
            long oldBalance = wealth.getLastCheckedWealth();
            long lastTime = wealth.getLastCheckTimeMs();

            // Run the Security Audit BEFORE saving the new data
            if (deepScan) {
                SecurityLogger.audit(player, finalTotal, oldBalance, lastTime);
                // Update the memory for the next check
                wealth.setLastCheckedData(finalTotal, currentTime);
            }

            wealth.setNetWorth(finalTotal);
        });
    }
}