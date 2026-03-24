package com.femtendo.realecon.logic;

import com.femtendo.realecon.RealEcon;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = RealEcon.MODID)
public class LoadedChunkTracker {

    // Maps a Dimension to a thread-safe Set of currently loaded chunks
    private static final Map<ServerLevel, Set<LevelChunk>> LOADED_CHUNKS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        // Only track fully loaded chunks on the server side
        if (event.getLevel() instanceof ServerLevel serverLevel && event.getChunk() instanceof LevelChunk chunk) {
            LOADED_CHUNKS.computeIfAbsent(serverLevel, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(chunk);
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && event.getChunk() instanceof LevelChunk chunk) {
            Set<LevelChunk> chunks = LOADED_CHUNKS.get(serverLevel);
            if (chunks != null) {
                chunks.remove(chunk);
            }
        }
    }

    // A clean, public getter we can use anywhere
    public static Set<LevelChunk> getLoadedChunks(ServerLevel level) {
        return LOADED_CHUNKS.getOrDefault(level, Collections.emptySet());
    }
}