package com.femtendo.realecon.api.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.Event;

import java.util.Map;

/**
 * Fired immediately after the Global Market Index recalculates its prices.
 */
public class MarketUpdateEvent extends Event {
    private final ServerLevel level;
    private final Map<String, Double> newPrices;

    public MarketUpdateEvent(ServerLevel level, Map<String, Double> newPrices) {
        this.level = level;
        this.newPrices = newPrices;
    }

    public ServerLevel getLevel() { return level; }
    public Map<String, Double> getNewPrices() { return newPrices; }
}